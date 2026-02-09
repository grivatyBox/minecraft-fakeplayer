package net.minecraft.world.entity;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;

public class InterpolationHandler {

    public static final int DEFAULT_INTERPOLATION_STEPS = 3;
    private final Entity entity;
    private int interpolationSteps;
    private final InterpolationHandler.a interpolationData;
    @Nullable
    private Vec3D previousTickPosition;
    @Nullable
    private Vec2F previousTickRot;
    @Nullable
    private final Consumer<InterpolationHandler> onInterpolationStart;

    public InterpolationHandler(Entity entity) {
        this(entity, 3, (Consumer) null);
    }

    public InterpolationHandler(Entity entity, int i) {
        this(entity, i, (Consumer) null);
    }

    public InterpolationHandler(Entity entity, @Nullable Consumer<InterpolationHandler> consumer) {
        this(entity, 3, consumer);
    }

    public InterpolationHandler(Entity entity, int i, @Nullable Consumer<InterpolationHandler> consumer) {
        this.interpolationData = new InterpolationHandler.a(0, Vec3D.ZERO, 0.0F, 0.0F);
        this.interpolationSteps = i;
        this.entity = entity;
        this.onInterpolationStart = consumer;
    }

    public Vec3D position() {
        return this.interpolationData.steps > 0 ? this.interpolationData.position : this.entity.position();
    }

    public float yRot() {
        return this.interpolationData.steps > 0 ? this.interpolationData.yRot : this.entity.getYRot();
    }

    public float xRot() {
        return this.interpolationData.steps > 0 ? this.interpolationData.xRot : this.entity.getXRot();
    }

    public void interpolateTo(Vec3D vec3d, float f, float f1) {
        if (this.interpolationSteps == 0) {
            this.entity.snapTo(vec3d, f, f1);
            this.cancel();
        } else {
            this.interpolationData.steps = this.interpolationSteps;
            this.interpolationData.position = vec3d;
            this.interpolationData.yRot = f;
            this.interpolationData.xRot = f1;
            this.previousTickPosition = this.entity.position();
            this.previousTickRot = new Vec2F(this.entity.getXRot(), this.entity.getYRot());
            if (this.onInterpolationStart != null) {
                this.onInterpolationStart.accept(this);
            }

        }
    }

    public boolean hasActiveInterpolation() {
        return this.interpolationData.steps > 0;
    }

    public void setInterpolationLength(int i) {
        this.interpolationSteps = i;
    }

    public void interpolate() {
        if (!this.hasActiveInterpolation()) {
            this.cancel();
        } else {
            double d0 = 1.0D / (double) this.interpolationData.steps;

            if (this.previousTickPosition != null) {
                Vec3D vec3d = this.entity.position().subtract(this.previousTickPosition);

                if (this.entity.level().noCollision(this.entity, this.entity.makeBoundingBox().move(this.interpolationData.position.add(vec3d)))) {
                    this.interpolationData.addDelta(vec3d);
                }
            }

            if (this.previousTickRot != null) {
                float f = this.entity.getYRot() - this.previousTickRot.y;
                float f1 = this.entity.getXRot() - this.previousTickRot.x;

                this.interpolationData.addRotation(f, f1);
            }

            double d1 = MathHelper.lerp(d0, this.entity.getX(), this.interpolationData.position.x);
            double d2 = MathHelper.lerp(d0, this.entity.getY(), this.interpolationData.position.y);
            double d3 = MathHelper.lerp(d0, this.entity.getZ(), this.interpolationData.position.z);
            Vec3D vec3d1 = new Vec3D(d1, d2, d3);
            float f2 = (float) MathHelper.rotLerp(d0, (double) this.entity.getYRot(), (double) this.interpolationData.yRot);
            float f3 = (float) MathHelper.lerp(d0, (double) this.entity.getXRot(), (double) this.interpolationData.xRot);

            this.entity.setPos(vec3d1);
            this.entity.setRot(f2, f3);
            this.interpolationData.decrease();
            this.previousTickPosition = vec3d1;
            this.previousTickRot = new Vec2F(this.entity.getXRot(), this.entity.getYRot());
        }
    }

    public void cancel() {
        this.interpolationData.steps = 0;
        this.previousTickPosition = null;
        this.previousTickRot = null;
    }

    private static class a {

        protected int steps;
        Vec3D position;
        float yRot;
        float xRot;

        a(int i, Vec3D vec3d, float f, float f1) {
            this.steps = i;
            this.position = vec3d;
            this.yRot = f;
            this.xRot = f1;
        }

        public void decrease() {
            --this.steps;
        }

        public void addDelta(Vec3D vec3d) {
            this.position = this.position.add(vec3d);
        }

        public void addRotation(float f, float f1) {
            this.yRot += f;
            this.xRot += f1;
        }
    }
}
