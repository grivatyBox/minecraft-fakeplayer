package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;

public class WindCharge extends EntityFireball implements ItemSupplier {

    public static final WindCharge.a EXPLOSION_DAMAGE_CALCULATOR = new WindCharge.a();

    public WindCharge(EntityTypes<? extends WindCharge> entitytypes, World world) {
        super(entitytypes, world);
    }

    public WindCharge(EntityTypes<? extends WindCharge> entitytypes, Breeze breeze, World world) {
        super(entitytypes, breeze.getX(), breeze.getSnoutYPosition(), breeze.getZ(), world);
        this.setOwner(breeze);
    }

    @Override
    protected AxisAlignedBB makeBoundingBox() {
        float f = this.getType().getDimensions().width / 2.0F;
        float f1 = this.getType().getDimensions().height;
        float f2 = 0.15F;

        return new AxisAlignedBB(this.position().x - (double) f, this.position().y - 0.15000000596046448D, this.position().z - (double) f, this.position().x + (double) f, this.position().y - 0.15000000596046448D + (double) f1, this.position().z + (double) f);
    }

    @Override
    protected float getEyeHeight(EntityPose entitypose, EntitySize entitysize) {
        return 0.0F;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return entity instanceof WindCharge ? false : super.canCollideWith(entity);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof WindCharge ? false : super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        if (!this.level().isClientSide) {
            Entity entity = movingobjectpositionentity.getEntity();
            DamageSources damagesources = this.damageSources();
            Entity entity1 = this.getOwner();
            EntityLiving entityliving;

            if (entity1 instanceof EntityLiving) {
                EntityLiving entityliving1 = (EntityLiving) entity1;

                entityliving = entityliving1;
            } else {
                entityliving = null;
            }

            entity.hurt(damagesources.mobProjectile(this, entityliving), 1.0F);
            this.explode();
        }
    }

    private void explode() {
        this.level().explode(this, (DamageSource) null, WindCharge.EXPLOSION_DAMAGE_CALCULATOR, this.getX(), this.getY(), this.getZ(), (float) (3.0D + this.random.nextDouble()), false, World.a.BLOW, Particles.GUST, Particles.GUST_EMITTER, SoundEffects.WIND_BURST);
    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        this.explode();
        this.discard();
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            this.discard();
        }

    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected float getLiquidInertia() {
        return this.getInertia();
    }

    @Nullable
    @Override
    protected ParticleParam getTrailParticle() {
        return null;
    }

    @Override
    protected RayTrace.BlockCollisionOption getClipType() {
        return RayTrace.BlockCollisionOption.OUTLINE;
    }

    public static final class a extends ExplosionDamageCalculator {

        public a() {}

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }
    }
}
