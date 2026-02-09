package net.minecraft.world.entity;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.World;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class EntityExperienceOrb extends Entity {

    protected static final DataWatcherObject<Integer> DATA_VALUE = DataWatcher.<Integer>defineId(EntityExperienceOrb.class, DataWatcherRegistry.INT);
    private static final int LIFETIME = 6000;
    private static final int ENTITY_SCAN_PERIOD = 20;
    private static final int MAX_FOLLOW_DIST = 8;
    private static final int ORB_GROUPS_PER_AREA = 40;
    private static final double ORB_MERGE_DISTANCE = 0.5D;
    private static final short DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_VALUE = 0;
    private static final int DEFAULT_COUNT = 1;
    private int age;
    private int health;
    private int count;
    @Nullable
    private EntityHuman followingPlayer;
    private final InterpolationHandler interpolation;

    public EntityExperienceOrb(World world, double d0, double d1, double d2, int i) {
        this(EntityTypes.EXPERIENCE_ORB, world);
        this.setPos(d0, d1, d2);
        if (!this.level().isClientSide) {
            this.setYRot((float) (this.random.nextDouble() * 360.0D));
            this.setDeltaMovement((this.random.nextDouble() * (double) 0.2F - (double) 0.1F) * 2.0D, this.random.nextDouble() * 0.2D * 2.0D, (this.random.nextDouble() * (double) 0.2F - (double) 0.1F) * 2.0D);
        }

        this.setValue(i);
    }

    public EntityExperienceOrb(EntityTypes<? extends EntityExperienceOrb> entitytypes, World world) {
        super(entitytypes, world);
        this.age = 0;
        this.health = 5;
        this.count = 1;
        this.interpolation = new InterpolationHandler(this);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityExperienceOrb.DATA_VALUE, 0);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }

    @Override
    public void tick() {
        this.interpolation.interpolate();
        if (this.firstTick && this.level().isClientSide) {
            this.firstTick = false;
        } else {
            super.tick();
            boolean flag = !this.level().noCollision(this.getBoundingBox());

            if (this.isEyeInFluid(TagsFluid.WATER)) {
                this.setUnderwaterMovement();
            } else if (!flag) {
                this.applyGravity();
            }

            if (this.level().getFluidState(this.blockPosition()).is(TagsFluid.LAVA)) {
                this.setDeltaMovement((double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F), (double) 0.2F, (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F));
            }

            if (this.tickCount % 20 == 1) {
                this.scanForMerges();
            }

            this.followNearbyPlayer();
            if (this.followingPlayer == null && !this.level().isClientSide && flag) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
                this.hasImpulse = true;
            }

            double d0 = this.getDeltaMovement().y;

            this.move(EnumMoveType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            float f = 0.98F;

            if (this.onGround()) {
                f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().scale((double) f));
            if (this.verticalCollisionBelow && d0 < -this.getGravity()) {
                this.setDeltaMovement(new Vec3D(this.getDeltaMovement().x, -d0 * 0.4D, this.getDeltaMovement().z));
            }

            ++this.age;
            if (this.age >= 6000) {
                this.discard();
            }

        }
    }

    private void followNearbyPlayer() {
        if (this.followingPlayer == null || this.followingPlayer.isSpectator() || this.followingPlayer.distanceToSqr((Entity) this) > 64.0D) {
            EntityHuman entityhuman = this.level().getNearestPlayer(this, 8.0D);

            if (entityhuman != null && !entityhuman.isSpectator() && !entityhuman.isDeadOrDying()) {
                this.followingPlayer = entityhuman;
            } else {
                this.followingPlayer = null;
            }
        }

        if (this.followingPlayer != null) {
            Vec3D vec3d = new Vec3D(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() / 2.0D - this.getY(), this.followingPlayer.getZ() - this.getZ());
            double d0 = vec3d.lengthSqr();
            double d1 = 1.0D - Math.sqrt(d0) / 8.0D;

            this.setDeltaMovement(this.getDeltaMovement().add(vec3d.normalize().scale(d1 * d1 * 0.1D)));
        }

    }

    @Override
    public BlockPosition getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }

    private void scanForMerges() {
        if (this.level() instanceof WorldServer) {
            for (EntityExperienceOrb entityexperienceorb : this.level().getEntities(EntityTypeTest.forClass(EntityExperienceOrb.class), this.getBoundingBox().inflate(0.5D), this::canMerge)) {
                this.merge(entityexperienceorb);
            }
        }

    }

    public static void award(WorldServer worldserver, Vec3D vec3d, int i) {
        while (i > 0) {
            int j = getExperienceValue(i);

            i -= j;
            if (!tryMergeToExisting(worldserver, vec3d, j)) {
                worldserver.addFreshEntity(new EntityExperienceOrb(worldserver, vec3d.x(), vec3d.y(), vec3d.z(), j));
            }
        }

    }

    private static boolean tryMergeToExisting(WorldServer worldserver, Vec3D vec3d, int i) {
        AxisAlignedBB axisalignedbb = AxisAlignedBB.ofSize(vec3d, 1.0D, 1.0D, 1.0D);
        int j = worldserver.getRandom().nextInt(40);
        List<EntityExperienceOrb> list = worldserver.getEntities(EntityTypeTest.forClass(EntityExperienceOrb.class), axisalignedbb, (entityexperienceorb) -> {
            return canMerge(entityexperienceorb, j, i);
        });

        if (!list.isEmpty()) {
            EntityExperienceOrb entityexperienceorb = (EntityExperienceOrb) list.get(0);

            ++entityexperienceorb.count;
            entityexperienceorb.age = 0;
            return true;
        } else {
            return false;
        }
    }

    private boolean canMerge(EntityExperienceOrb entityexperienceorb) {
        return entityexperienceorb != this && canMerge(entityexperienceorb, this.getId(), this.getValue());
    }

    private static boolean canMerge(EntityExperienceOrb entityexperienceorb, int i, int j) {
        return !entityexperienceorb.isRemoved() && (entityexperienceorb.getId() - i) % 40 == 0 && entityexperienceorb.getValue() == j;
    }

    private void merge(EntityExperienceOrb entityexperienceorb) {
        this.count += entityexperienceorb.count;
        this.age = Math.min(this.age, entityexperienceorb.age);
        entityexperienceorb.discard();
    }

    private void setUnderwaterMovement() {
        Vec3D vec3d = this.getDeltaMovement();

        this.setDeltaMovement(vec3d.x * (double) 0.99F, Math.min(vec3d.y + (double) 5.0E-4F, (double) 0.06F), vec3d.z * (double) 0.99F);
    }

    @Override
    protected void doWaterSplashEffect() {}

    @Override
    public final boolean hurtClient(DamageSource damagesource) {
        return !this.isInvulnerableToBase(damagesource);
    }

    @Override
    public final boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (this.isInvulnerableToBase(damagesource)) {
            return false;
        } else {
            this.markHurt();
            this.health = (int) ((float) this.health - f);
            if (this.health <= 0) {
                this.discard();
            }

            return true;
        }
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.putShort("Health", (short) this.health);
        nbttagcompound.putShort("Age", (short) this.age);
        nbttagcompound.putShort("Value", (short) this.getValue());
        nbttagcompound.putInt("Count", this.count);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.health = nbttagcompound.getShortOr("Health", (short) 5);
        this.age = nbttagcompound.getShortOr("Age", (short) 0);
        this.setValue(nbttagcompound.getShortOr("Value", (short) 0));
        this.count = (Integer) nbttagcompound.read("Count", ExtraCodecs.POSITIVE_INT).orElse(1);
    }

    @Override
    public void playerTouch(EntityHuman entityhuman) {
        if (entityhuman instanceof EntityPlayer entityplayer) {
            if (entityhuman.takeXpDelay == 0) {
                entityhuman.takeXpDelay = 2;
                entityhuman.take(this, 1);
                int i = this.repairPlayerItems(entityplayer, this.getValue());

                if (i > 0) {
                    entityhuman.giveExperiencePoints(i);
                }

                --this.count;
                if (this.count == 0) {
                    this.discard();
                }
            }

        }
    }

    private int repairPlayerItems(EntityPlayer entityplayer, int i) {
        Optional<EnchantedItemInUse> optional = EnchantmentManager.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, entityplayer, ItemStack::isDamaged);

        if (optional.isPresent()) {
            ItemStack itemstack = ((EnchantedItemInUse) optional.get()).itemStack();
            int j = EnchantmentManager.modifyDurabilityToRepairFromXp(entityplayer.serverLevel(), itemstack, i);
            int k = Math.min(j, itemstack.getDamageValue());

            itemstack.setDamageValue(itemstack.getDamageValue() - k);
            if (k > 0) {
                int l = i - k * i / j;

                if (l > 0) {
                    return this.repairPlayerItems(entityplayer, l);
                }
            }

            return 0;
        } else {
            return i;
        }
    }

    public int getValue() {
        return (Integer) this.entityData.get(EntityExperienceOrb.DATA_VALUE);
    }

    public void setValue(int i) {
        this.entityData.set(EntityExperienceOrb.DATA_VALUE, i);
    }

    public int getIcon() {
        int i = this.getValue();

        return i >= 2477 ? 10 : (i >= 1237 ? 9 : (i >= 617 ? 8 : (i >= 307 ? 7 : (i >= 149 ? 6 : (i >= 73 ? 5 : (i >= 37 ? 4 : (i >= 17 ? 3 : (i >= 7 ? 2 : (i >= 3 ? 1 : 0)))))))));
    }

    public static int getExperienceValue(int i) {
        return i >= 2477 ? 2477 : (i >= 1237 ? 1237 : (i >= 617 ? 617 : (i >= 307 ? 307 : (i >= 149 ? 149 : (i >= 73 ? 73 : (i >= 37 ? 37 : (i >= 17 ? 17 : (i >= 7 ? 7 : (i >= 3 ? 3 : 1)))))))));
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundCategory getSoundSource() {
        return SoundCategory.AMBIENT;
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }
}
