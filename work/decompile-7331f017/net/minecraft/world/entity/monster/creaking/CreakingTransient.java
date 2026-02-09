package net.minecraft.world.entity.monster.creaking;

import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Pathfinder;
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class CreakingTransient extends Creaking {

    public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
    private int invulnerabilityAnimationRemainingTicks;
    @Nullable
    BlockPosition homePos;

    public CreakingTransient(EntityTypes<? extends Creaking> entitytypes, World world) {
        super(entitytypes, world);
    }

    public void bindToCreakingHeart(BlockPosition blockposition) {
        this.homePos = blockposition;
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        if (this.level().isClientSide) {
            return super.hurtServer(worldserver, damagesource, f);
        } else if (damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(worldserver, damagesource, f);
        } else if (!this.isInvulnerableTo(worldserver, damagesource) && this.invulnerabilityAnimationRemainingTicks <= 0) {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.level().broadcastEntityEvent(this, (byte) 66);
            TileEntity tileentity = this.level().getBlockEntity(this.homePos);

            if (tileentity instanceof CreakingHeartBlockEntity) {
                CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) tileentity;

                if (creakingheartblockentity.isProtector(this)) {
                    if (damagesource.getEntity() instanceof EntityHuman) {
                        creakingheartblockentity.creakingHurt();
                    }

                    this.playHurtSound(damagesource);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void aiStep() {
        if (this.invulnerabilityAnimationRemainingTicks > 0) {
            --this.invulnerabilityAnimationRemainingTicks;
        }

        super.aiStep();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            label18:
            {
                if (this.homePos != null) {
                    TileEntity tileentity = this.level().getBlockEntity(this.homePos);

                    if (tileentity instanceof CreakingHeartBlockEntity) {
                        CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) tileentity;

                        if (creakingheartblockentity.isProtector(this)) {
                            break label18;
                        }
                    }
                }

                this.setRemoved(Entity.RemovalReason.DISCARDED);
                return;
            }
        }

        super.tick();
        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }

    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 66) {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.playHurtSound(this.damageSources().generic());
        } else {
            super.handleEntityEvent(b0);
        }

    }

    private void setupAnimationStates() {
        this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
    }

    public void tearDown(@Nullable DamageSource damagesource) {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            AxisAlignedBB axisalignedbb = this.getBoundingBox();
            Vec3D vec3d = axisalignedbb.getCenter();
            double d0 = axisalignedbb.getXsize() * 0.3D;
            double d1 = axisalignedbb.getYsize() * 0.3D;
            double d2 = axisalignedbb.getZsize() * 0.3D;

            worldserver.sendParticles(new ParticleParamBlock(Particles.BLOCK_CRUMBLE, Blocks.PALE_OAK_WOOD.defaultBlockState()), vec3d.x, vec3d.y, vec3d.z, 100, d0, d1, d2, 0.0D);
            worldserver.sendParticles(new ParticleParamBlock(Particles.BLOCK_CRUMBLE, (IBlockData) Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.a.ACTIVE)), vec3d.x, vec3d.y, vec3d.z, 10, d0, d1, d2, 0.0D);
        }

        this.makeSound(this.getDeathSound());
        if (this.deathScore >= 0 && damagesource != null) {
            Entity entity = damagesource.getEntity();

            if (entity instanceof EntityLiving) {
                EntityLiving entityliving = (EntityLiving) entity;

                entityliving.awardKillScore(this, this.deathScore, damagesource);
            }
        }

        this.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity entity) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public boolean canUsePortal(boolean flag) {
        return false;
    }

    @Override
    protected NavigationAbstract createNavigation(World world) {
        return new CreakingTransient.a(this, world);
    }

    private class a extends Navigation {

        a(final Creaking creaking, final World world) {
            super(creaking, world);
        }

        @Override
        public void tick() {
            if (CreakingTransient.this.canMove()) {
                super.tick();
            }

        }

        @Override
        protected Pathfinder createPathFinder(int i) {
            this.nodeEvaluator = CreakingTransient.this.new b();
            return new Pathfinder(this.nodeEvaluator, i);
        }
    }

    private class b extends PathfinderNormal {

        private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

        b() {}

        @Override
        public PathType getPathType(PathfindingContext pathfindingcontext, int i, int j, int k) {
            BlockPosition blockposition = CreakingTransient.this.homePos;

            if (blockposition == null) {
                return super.getPathType(pathfindingcontext, i, j, k);
            } else {
                double d0 = blockposition.distSqr(new BaseBlockPosition(i, j, k));

                return d0 > 1024.0D && d0 >= blockposition.distSqr(pathfindingcontext.mobPosition()) ? PathType.BLOCKED : super.getPathType(pathfindingcontext, i, j, k);
            }
        }
    }
}
