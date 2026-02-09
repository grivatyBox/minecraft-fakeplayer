package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import org.bukkit.event.entity.EntityRemoveEvent;
// CraftBukkit end

public class EntityEnderSignal extends Entity implements ItemSupplier {

    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
    private static final float TOO_FAR_SIGNAL_HEIGHT = 8.0F;
    private static final float TOO_FAR_DISTANCE = 12.0F;
    private static final DataWatcherObject<ItemStack> DATA_ITEM_STACK = DataWatcher.<ItemStack>defineId(EntityEnderSignal.class, DataWatcherRegistry.ITEM_STACK);
    @Nullable
    public Vec3D target;
    public int life;
    public boolean surviveAfterDeath;

    public EntityEnderSignal(EntityTypes<? extends EntityEnderSignal> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityEnderSignal(World world, double d0, double d1, double d2) {
        this(EntityTypes.EYE_OF_ENDER, world);
        this.setPos(d0, d1, d2);
    }

    public void setItem(ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            this.getEntityData().set(EntityEnderSignal.DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(EntityEnderSignal.DATA_ITEM_STACK, itemstack.copyWithCount(1));
        }

    }

    @Override
    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(EntityEnderSignal.DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityEnderSignal.DATA_ITEM_STACK, this.getDefaultItem());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        if (this.tickCount < 2 && d0 < 12.25D) {
            return false;
        } else {
            double d1 = this.getBoundingBox().getSize() * 4.0D;

            if (Double.isNaN(d1)) {
                d1 = 4.0D;
            }

            d1 *= 64.0D;
            return d0 < d1 * d1;
        }
    }

    public void signalTo(Vec3D vec3d) {
        Vec3D vec3d1 = vec3d.subtract(this.position());
        double d0 = vec3d1.horizontalDistance();

        if (d0 > 12.0D) {
            this.target = this.position().add(vec3d1.x / d0 * 12.0D, 8.0D, vec3d1.z / d0 * 12.0D);
        } else {
            this.target = vec3d;
        }

        this.life = 0;
        this.surviveAfterDeath = this.random.nextInt(5) > 0;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3D vec3d = this.position().add(this.getDeltaMovement());

        if (!this.level().isClientSide() && this.target != null) {
            this.setDeltaMovement(updateDeltaMovement(this.getDeltaMovement(), vec3d, this.target));
        }

        if (this.level().isClientSide()) {
            Vec3D vec3d1 = vec3d.subtract(this.getDeltaMovement().scale(0.25D));

            this.spawnParticles(vec3d1, this.getDeltaMovement());
        }

        this.setPos(vec3d);
        if (!this.level().isClientSide()) {
            ++this.life;
            if (this.life > 80 && !this.level().isClientSide) {
                this.playSound(SoundEffects.ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.discard(this.surviveAfterDeath ? EntityRemoveEvent.Cause.DROP : EntityRemoveEvent.Cause.DESPAWN); // CraftBukkit - add Bukkit remove cause
                if (this.surviveAfterDeath) {
                    this.level().addFreshEntity(new EntityItem(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
                } else {
                    this.level().levelEvent(2003, this.blockPosition(), 0);
                }
            }
        }

    }

    private void spawnParticles(Vec3D vec3d, Vec3D vec3d1) {
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(Particles.BUBBLE, vec3d.x, vec3d.y, vec3d.z, vec3d1.x, vec3d1.y, vec3d1.z);
            }
        } else {
            this.level().addParticle(Particles.PORTAL, vec3d.x + this.random.nextDouble() * 0.6D - 0.3D, vec3d.y - 0.5D, vec3d.z + this.random.nextDouble() * 0.6D - 0.3D, vec3d1.x, vec3d1.y, vec3d1.z);
        }

    }

    private static Vec3D updateDeltaMovement(Vec3D vec3d, Vec3D vec3d1, Vec3D vec3d2) {
        Vec3D vec3d3 = new Vec3D(vec3d2.x - vec3d1.x, 0.0D, vec3d2.z - vec3d1.z);
        double d0 = vec3d3.length();
        double d1 = MathHelper.lerp(0.0025D, vec3d.horizontalDistance(), d0);
        double d2 = vec3d.y;

        if (d0 < 1.0D) {
            d1 *= 0.8D;
            d2 *= 0.8D;
        }

        double d3 = vec3d1.y - vec3d.y < vec3d2.y ? 1.0D : -1.0D;

        return vec3d3.scale(d1 / d0).add(0.0D, d2 + (d3 - d2) * 0.015D, 0.0D);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {
        valueoutput.store("Item", ItemStack.CODEC, this.getItem());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {
        this.setItem((ItemStack) valueinput.read("Item", ItemStack.CODEC).orElse(this.getDefaultItem()));
    }

    private ItemStack getDefaultItem() {
        return new ItemStack(Items.ENDER_EYE);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurtServer(WorldServer worldserver, DamageSource damagesource, float f) {
        return false;
    }
}
