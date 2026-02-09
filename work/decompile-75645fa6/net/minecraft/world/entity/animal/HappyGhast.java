package net.minecraft.world.entity.animal;

import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.entity.ai.control.ControllerMoveFlying;
import net.minecraft.world.entity.ai.control.EntityAIBodyControl;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalTempt;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.navigation.NavigationFlying;
import net.minecraft.world.entity.monster.EntityGhast;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import org.jetbrains.annotations.Nullable;

public class HappyGhast extends EntityAnimal {

    public static final float BABY_SCALE = 0.2375F;
    public static final int WANDER_GROUND_DISTANCE = 16;
    public static final int SMALL_RESTRICTION_RADIUS = 32;
    public static final int LARGE_RESTRICTION_RADIUS = 64;
    public static final int RESTRICTION_RADIUS_BUFFER = 16;
    public static final int FAST_HEALING_TICKS = 20;
    public static final int SLOW_HEALING_TICKS = 600;
    public static final int MAX_PASSANGERS = 4;
    private static final int STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD = 60;
    private static final int MAX_STILL_TIMEOUT = 10;
    public static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0F;
    public static final Predicate<ItemStack> IS_FOOD = (itemstack) -> {
        return itemstack.is(TagsItem.HAPPY_GHAST_FOOD);
    };
    private int leashHolderTime = 0;
    private int serverStillTimeout;
    private static final DataWatcherObject<Boolean> IS_LEASH_HOLDER = DataWatcher.<Boolean>defineId(HappyGhast.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Boolean> STAYS_STILL = DataWatcher.<Boolean>defineId(HappyGhast.class, DataWatcherRegistry.BOOLEAN);
    private static final float MAX_SCALE = 1.0F;

    public HappyGhast(EntityTypes<? extends HappyGhast> entitytypes, World world) {
        super(entitytypes, world);
        this.moveControl = new EntityGhast.ControllerGhast(this, true, this::isOnStillTimeout);
        this.lookControl = new HappyGhast.d();
    }

    private void setServerStillTimeout(int i) {
        if (this.serverStillTimeout <= 0 && i > 0) {
            World world = this.level();

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
                worldserver.getChunkSource().chunkMap.sendToTrackingPlayers(this, ClientboundEntityPositionSyncPacket.of(this));
            }
        }

        this.serverStillTimeout = i;
        this.syncStayStillFlag();
    }

    private NavigationAbstract createBabyNavigation(World world) {
        return new HappyGhast.a(this, world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new HappyGhast.c());
        this.goalSelector.addGoal(4, new PathfinderGoalTempt.a(this, 1.0D, (itemstack) -> {
            return !this.isWearingBodyArmor() && !this.isBaby() ? itemstack.is(TagsItem.HAPPY_GHAST_TEMPT_ITEMS) : HappyGhast.IS_FOOD.test(itemstack);
        }, false, 7.0D));
        this.goalSelector.addGoal(5, new EntityGhast.PathfinderGoalGhastIdleMove(this, 16));
    }

    private void adultGhastSetup() {
        this.moveControl = new EntityGhast.ControllerGhast(this, true, this::isOnStillTimeout);
        this.lookControl = new HappyGhast.d();
        this.navigation = this.createNavigation(this.level());
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            this.removeAllGoals((pathfindergoal) -> {
                return true;
            });
            this.registerGoals();
            this.brain.stopAll(worldserver, this);
            this.brain.clearMemories();
        }

    }

    private void babyGhastSetup() {
        this.moveControl = new ControllerMoveFlying(this, 180, true);
        this.lookControl = new ControllerLook(this);
        this.navigation = this.createBabyNavigation(this.level());
        this.setServerStillTimeout(0);
        this.removeAllGoals((pathfindergoal) -> {
            return true;
        });
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.babyGhastSetup();
        } else {
            this.adultGhastSetup();
        }

        super.ageBoundaryReached();
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityAnimal.createAnimalAttributes().add(GenericAttributes.MAX_HEALTH, 20.0D).add(GenericAttributes.TEMPT_RANGE, 16.0D).add(GenericAttributes.FLYING_SPEED, 0.05D).add(GenericAttributes.MOVEMENT_SPEED, 0.05D).add(GenericAttributes.FOLLOW_RANGE, 16.0D).add(GenericAttributes.CAMERA_DISTANCE, 8.0D);
    }

    @Override
    protected float sanitizeScale(float f) {
        return Math.min(f, 1.0F);
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {}

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3D vec3d) {
        float f = (float) this.getAttributeValue(GenericAttributes.FLYING_SPEED) * 5.0F / 3.0F;

        this.travelFlying(vec3d, f, f, f);
    }

    @Override
    public float getWalkTargetValue(BlockPosition blockposition, IWorldReader iworldreader) {
        return !iworldreader.isEmptyBlock(blockposition) ? 0.0F : (iworldreader.isEmptyBlock(blockposition.below()) && !iworldreader.isEmptyBlock(blockposition.below(2)) ? 10.0F : 5.0F);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return this.isBaby() ? true : super.canBreatheUnderwater();
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {}

    @Override
    public float getVoicePitch() {
        return 1.0F;
    }

    @Override
    public SoundCategory getSoundSource() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public int getAmbientSoundInterval() {
        int i = super.getAmbientSoundInterval();

        return this.isVehicle() ? i * 6 : i;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.isBaby() ? SoundEffects.GHASTLING_AMBIENT : SoundEffects.HAPPY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return this.isBaby() ? SoundEffects.GHASTLING_HURT : SoundEffects.HAPPY_GHAST_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return this.isBaby() ? SoundEffects.GHASTLING_DEATH : SoundEffects.HAPPY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return this.isBaby() ? 1.0F : 4.0F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public @Nullable EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return EntityTypes.HAPPY_GHAST.create(worldserver, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.2375F : 1.0F;
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return HappyGhast.IS_FOOD.test(itemstack);
    }

    @Override
    public boolean canUseSlot(EnumItemSlot enumitemslot) {
        return enumitemslot != EnumItemSlot.BODY ? super.canUseSlot(enumitemslot) : this.isAlive() && !this.isBaby();
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EnumItemSlot enumitemslot) {
        return enumitemslot == EnumItemSlot.BODY;
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        if (this.isBaby()) {
            return super.mobInteract(entityhuman, enumhand);
        } else {
            ItemStack itemstack = entityhuman.getItemInHand(enumhand);

            if (!itemstack.isEmpty()) {
                EnumInteractionResult enuminteractionresult = itemstack.interactLivingEntity(entityhuman, this, enumhand);

                if (enuminteractionresult.consumesAction()) {
                    return enuminteractionresult;
                }
            }

            if (this.isWearingBodyArmor() && !entityhuman.isSecondaryUseActive()) {
                this.doPlayerRide(entityhuman);
                return EnumInteractionResult.SUCCESS;
            } else {
                return super.mobInteract(entityhuman, enumhand);
            }
        }
    }

    private void doPlayerRide(EntityHuman entityhuman) {
        if (!this.level().isClientSide()) {
            entityhuman.startRiding(this);
        }

    }

    @Override
    protected void addPassenger(Entity entity) {
        if (!this.isVehicle()) {
            this.level().playSound((Entity) null, this.getX(), this.getY(), this.getZ(), SoundEffects.HARNESS_GOGGLES_DOWN, this.getSoundSource(), 1.0F, 1.0F);
        }

        super.addPassenger(entity);
        if (!this.level().isClientSide()) {
            if (!this.scanPlayerAboveGhast()) {
                this.setServerStillTimeout(0);
            } else if (this.serverStillTimeout > 10) {
                this.setServerStillTimeout(10);
            }
        }

    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (!this.level().isClientSide()) {
            this.setServerStillTimeout(10);
        }

        if (!this.isVehicle()) {
            this.clearHome();
            this.level().playSound((Entity) null, this.getX(), this.getY(), this.getZ(), SoundEffects.HARNESS_GOGGLES_UP, this.getSoundSource(), 1.0F, 1.0F);
        }

    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < 4;
    }

    @Override
    public @Nullable EntityLiving getControllingPassenger() {
        Entity entity = this.getFirstPassenger();

        if (this.isWearingBodyArmor() && !this.isOnStillTimeout() && entity instanceof EntityHuman entityhuman) {
            return entityhuman;
        } else {
            return super.getControllingPassenger();
        }
    }

    @Override
    protected Vec3D getRiddenInput(EntityHuman entityhuman, Vec3D vec3d) {
        float f = entityhuman.xxa;
        float f1 = 0.0F;
        float f2 = 0.0F;

        if (entityhuman.zza != 0.0F) {
            float f3 = MathHelper.cos(entityhuman.getXRot() * ((float) Math.PI / 180F));
            float f4 = -MathHelper.sin(entityhuman.getXRot() * ((float) Math.PI / 180F));

            if (entityhuman.zza < 0.0F) {
                f3 *= -0.5F;
                f4 *= -0.5F;
            }

            f2 = f4;
            f1 = f3;
        }

        if (entityhuman.isJumping()) {
            f2 += 0.5F;
        }

        return (new Vec3D((double) f, (double) f2, (double) f1)).scale((double) 3.9F * this.getAttributeValue(GenericAttributes.FLYING_SPEED));
    }

    protected Vec2F getRiddenRotation(EntityLiving entityliving) {
        return new Vec2F(entityliving.getXRot() * 0.5F, entityliving.getYRot());
    }

    @Override
    protected void tickRidden(EntityHuman entityhuman, Vec3D vec3d) {
        super.tickRidden(entityhuman, vec3d);
        Vec2F vec2f = this.getRiddenRotation(entityhuman);
        float f = this.getYRot();
        float f1 = MathHelper.wrapDegrees(vec2f.y - f);
        float f2 = 0.08F;

        f += f1 * 0.08F;
        this.setRot(f, vec2f.x);
        this.yRotO = this.yBodyRot = this.yHeadRot = f;
    }

    @Override
    protected BehaviorController.b<HappyGhast> brainProvider() {
        return HappyGhastAi.brainProvider();
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return HappyGhastAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        if (this.isBaby()) {
            GameProfilerFiller gameprofilerfiller = Profiler.get();

            gameprofilerfiller.push("happyGhastBrain");
            this.brain.tick(worldserver, this);
            gameprofilerfiller.pop();
            gameprofilerfiller.push("happyGhastActivityUpdate");
            HappyGhastAi.updateActivity(this);
            gameprofilerfiller.pop();
        }

        this.checkRestriction();
        super.customServerAiStep(worldserver);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.leashHolderTime > 0) {
                --this.leashHolderTime;
            }

            this.setLeashHolder(this.leashHolderTime > 0);
            if (this.serverStillTimeout > 0) {
                if (this.tickCount > 60) {
                    --this.serverStillTimeout;
                }

                this.setServerStillTimeout(this.serverStillTimeout);
            }

            if (this.scanPlayerAboveGhast()) {
                this.setServerStillTimeout(10);
            }

        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            this.setRequiresPrecisePosition(this.isOnStillTimeout());
        }

        super.aiStep();
        this.continuousHeal();
    }

    private int getHappyGhastRestrictionRadius() {
        return !this.isBaby() && this.getItemBySlot(EnumItemSlot.BODY).isEmpty() ? 64 : 32;
    }

    private void checkRestriction() {
        if (!this.isLeashed() && !this.isVehicle()) {
            int i = this.getHappyGhastRestrictionRadius();

            if (!this.hasHome() || !this.getHomePosition().closerThan(this.blockPosition(), (double) (i + 16)) || i != this.getHomeRadius()) {
                this.setHomeTo(this.blockPosition(), i);
            }
        }
    }

    private void continuousHeal() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (this.isAlive() && this.deathTime == 0 && this.getMaxHealth() != this.getHealth()) {
                boolean flag = worldserver.dimensionType().natural() && (this.isInClouds() || worldserver.precipitationAt(this.blockPosition()) != BiomeBase.Precipitation.NONE);

                if (this.tickCount % (flag ? 20 : 600) == 0) {
                    this.heal(1.0F);
                }

                return;
            }
        }

    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(HappyGhast.IS_LEASH_HOLDER, false);
        datawatcher_a.define(HappyGhast.STAYS_STILL, false);
    }

    private void setLeashHolder(boolean flag) {
        this.entityData.set(HappyGhast.IS_LEASH_HOLDER, flag);
    }

    public boolean isLeashHolder() {
        return (Boolean) this.entityData.get(HappyGhast.IS_LEASH_HOLDER);
    }

    private void syncStayStillFlag() {
        this.entityData.set(HappyGhast.STAYS_STILL, this.serverStillTimeout > 0);
    }

    public boolean staysStill() {
        return (Boolean) this.entityData.get(HappyGhast.STAYS_STILL);
    }

    @Override
    public boolean supportQuadLeashAsHolder() {
        return true;
    }

    @Override
    public Vec3D[] getQuadLeashHolderOffsets() {
        return Leashable.createQuadLeashOffsets(this, -0.03125D, 0.4375D, 0.46875D, 0.03125D);
    }

    @Override
    public Vec3D getLeashOffset() {
        return Vec3D.ZERO;
    }

    @Override
    public double leashElasticDistance() {
        return 10.0D;
    }

    @Override
    public double leashSnapDistance() {
        return 16.0D;
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        this.getMoveControl().setWait();
    }

    @Override
    public void notifyLeashHolder(Leashable leashable) {
        if (leashable.supportQuadLeash()) {
            this.leashHolderTime = 5;
        }

    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueoutput) {
        super.addAdditionalSaveData(valueoutput);
        valueoutput.putInt("still_timeout", this.serverStillTimeout);
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueinput) {
        super.readAdditionalSaveData(valueinput);
        this.setServerStillTimeout(valueinput.getIntOr("still_timeout", 0));
    }

    public boolean isOnStillTimeout() {
        return this.staysStill() || this.serverStillTimeout > 0;
    }

    private boolean scanPlayerAboveGhast() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX - 1.0D, axisalignedbb.maxY - (double) 1.0E-5F, axisalignedbb.minZ - 1.0D, axisalignedbb.maxX + 1.0D, axisalignedbb.maxY + axisalignedbb.getYsize() / 2.0D, axisalignedbb.maxZ + 1.0D);

        for (EntityHuman entityhuman : this.level().players()) {
            if (!entityhuman.isSpectator()) {
                Entity entity = entityhuman.getRootVehicle();

                if (!(entity instanceof HappyGhast) && axisalignedbb1.contains(entity.position())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected EntityAIBodyControl createBodyControl() {
        return new HappyGhast.b();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity @nullable entity) {
        return !this.isBaby() && this.isAlive() ? (this.level().isClientSide() && @nullable entity instanceof EntityHuman && @nullable entity.position().y >= this.getBoundingBox().maxY ? true : (this.isVehicle() && @nullable entity instanceof HappyGhast ? true : this.isOnStillTimeout())) : false;
    }

    @Override
    public boolean isFlyingVehicle() {
        return !this.isBaby();
    }

    private static class a extends NavigationFlying {

        public a(HappyGhast happyghast, World world) {
            super(happyghast, world);
            this.setCanOpenDoors(false);
            this.setCanFloat(true);
            this.setRequiredPathLength(48.0F);
        }

        @Override
        protected boolean canMoveDirectly(Vec3D vec3d, Vec3D vec3d1) {
            return isClearForMovementBetween(this.mob, vec3d, vec3d1, false);
        }
    }

    private class c extends PathfinderGoalFloat {

        public c() {
            super(HappyGhast.this);
        }

        @Override
        public boolean canUse() {
            return !HappyGhast.this.isOnStillTimeout() && super.canUse();
        }
    }

    private class d extends ControllerLook {

        d() {
            super(HappyGhast.this);
        }

        @Override
        public void tick() {
            if (HappyGhast.this.isOnStillTimeout()) {
                float f = wrapDegrees90(HappyGhast.this.getYRot());

                HappyGhast.this.setYRot(HappyGhast.this.getYRot() - f);
                HappyGhast.this.setYHeadRot(HappyGhast.this.getYRot());
            } else if (this.lookAtCooldown > 0) {
                --this.lookAtCooldown;
                double d0 = this.wantedX - HappyGhast.this.getX();
                double d1 = this.wantedZ - HappyGhast.this.getZ();

                HappyGhast.this.setYRot(-((float) MathHelper.atan2(d0, d1)) * (180F / (float) Math.PI));
                HappyGhast.this.yBodyRot = HappyGhast.this.getYRot();
                HappyGhast.this.yHeadRot = HappyGhast.this.yBodyRot;
            } else {
                EntityGhast.faceMovementDirection(this.mob);
            }
        }

        public static float wrapDegrees90(float f) {
            float f1 = f % 90.0F;

            if (f1 >= 45.0F) {
                f1 -= 90.0F;
            }

            if (f1 < -45.0F) {
                f1 += 90.0F;
            }

            return f1;
        }
    }

    private class b extends EntityAIBodyControl {

        public b() {
            super(HappyGhast.this);
        }

        @Override
        public void clientTick() {
            if (HappyGhast.this.isVehicle()) {
                HappyGhast.this.yHeadRot = HappyGhast.this.getYRot();
                HappyGhast.this.yBodyRot = HappyGhast.this.yHeadRot;
            }

            super.clientTick();
        }
    }
}
