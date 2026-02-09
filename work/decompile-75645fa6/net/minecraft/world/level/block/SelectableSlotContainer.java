package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.MathHelper;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;

public interface SelectableSlotContainer {

    int getRows();

    int getColumns();

    default OptionalInt getHitSlot(MovingObjectPositionBlock movingobjectpositionblock, EnumDirection enumdirection) {
        return (OptionalInt) getRelativeHitCoordinatesForBlockFace(movingobjectpositionblock, enumdirection).map((vec2f) -> {
            int i = getSection(1.0F - vec2f.y, this.getRows());
            int j = getSection(vec2f.x, this.getColumns());

            return OptionalInt.of(j + i * this.getColumns());
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2F> getRelativeHitCoordinatesForBlockFace(MovingObjectPositionBlock movingobjectpositionblock, EnumDirection enumdirection) {
        EnumDirection enumdirection1 = movingobjectpositionblock.getDirection();

        if (enumdirection != enumdirection1) {
            return Optional.empty();
        } else {
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos().relative(enumdirection1);
            Vec3D vec3d = movingobjectpositionblock.getLocation().subtract((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
            double d0 = vec3d.x();
            double d1 = vec3d.y();
            double d2 = vec3d.z();
            Optional optional;

            switch (enumdirection1) {
                case NORTH:
                    optional = Optional.of(new Vec2F((float) (1.0D - d0), (float) d1));
                    break;
                case SOUTH:
                    optional = Optional.of(new Vec2F((float) d0, (float) d1));
                    break;
                case WEST:
                    optional = Optional.of(new Vec2F((float) d2, (float) d1));
                    break;
                case EAST:
                    optional = Optional.of(new Vec2F((float) (1.0D - d2), (float) d1));
                    break;
                case DOWN:
                case UP:
                    optional = Optional.empty();
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return optional;
        }
    }

    static int getSection(float f, int i) {
        float f1 = f * 16.0F;
        float f2 = 16.0F / (float) i;

        return MathHelper.clamp(MathHelper.floor(f1 / f2), 0, i - 1);
    }
}
