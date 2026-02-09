package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.animal.EntityDolphin;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3D;

public class PathfinderGoalWaterJump extends PathfinderGoalWaterJumpAbstract {

    private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
    private final EntityDolphin dolphin;
    private final int interval;
    private boolean breached;

    public PathfinderGoalWaterJump(EntityDolphin entitydolphin, int i) {
        this.dolphin = entitydolphin;
        this.interval = reducedTickDelay(i);
    }

    @Override
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        } else {
            EnumDirection enumdirection = this.dolphin.getMotionDirection();
            int i = enumdirection.getStepX();
            int j = enumdirection.getStepZ();
            BlockPosition blockposition = this.dolphin.blockPosition();

            for (int k : PathfinderGoalWaterJump.STEPS_TO_CHECK) {
                if (!this.waterIsClear(blockposition, i, j, k) || !this.surfaceIsClear(blockposition, i, j, k)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean waterIsClear(BlockPosition blockposition, int i, int j, int k) {
        BlockPosition blockposition1 = blockposition.offset(i * k, 0, j * k);

        return this.dolphin.level().getFluidState(blockposition1).is(TagsFluid.WATER) && !this.dolphin.level().getBlockState(blockposition1).blocksMotion();
    }

    private boolean surfaceIsClear(BlockPosition blockposition, int i, int j, int k) {
        return this.dolphin.level().getBlockState(blockposition.offset(i * k, 1, j * k)).isAir() && this.dolphin.level().getBlockState(blockposition.offset(i * k, 2, j * k)).isAir();
    }

    @Override
    public boolean canContinueToUse() {
        double d0 = this.dolphin.getDeltaMovement().y;

        return (d0 * d0 >= (double) 0.03F || this.dolphin.getXRot() == 0.0F || Math.abs(this.dolphin.getXRot()) >= 10.0F || !this.dolphin.isInWater()) && !this.dolphin.onGround();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        EnumDirection enumdirection = this.dolphin.getMotionDirection();

        this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add((double) enumdirection.getStepX() * 0.6D, 0.7D, (double) enumdirection.getStepZ() * 0.6D));
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.dolphin.setXRot(0.0F);
    }

    @Override
    public void tick() {
        boolean flag = this.breached;

        if (!flag) {
            Fluid fluid = this.dolphin.level().getFluidState(this.dolphin.blockPosition());

            this.breached = fluid.is(TagsFluid.WATER);
        }

        if (this.breached && !flag) {
            this.dolphin.playSound(SoundEffects.DOLPHIN_JUMP, 1.0F, 1.0F);
        }

        Vec3D vec3d = this.dolphin.getDeltaMovement();

        if (vec3d.y * vec3d.y < (double) 0.03F && this.dolphin.getXRot() != 0.0F) {
            this.dolphin.setXRot(MathHelper.rotLerp(0.2F, this.dolphin.getXRot(), 0.0F));
        } else if (vec3d.length() > (double) 1.0E-5F) {
            double d0 = vec3d.horizontalDistance();
            double d1 = Math.atan2(-vec3d.y, d0) * (double) (180F / (float) Math.PI);

            this.dolphin.setXRot((float) d1);
        }

    }
}
