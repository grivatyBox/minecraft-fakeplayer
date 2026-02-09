package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsEntity;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerLook;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Pathfinder;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.phys.Vec3D;

public class Frog extends EntityAnimal {

    protected static final ImmutableList<SensorType<? extends Sensor<? super Frog>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FROG_ATTACKABLES, SensorType.FROG_TEMPTATIONS, SensorType.IS_IN_WATER);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, new MemoryModuleType[]{MemoryModuleType.IS_TEMPTED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.IS_IN_WATER, MemoryModuleType.IS_PREGNANT, MemoryModuleType.IS_PANICKING, MemoryModuleType.UNREACHABLE_TONGUE_TARGETS});
    private static final DataWatcherObject<Holder<FrogVariant>> DATA_VARIANT_ID = DataWatcher.<Holder<FrogVariant>>defineId(Frog.class, DataWatcherRegistry.FROG_VARIANT);
    private static final DataWatcherObject<OptionalInt> DATA_TONGUE_TARGET_ID = DataWatcher.<OptionalInt>defineId(Frog.class, DataWatcherRegistry.OPTIONAL_UNSIGNED_INT);
    private static final int FROG_FALL_DAMAGE_REDUCTION = 5;
    private static final ResourceKey<FrogVariant> DEFAULT_VARIANT = FrogVariants.TEMPERATE;
    public final AnimationState jumpAnimationState = new AnimationState();
    public final AnimationState croakAnimationState = new AnimationState();
    public final AnimationState tongueAnimationState = new AnimationState();
    public final AnimationState swimIdleAnimationState = new AnimationState();

    public Frog(EntityTypes<? extends EntityAnimal> entitytypes, World world) {
        super(entitytypes, world);
        this.lookControl = new Frog.a(this);
        this.setPathfindingMalus(PathType.WATER, 4.0F);
        this.setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
    }

    @Override
    protected BehaviorController.b<Frog> brainProvider() {
        return BehaviorController.<Frog>provider(Frog.MEMORY_TYPES, Frog.SENSOR_TYPES);
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return FrogAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public BehaviorController<Frog> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        IRegistry<FrogVariant> iregistry = this.registryAccess().lookupOrThrow(Registries.FROG_VARIANT);

        datawatcher_a.define(Frog.DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), Frog.DEFAULT_VARIANT));
        datawatcher_a.define(Frog.DATA_TONGUE_TARGET_ID, OptionalInt.empty());
    }

    public void eraseTongueTarget() {
        this.entityData.set(Frog.DATA_TONGUE_TARGET_ID, OptionalInt.empty());
    }

    public Optional<Entity> getTongueTarget() {
        IntStream intstream = ((OptionalInt) this.entityData.get(Frog.DATA_TONGUE_TARGET_ID)).stream();
        World world = this.level();

        Objects.requireNonNull(world);
        return intstream.mapToObj(world::getEntity).filter(Objects::nonNull).findFirst();
    }

    public void setTongueTarget(Entity entity) {
        this.entityData.set(Frog.DATA_TONGUE_TARGET_ID, OptionalInt.of(entity.getId()));
    }

    @Override
    public int getHeadRotSpeed() {
        return 35;
    }

    @Override
    public int getMaxHeadYRot() {
        return 5;
    }

    public Holder<FrogVariant> getVariant() {
        return (Holder) this.entityData.get(Frog.DATA_VARIANT_ID);
    }

    public void setVariant(Holder<FrogVariant> holder) {
        this.entityData.set(Frog.DATA_VARIANT_ID, holder);
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> datacomponenttype) {
        return (T) (datacomponenttype == DataComponents.FROG_VARIANT ? castComponentValue(datacomponenttype, this.getVariant()) : super.get(datacomponenttype));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        this.applyImplicitComponentIfPresent(datacomponentgetter, DataComponents.FROG_VARIANT);
        super.applyImplicitComponents(datacomponentgetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> datacomponenttype, T t0) {
        if (datacomponenttype == DataComponents.FROG_VARIANT) {
            this.setVariant((Holder) castComponentValue(DataComponents.FROG_VARIANT, t0));
            return true;
        } else {
            return super.applyImplicitComponent(datacomponenttype, t0);
        }
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        VariantUtils.writeVariant(nbttagcompound, this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        VariantUtils.readVariant(nbttagcompound, this.registryAccess(), Registries.FROG_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected void customServerAiStep(WorldServer worldserver) {
        GameProfilerFiller gameprofilerfiller = Profiler.get();

        gameprofilerfiller.push("frogBrain");
        this.getBrain().tick(worldserver, this);
        gameprofilerfiller.pop();
        gameprofilerfiller.push("frogActivityUpdate");
        FrogAi.updateActivity(this);
        gameprofilerfiller.pop();
        super.customServerAiStep(worldserver);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.swimIdleAnimationState.animateWhen(this.isInWater() && !this.walkAnimation.isMoving(), this.tickCount);
        }

        super.tick();
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (Frog.DATA_POSE.equals(datawatcherobject)) {
            EntityPose entitypose = this.getPose();

            if (entitypose == EntityPose.LONG_JUMPING) {
                this.jumpAnimationState.start(this.tickCount);
            } else {
                this.jumpAnimationState.stop();
            }

            if (entitypose == EntityPose.CROAKING) {
                this.croakAnimationState.start(this.tickCount);
            } else {
                this.croakAnimationState.stop();
            }

            if (entitypose == EntityPose.USING_TONGUE) {
                this.tongueAnimationState.start(this.tickCount);
            } else {
                this.tongueAnimationState.stop();
            }
        }

        super.onSyncedDataUpdated(datawatcherobject);
    }

    @Override
    protected void updateWalkAnimation(float f) {
        float f1;

        if (this.jumpAnimationState.isStarted()) {
            f1 = 0.0F;
        } else {
            f1 = Math.min(f * 25.0F, 1.0F);
        }

        this.walkAnimation.update(f1, 0.4F, this.isBaby() ? 3.0F : 1.0F);
    }

    @Override
    public void playEatingSound() {
        this.level().playSound((Entity) null, (Entity) this, SoundEffects.FROG_EAT, SoundCategory.NEUTRAL, 2.0F, 1.0F);
    }

    @Nullable
    @Override
    public EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        Frog frog = EntityTypes.FROG.create(worldserver, EntitySpawnReason.BREEDING);

        if (frog != null) {
            FrogAi.initMemories(frog, worldserver.getRandom());
        }

        return frog;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean flag) {}

    @Override
    public void spawnChildFromBreeding(WorldServer worldserver, EntityAnimal entityanimal) {
        this.finalizeSpawnChildFromBreeding(worldserver, entityanimal, (EntityAgeable) null);
        this.getBrain().setMemory(MemoryModuleType.IS_PREGNANT, Unit.INSTANCE);
    }

    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        FrogVariants.selectVariantToSpawn(this.random, this.registryAccess(), SpawnContext.create(worldaccess, this.blockPosition())).ifPresent(this::setVariant);
        FrogAi.initMemories(this, worldaccess.getRandom());
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityAnimal.createAnimalAttributes().add(GenericAttributes.MOVEMENT_SPEED, 1.0D).add(GenericAttributes.MAX_HEALTH, 10.0D).add(GenericAttributes.ATTACK_DAMAGE, 10.0D).add(GenericAttributes.STEP_HEIGHT, 1.0D);
    }

    @Nullable
    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.FROG_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.FROG_HURT;
    }

    @Nullable
    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.FROG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.FROG_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    @Override
    protected int calculateFallDamage(double d0, float f) {
        return super.calculateFallDamage(d0, f) - 5;
    }

    @Override
    public void travel(Vec3D vec3d) {
        if (this.isInWater()) {
            this.moveRelative(this.getSpeed(), vec3d);
            this.move(EnumMoveType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(vec3d);
        }

    }

    public static boolean canEat(EntityLiving entityliving) {
        if (entityliving instanceof EntitySlime entityslime) {
            if (entityslime.getSize() != 1) {
                return false;
            }
        }

        return entityliving.getType().is(TagsEntity.FROG_FOOD);
    }

    @Override
    protected NavigationAbstract createNavigation(World world) {
        return new Frog.c(this, world);
    }

    @Nullable
    @Override
    public EntityLiving getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.FROG_FOOD);
    }

    public static boolean checkFrogSpawnRules(EntityTypes<? extends EntityAnimal> entitytypes, GeneratorAccess generatoraccess, EntitySpawnReason entityspawnreason, BlockPosition blockposition, RandomSource randomsource) {
        return generatoraccess.getBlockState(blockposition.below()).is(TagsBlock.FROGS_SPAWNABLE_ON) && isBrightEnoughToSpawn(generatoraccess, blockposition);
    }

    private class a extends ControllerLook {

        a(final EntityInsentient entityinsentient) {
            super(entityinsentient);
        }

        @Override
        protected boolean resetXRotOnTick() {
            return Frog.this.getTongueTarget().isEmpty();
        }
    }

    private static class c extends AmphibiousPathNavigation {

        c(Frog frog, World world) {
            super(frog, world);
        }

        @Override
        public boolean canCutCorner(PathType pathtype) {
            return pathtype != PathType.WATER_BORDER && super.canCutCorner(pathtype);
        }

        @Override
        protected Pathfinder createPathFinder(int i) {
            this.nodeEvaluator = new Frog.b(true);
            return new Pathfinder(this.nodeEvaluator, i);
        }
    }

    private static class b extends AmphibiousNodeEvaluator {

        private final BlockPosition.MutableBlockPosition belowPos = new BlockPosition.MutableBlockPosition();

        public b(boolean flag) {
            super(flag);
        }

        @Override
        public PathPoint getStart() {
            return !this.mob.isInWater() ? super.getStart() : this.getStartNode(new BlockPosition(MathHelper.floor(this.mob.getBoundingBox().minX), MathHelper.floor(this.mob.getBoundingBox().minY), MathHelper.floor(this.mob.getBoundingBox().minZ)));
        }

        @Override
        public PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k) {
            this.belowPos.set(i, j - 1, k);
            IBlockData iblockdata = pathfindingcontext.getBlockState(this.belowPos);

            return iblockdata.is(TagsBlock.FROG_PREFER_JUMP_TO) ? PathType.OPEN : super.getPathType(pathfindingcontext, i, j, k);
        }
    }
}
