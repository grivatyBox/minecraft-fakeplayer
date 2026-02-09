package net.minecraft.world.entity.ai.goal;

import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityPerchable;

public class PathfinderGoalPerch extends PathfinderGoal {

    private final EntityPerchable entity;
    private boolean isSittingOnShoulder;

    public PathfinderGoalPerch(EntityPerchable entityperchable) {
        this.entity = entityperchable;
    }

    @Override
    public boolean canUse() {
        EntityLiving entityliving = this.entity.getOwner();

        if (!(entityliving instanceof EntityPlayer entityplayer)) {
            return false;
        } else {
            boolean flag = !entityplayer.isSpectator() && !entityplayer.getAbilities().flying && !entityplayer.isInWater() && !entityplayer.isInPowderSnow;

            return !this.entity.isOrderedToSit() && flag && this.entity.canSitOnShoulder();
        }
    }

    @Override
    public boolean isInterruptable() {
        return !this.isSittingOnShoulder;
    }

    @Override
    public void start() {
        this.isSittingOnShoulder = false;
    }

    @Override
    public void tick() {
        if (!this.isSittingOnShoulder && !this.entity.isInSittingPose() && !this.entity.isLeashed()) {
            EntityLiving entityliving = this.entity.getOwner();

            if (entityliving instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityliving;

                if (this.entity.getBoundingBox().intersects(entityplayer.getBoundingBox())) {
                    this.isSittingOnShoulder = this.entity.setEntityOnShoulder(entityplayer);
                }
            }

        }
    }
}
