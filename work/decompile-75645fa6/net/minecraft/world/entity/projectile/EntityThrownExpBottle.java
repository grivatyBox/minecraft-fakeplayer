package net.minecraft.world.entity.projectile;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityExperienceOrb;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class EntityThrownExpBottle extends EntityProjectileThrowable {

    public EntityThrownExpBottle(EntityTypes<? extends EntityThrownExpBottle> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityThrownExpBottle(World world, EntityLiving entityliving, ItemStack itemstack) {
        super(EntityTypes.EXPERIENCE_BOTTLE, entityliving, world, itemstack);
    }

    public EntityThrownExpBottle(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(EntityTypes.EXPERIENCE_BOTTLE, d0, d1, d2, world, itemstack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07D;
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            worldserver.levelEvent(2002, this.blockPosition(), -13083194);
            int i = 3 + worldserver.random.nextInt(5) + worldserver.random.nextInt(5);

            if (movingobjectposition instanceof MovingObjectPositionBlock movingobjectpositionblock) {
                Vec3D vec3d = movingobjectpositionblock.getDirection().getUnitVec3();

                EntityExperienceOrb.awardWithDirection(worldserver, movingobjectposition.getLocation(), vec3d, i);
            } else {
                EntityExperienceOrb.awardWithDirection(worldserver, movingobjectposition.getLocation(), this.getDeltaMovement().scale(-1.0D), i);
            }

            this.discard();
        }

    }
}
