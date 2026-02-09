package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityLargeFireball;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class EntityGhast extends EntityInsentient implements IMonster {

    private static final DataWatcherObject<Boolean> DATA_IS_CHARGING = DataWatcher.<Boolean>defineId(EntityGhast.class, DataWatcherRegistry.BOOLEAN);
    private static final byte DEFAULT_EXPLOSION_POWER = 1;
    private int explosionPower = 1;

    public EntityGhast(EntityTypes<? extends EntityGhast> entitytypes, World world) {
        super(entitytypes, world);
        this.xpReward = 5;
        this.moveControl = new EntityGhast.ControllerGhast(this, false, () -> {
            return false;
        });
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new EntityGhast.PathfinderGoalGhastIdleMove(this));
        this.goalSelector.addGoal(7, new EntityGhast.PathfinderGoalGhastMoveTowardsTarget(this));
        this.goalSelector.addGoal(7, new EntityGhast.PathfinderGoalGhastAttackTarget(this));
        this.targetSelector.addGoal(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 10, true, false, (entityliving, worldserver) -> {
            return Math.abs(entityliving.getY() - this.getY()) <= 4.0D;
        }));
    }

    public boolean isCharging() {
        return (Boolean) this.entityData.get(EntityGhast.DATA_IS_CHARGING);
    }

    public void setCharging(boolean flag) {
        this.entityData.set(EntityGhast.DATA_IS_CHARGING, flag);
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    private static boolean isReflectedFireball(DamageSource damagesource) {
        return damagesource.getDirectEntity() instanceof EntityLargeFireball && damagesource.getEntity() instanceof EntityHuman;
    }

    @Override
    public boolean isInvulnerableTo(WorldServer worldserver, DamageSource damagesource) {
        return this.isInvulnerable() && !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !isReflectedFireball(damagesource) && super.isInvulnerableTo(worldserver, damagesource);
    }

    @Override
    protected void checkFallDamage(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {}

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3D vec3d) {
        this.travelFlying(vec3d, 0.02F);
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (isReflectedFireball(damagesource)) {
            super.hurtServer(worldserver, damagesource, 1000.0F);
            return true;
        } else {
            return this.isInvulnerableTo(worldserver, damagesource) ? false : super.hurtServer(worldserver, damagesource, f);
        }
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityGhast.DATA_IS_CHARGING, false);
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MAX_HEALTH, 10.0D).add(GenericAttributes.FOLLOW_RANGE, 100.0D).add(GenericAttributes.CAMERA_DISTANCE, 8.0D).add(GenericAttributes.FLYING_SPEED, 0.06D);
    }

    @Override
    public SoundCategory getSoundSource() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.GHAST_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.GHAST_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    public static boolean checkGhastSpawnRules(EntityTypes<EntityGhast> entitytypes, GeneratorAccess generatoraccess, EntitySpawnReason entityspawnreason, BlockPosition blockposition, RandomSource randomsource) {
        return generatoraccess.getDifficulty() != EnumDifficulty.PEACEFUL && randomsource.nextInt(20) == 0 && checkMobSpawnRules(entitytypes, generatoraccess, entityspawnreason, blockposition, randomsource);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {
        super.addAdditionalSaveData(valueoutput);
        valueoutput.putByte("ExplosionPower", (byte) this.explosionPower);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {
        super.readAdditionalSaveData(valueinput);
        this.explosionPower = valueinput.getByteOr("ExplosionPower", (byte) 1);
    }

    @Override
    public boolean supportQuadLeashAsHolder() {
        return true;
    }

    @Override
    public double leashElasticDistance() {
        return 10.0D;
    }

    @Override
    public double leashSnapDistance() {
        return 16.0D;
    }

    public static void faceMovementDirection(EntityInsentient entityinsentient) {
        if (entityinsentient.getTarget() == null) {
            Vec3D vec3d = entityinsentient.getDeltaMovement();

            entityinsentient.setYRot(-((float) MathHelper.atan2(vec3d.x, vec3d.z)) * (180F / (float) Math.PI));
            entityinsentient.yBodyRot = entityinsentient.getYRot();
        } else {
            EntityLiving entityliving = entityinsentient.getTarget();
            double d0 = 64.0D;

            if (entityliving.distanceToSqr((Entity) entityinsentient) < 4096.0D) {
                double d1 = entityliving.getX() - entityinsentient.getX();
                double d2 = entityliving.getZ() - entityinsentient.getZ();

                entityinsentient.setYRot(-((float) MathHelper.atan2(d1, d2)) * (180F / (float) Math.PI));
                entityinsentient.yBodyRot = entityinsentient.getYRot();
            }
        }

    }

    public static class ControllerGhast extends ControllerMove {

        private final EntityInsentient ghast;
        private int floatDuration;
        private final boolean careful;
        private final BooleanSupplier shouldBeStopped;

        public ControllerGhast(EntityInsentient entityinsentient, boolean flag, BooleanSupplier booleansupplier) {
            super(entityinsentient);
            this.ghast = entityinsentient;
            this.careful = flag;
            this.shouldBeStopped = booleansupplier;
        }

        @Override
        public void tick() {
            if (this.shouldBeStopped.getAsBoolean()) {
                this.operation = ControllerMove.Operation.WAIT;
                this.ghast.stopInPlace();
            }

            if (this.operation == ControllerMove.Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
                    Vec3D vec3d = new Vec3D(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());

                    if (this.canReach(vec3d)) {
                        this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(vec3d.normalize().scale(this.ghast.getAttributeValue(GenericAttributes.FLYING_SPEED) * 5.0D / 3.0D)));
                    } else {
                        this.operation = ControllerMove.Operation.WAIT;
                    }
                }

            }
        }

        private boolean canReach(Vec3D vec3d) {
            AxisAlignedBB axisalignedbb = this.ghast.getBoundingBox();
            AxisAlignedBB axisalignedbb1 = axisalignedbb.move(vec3d);

            if (this.careful) {
                for (BlockPosition blockposition : BlockPosition.betweenClosed(axisalignedbb1.inflate(1.0D))) {
                    if (!this.blockTraversalPossible(this.ghast.level(), (Vec3D) null, (Vec3D) null, blockposition, false, false)) {
                        return false;
                    }
                }
            }

            boolean flag = this.ghast.isInWater();
            boolean flag1 = this.ghast.isInLava();
            Vec3D vec3d1 = this.ghast.position();
            Vec3D vec3d2 = vec3d1.add(vec3d);

            return IBlockAccess.forEachBlockIntersectedBetween(vec3d1, vec3d2, axisalignedbb1, (blockposition1, i) -> {
                return axisalignedbb.intersects(blockposition1) ? true : this.blockTraversalPossible(this.ghast.level(), vec3d1, vec3d2, blockposition1, flag, flag1);
            });
        }

        private boolean blockTraversalPossible(IBlockAccess iblockaccess, @Nullable Vec3D vec3d, @Nullable Vec3D vec3d1, BlockPosition blockposition, boolean flag, boolean flag1) {
            IBlockData iblockdata = iblockaccess.getBlockState(blockposition);

            if (iblockdata.isAir()) {
                return true;
            } else {
                boolean flag2 = vec3d != null && vec3d1 != null;
                boolean flag3 = flag2 ? !this.ghast.collidedWithShapeMovingFrom(vec3d, vec3d1, iblockdata.getCollisionShape(iblockaccess, blockposition).move(new Vec3D(blockposition)).toAabbs()) : iblockdata.getCollisionShape(iblockaccess, blockposition).isEmpty();

                if (!this.careful) {
                    return flag3;
                } else if (iblockdata.is(TagsBlock.HAPPY_GHAST_AVOIDS)) {
                    return false;
                } else {
                    Fluid fluid = iblockaccess.getFluidState(blockposition);

                    if (!fluid.isEmpty() && (!flag2 || this.ghast.collidedWithFluid(fluid, blockposition, vec3d, vec3d1))) {
                        if (fluid.is(TagsFluid.WATER)) {
                            return flag;
                        }

                        if (fluid.is(TagsFluid.LAVA)) {
                            return flag1;
                        }
                    }

                    return flag3;
                }
            }
        }
    }

    public static class PathfinderGoalGhastIdleMove extends PathfinderGoal {

        private static final int MAX_ATTEMPTS = 64;
        private final EntityInsentient ghast;
        private final int distanceToBlocks;

        public PathfinderGoalGhastIdleMove(EntityInsentient entityinsentient) {
            this(entityinsentient, 0);
        }

        public PathfinderGoalGhastIdleMove(EntityInsentient entityinsentient, int i) {
            this.ghast = entityinsentient;
            this.distanceToBlocks = i;
            this.setFlags(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean canUse() {
            ControllerMove controllermove = this.ghast.getMoveControl();

            if (!controllermove.hasWanted()) {
                return true;
            } else {
                double d0 = controllermove.getWantedX() - this.ghast.getX();
                double d1 = controllermove.getWantedY() - this.ghast.getY();
                double d2 = controllermove.getWantedZ() - this.ghast.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Vec3D vec3d = getSuitableFlyToPosition(this.ghast, this.distanceToBlocks);

            this.ghast.getMoveControl().setWantedPosition(vec3d.x(), vec3d.y(), vec3d.z(), 1.0D);
        }

        public static Vec3D getSuitableFlyToPosition(EntityInsentient entityinsentient, int i) {
            World world = entityinsentient.level();
            RandomSource randomsource = entityinsentient.getRandom();
            Vec3D vec3d = entityinsentient.position();
            Vec3D vec3d1 = null;

            for (int j = 0; j < 64; ++j) {
                vec3d1 = chooseRandomPositionWithRestriction(entityinsentient, vec3d, randomsource);
                if (vec3d1 != null && isGoodTarget(world, vec3d1, i)) {
                    return vec3d1;
                }
            }

            if (vec3d1 == null) {
                vec3d1 = chooseRandomPosition(vec3d, randomsource);
            }

            BlockPosition blockposition = BlockPosition.containing(vec3d1);
            int k = world.getHeight(HeightMap.Type.MOTION_BLOCKING, blockposition.getX(), blockposition.getZ());

            if (k < blockposition.getY() && k > world.getMinY()) {
                vec3d1 = new Vec3D(vec3d1.x(), entityinsentient.getY() - Math.abs(entityinsentient.getY() - vec3d1.y()), vec3d1.z());
            }

            return vec3d1;
        }

        private static boolean isGoodTarget(World world, Vec3D vec3d, int i) {
            if (i <= 0) {
                return true;
            } else {
                BlockPosition blockposition = BlockPosition.containing(vec3d);

                if (!world.getBlockState(blockposition).isAir()) {
                    return false;
                } else {
                    for (EnumDirection enumdirection : EnumDirection.values()) {
                        for (int j = 1; j < i; ++j) {
                            BlockPosition blockposition1 = blockposition.relative(enumdirection, j);

                            if (!world.getBlockState(blockposition1).isAir()) {
                                return true;
                            }
                        }
                    }

                    return false;
                }
            }
        }

        private static Vec3D chooseRandomPosition(Vec3D vec3d, RandomSource randomsource) {
            double d0 = vec3d.x() + (double) ((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = vec3d.y() + (double) ((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = vec3d.z() + (double) ((randomsource.nextFloat() * 2.0F - 1.0F) * 16.0F);

            return new Vec3D(d0, d1, d2);
        }

        @Nullable
        private static Vec3D chooseRandomPositionWithRestriction(EntityInsentient entityinsentient, Vec3D vec3d, RandomSource randomsource) {
            Vec3D vec3d1 = chooseRandomPosition(vec3d, randomsource);

            return entityinsentient.hasHome() && !entityinsentient.isWithinHome(vec3d1) ? null : vec3d1;
        }
    }

    public static class PathfinderGoalGhastMoveTowardsTarget extends PathfinderGoal {

        private final EntityInsentient ghast;

        public PathfinderGoalGhastMoveTowardsTarget(EntityInsentient entityinsentient) {
            this.ghast = entityinsentient;
            this.setFlags(EnumSet.of(PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            EntityGhast.faceMovementDirection(this.ghast);
        }
    }

    private static class PathfinderGoalGhastAttackTarget extends PathfinderGoal {

        private final EntityGhast ghast;
        public int chargeTime;

        public PathfinderGoalGhastAttackTarget(EntityGhast entityghast) {
            this.ghast = entityghast;
        }

        @Override
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
        }

        @Override
        public void stop() {
            this.ghast.setCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            EntityLiving entityliving = this.ghast.getTarget();

            if (entityliving != null) {
                double d0 = 64.0D;

                if (entityliving.distanceToSqr((Entity) this.ghast) < 4096.0D && this.ghast.hasLineOfSight(entityliving)) {
                    World world = this.ghast.level();

                    ++this.chargeTime;
                    if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                        world.levelEvent((Entity) null, 1015, this.ghast.blockPosition(), 0);
                    }

                    if (this.chargeTime == 20) {
                        double d1 = 4.0D;
                        Vec3D vec3d = this.ghast.getViewVector(1.0F);
                        double d2 = entityliving.getX() - (this.ghast.getX() + vec3d.x * 4.0D);
                        double d3 = entityliving.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
                        double d4 = entityliving.getZ() - (this.ghast.getZ() + vec3d.z * 4.0D);
                        Vec3D vec3d1 = new Vec3D(d2, d3, d4);

                        if (!this.ghast.isSilent()) {
                            world.levelEvent((Entity) null, 1016, this.ghast.blockPosition(), 0);
                        }

                        EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this.ghast, vec3d1.normalize(), this.ghast.getExplosionPower());

                        entitylargefireball.setPos(this.ghast.getX() + vec3d.x * 4.0D, this.ghast.getY(0.5D) + 0.5D, entitylargefireball.getZ() + vec3d.z * 4.0D);
                        world.addFreshEntity(entitylargefireball);
                        this.chargeTime = -40;
                    }
                } else if (this.chargeTime > 0) {
                    --this.chargeTime;
                }

                this.ghast.setCharging(this.chargeTime > 10);
            }
        }
    }
}
