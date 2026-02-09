package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EntitySpectralArrow extends EntityArrow {

    private static final int DEFAULT_DURATION = 200;
    public int duration = 200;

    public EntitySpectralArrow(EntityTypes<? extends EntitySpectralArrow> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntitySpectralArrow(World world, EntityLiving entityliving, ItemStack itemstack, @Nullable ItemStack itemstack1) {
        super(EntityTypes.SPECTRAL_ARROW, entityliving, world, itemstack, itemstack1);
    }

    public EntitySpectralArrow(World world, double d0, double d1, double d2, ItemStack itemstack, @Nullable ItemStack itemstack1) {
        super(EntityTypes.SPECTRAL_ARROW, d0, d1, d2, world, itemstack, itemstack1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && !this.isInGround()) {
            this.level().addParticle(SpellParticleOption.create(Particles.EFFECT, -1, 1.0F), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }

    }

    @Override
    protected void doPostHurtEffects(EntityLiving entityliving) {
        super.doPostHurtEffects(entityliving);
        MobEffect mobeffect = new MobEffect(MobEffects.GLOWING, this.duration, 0);

        entityliving.addEffect(mobeffect, this.getEffectSource());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {
        super.readAdditionalSaveData(valueinput);
        this.duration = valueinput.getIntOr("Duration", 200);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {
        super.addAdditionalSaveData(valueoutput);
        valueoutput.putInt("Duration", this.duration);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.SPECTRAL_ARROW);
    }
}
