package net.minecraft.world.entity.ai.control;

import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;

public class SmoothSwimmingMoveControl extends ControllerMove {

    private static final float FULL_SPEED_TURN_THRESHOLD = 10.0F;
    private static final float STOP_TURN_THRESHOLD = 60.0F;
    private final int maxTurnX;
    private final int maxTurnY;
    private final float inWaterSpeedModifier;
    private final float outsideWaterSpeedModifier;
    private final boolean applyGravity;

    public SmoothSwimmingMoveControl(EntityInsentient entityinsentient, int i, int j, float f, float f1, boolean flag) {
        super(entityinsentient);
        this.maxTurnX = i;
        this.maxTurnY = j;
        this.inWaterSpeedModifier = f;
        this.outsideWaterSpeedModifier = f1;
        this.applyGravity = flag;
    }

    @Override
    public void tick() {
        if (this.applyGravity && this.mob.isInWater()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
        }

        if (this.operation == ControllerMove.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedY - this.mob.getY();
            double d2 = this.wantedZ - this.mob.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d3 < (double) 2.5000003E-7F) {
                this.mob.setZza(0.0F);
            } else {
                float f = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;

                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, (float) this.maxTurnY));
                this.mob.yBodyRot = this.mob.getYRot();
                this.mob.yHeadRot = this.mob.getYRot();
                float f1 = (float) (this.speedModifier * this.mob.getAttributeValue(GenericAttributes.MOVEMENT_SPEED));

                if (this.mob.isInWater()) {
                    this.mob.setSpeed(f1 * this.inWaterSpeedModifier);
                    double d4 = Math.sqrt(d0 * d0 + d2 * d2);

                    if (Math.abs(d1) > (double) 1.0E-5F || Math.abs(d4) > (double) 1.0E-5F) {
                        float f2 = -((float) (MathHelper.atan2(d1, d4) * (double) (180F / (float) Math.PI)));

                        f2 = MathHelper.clamp(MathHelper.wrapDegrees(f2), (float) (-this.maxTurnX), (float) this.maxTurnX);
                        this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), f2, 5.0F));
                    }

                    float f3 = MathHelper.cos(this.mob.getXRot() * ((float) Math.PI / 180F));
                    float f4 = MathHelper.sin(this.mob.getXRot() * ((float) Math.PI / 180F));

                    this.mob.zza = f3 * f1;
                    this.mob.yya = -f4 * f1;
                } else {
                    float f5 = Math.abs(MathHelper.wrapDegrees(this.mob.getYRot() - f));
                    float f6 = getTurningSpeedFactor(f5);

                    this.mob.setSpeed(f1 * this.outsideWaterSpeedModifier * f6);
                }

            }
        } else {
            this.mob.setSpeed(0.0F);
            this.mob.setXxa(0.0F);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }
    }

    private static float getTurningSpeedFactor(float f) {
        return 1.0F - MathHelper.clamp((f - 10.0F) / 50.0F, 0.0F, 1.0F);
    }
}
