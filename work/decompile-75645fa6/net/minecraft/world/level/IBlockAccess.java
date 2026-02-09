package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface IBlockAccess extends LevelHeightAccessor {

    @Nullable
    TileEntity getBlockEntity(BlockPosition blockposition);

    default <T extends TileEntity> Optional<T> getBlockEntity(BlockPosition blockposition, TileEntityTypes<T> tileentitytypes) {
        TileEntity tileentity = this.getBlockEntity(blockposition);

        return tileentity != null && tileentity.getType() == tileentitytypes ? Optional.of(tileentity) : Optional.empty();
    }

    IBlockData getBlockState(BlockPosition blockposition);

    Fluid getFluidState(BlockPosition blockposition);

    default int getLightEmission(BlockPosition blockposition) {
        return this.getBlockState(blockposition).getLightEmission();
    }

    default Stream<IBlockData> getBlockStates(AxisAlignedBB axisalignedbb) {
        return BlockPosition.betweenClosedStream(axisalignedbb).map(this::getBlockState);
    }

    default MovingObjectPositionBlock isBlockInLine(ClipBlockStateContext clipblockstatecontext) {
        return (MovingObjectPositionBlock) traverseBlocks(clipblockstatecontext.getFrom(), clipblockstatecontext.getTo(), clipblockstatecontext, (clipblockstatecontext1, blockposition) -> {
            IBlockData iblockdata = this.getBlockState(blockposition);
            Vec3D vec3d = clipblockstatecontext1.getFrom().subtract(clipblockstatecontext1.getTo());

            return clipblockstatecontext1.isTargetBlock().test(iblockdata) ? new MovingObjectPositionBlock(clipblockstatecontext1.getTo(), EnumDirection.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z), BlockPosition.containing(clipblockstatecontext1.getTo()), false) : null;
        }, (clipblockstatecontext1) -> {
            Vec3D vec3d = clipblockstatecontext1.getFrom().subtract(clipblockstatecontext1.getTo());

            return MovingObjectPositionBlock.miss(clipblockstatecontext1.getTo(), EnumDirection.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z), BlockPosition.containing(clipblockstatecontext1.getTo()));
        });
    }

    default MovingObjectPositionBlock clip(RayTrace raytrace) {
        return (MovingObjectPositionBlock) traverseBlocks(raytrace.getFrom(), raytrace.getTo(), raytrace, (raytrace1, blockposition) -> {
            IBlockData iblockdata = this.getBlockState(blockposition);
            Fluid fluid = this.getFluidState(blockposition);
            Vec3D vec3d = raytrace1.getFrom();
            Vec3D vec3d1 = raytrace1.getTo();
            VoxelShape voxelshape = raytrace1.getBlockShape(iblockdata, this, blockposition);
            MovingObjectPositionBlock movingobjectpositionblock = this.clipWithInteractionOverride(vec3d, vec3d1, blockposition, voxelshape, iblockdata);
            VoxelShape voxelshape1 = raytrace1.getFluidShape(fluid, this, blockposition);
            MovingObjectPositionBlock movingobjectpositionblock1 = voxelshape1.clip(vec3d, vec3d1, blockposition);
            double d0 = movingobjectpositionblock == null ? Double.MAX_VALUE : raytrace1.getFrom().distanceToSqr(movingobjectpositionblock.getLocation());
            double d1 = movingobjectpositionblock1 == null ? Double.MAX_VALUE : raytrace1.getFrom().distanceToSqr(movingobjectpositionblock1.getLocation());

            return d0 <= d1 ? movingobjectpositionblock : movingobjectpositionblock1;
        }, (raytrace1) -> {
            Vec3D vec3d = raytrace1.getFrom().subtract(raytrace1.getTo());

            return MovingObjectPositionBlock.miss(raytrace1.getTo(), EnumDirection.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z), BlockPosition.containing(raytrace1.getTo()));
        });
    }

    @Nullable
    default MovingObjectPositionBlock clipWithInteractionOverride(Vec3D vec3d, Vec3D vec3d1, BlockPosition blockposition, VoxelShape voxelshape, IBlockData iblockdata) {
        MovingObjectPositionBlock movingobjectpositionblock = voxelshape.clip(vec3d, vec3d1, blockposition);

        if (movingobjectpositionblock != null) {
            MovingObjectPositionBlock movingobjectpositionblock1 = iblockdata.getInteractionShape(this, blockposition).clip(vec3d, vec3d1, blockposition);

            if (movingobjectpositionblock1 != null && movingobjectpositionblock1.getLocation().subtract(vec3d).lengthSqr() < movingobjectpositionblock.getLocation().subtract(vec3d).lengthSqr()) {
                return movingobjectpositionblock.withDirection(movingobjectpositionblock1.getDirection());
            }
        }

        return movingobjectpositionblock;
    }

    default double getBlockFloorHeight(VoxelShape voxelshape, Supplier<VoxelShape> supplier) {
        if (!voxelshape.isEmpty()) {
            return voxelshape.max(EnumDirection.EnumAxis.Y);
        } else {
            double d0 = ((VoxelShape) supplier.get()).max(EnumDirection.EnumAxis.Y);

            return d0 >= 1.0D ? d0 - 1.0D : Double.NEGATIVE_INFINITY;
        }
    }

    default double getBlockFloorHeight(BlockPosition blockposition) {
        return this.getBlockFloorHeight(this.getBlockState(blockposition).getCollisionShape(this, blockposition), () -> {
            BlockPosition blockposition1 = blockposition.below();

            return this.getBlockState(blockposition1).getCollisionShape(this, blockposition1);
        });
    }

    static <T, C> T traverseBlocks(Vec3D vec3d, Vec3D vec3d1, C c0, BiFunction<C, BlockPosition, T> bifunction, Function<C, T> function) {
        if (vec3d.equals(vec3d1)) {
            return (T) function.apply(c0);
        } else {
            double d0 = MathHelper.lerp(-1.0E-7D, vec3d1.x, vec3d.x);
            double d1 = MathHelper.lerp(-1.0E-7D, vec3d1.y, vec3d.y);
            double d2 = MathHelper.lerp(-1.0E-7D, vec3d1.z, vec3d.z);
            double d3 = MathHelper.lerp(-1.0E-7D, vec3d.x, vec3d1.x);
            double d4 = MathHelper.lerp(-1.0E-7D, vec3d.y, vec3d1.y);
            double d5 = MathHelper.lerp(-1.0E-7D, vec3d.z, vec3d1.z);
            int i = MathHelper.floor(d3);
            int j = MathHelper.floor(d4);
            int k = MathHelper.floor(d5);
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(i, j, k);
            T t0 = (T) bifunction.apply(c0, blockposition_mutableblockposition);

            if (t0 != null) {
                return t0;
            } else {
                double d6 = d0 - d3;
                double d7 = d1 - d4;
                double d8 = d2 - d5;
                int l = MathHelper.sign(d6);
                int i1 = MathHelper.sign(d7);
                int j1 = MathHelper.sign(d8);
                double d9 = l == 0 ? Double.MAX_VALUE : (double) l / d6;
                double d10 = i1 == 0 ? Double.MAX_VALUE : (double) i1 / d7;
                double d11 = j1 == 0 ? Double.MAX_VALUE : (double) j1 / d8;
                double d12 = d9 * (l > 0 ? 1.0D - MathHelper.frac(d3) : MathHelper.frac(d3));
                double d13 = d10 * (i1 > 0 ? 1.0D - MathHelper.frac(d4) : MathHelper.frac(d4));
                double d14 = d11 * (j1 > 0 ? 1.0D - MathHelper.frac(d5) : MathHelper.frac(d5));

                while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                    if (d12 < d13) {
                        if (d12 < d14) {
                            i += l;
                            d12 += d9;
                        } else {
                            k += j1;
                            d14 += d11;
                        }
                    } else if (d13 < d14) {
                        j += i1;
                        d13 += d10;
                    } else {
                        k += j1;
                        d14 += d11;
                    }

                    T t1 = (T) bifunction.apply(c0, blockposition_mutableblockposition.set(i, j, k));

                    if (t1 != null) {
                        return t1;
                    }
                }

                return (T) function.apply(c0);
            }
        }
    }

    static boolean forEachBlockIntersectedBetween(Vec3D vec3d, Vec3D vec3d1, AxisAlignedBB axisalignedbb, IBlockAccess.a iblockaccess_a) {
        Vec3D vec3d2 = vec3d1.subtract(vec3d);

        if (vec3d2.lengthSqr() < (double) MathHelper.square(1.0E-5F)) {
            for (BlockPosition blockposition : BlockPosition.betweenClosed(axisalignedbb)) {
                if (!iblockaccess_a.visit(blockposition, 0)) {
                    return false;
                }
            }

            return true;
        } else {
            LongSet longset = new LongOpenHashSet();

            for (BlockPosition blockposition1 : BlockPosition.betweenCornersInDirection(axisalignedbb.move(vec3d2.scale(-1.0D)), vec3d2)) {
                if (!iblockaccess_a.visit(blockposition1, 0)) {
                    return false;
                }

                longset.add(blockposition1.asLong());
            }

            int i = addCollisionsAlongTravel(longset, vec3d2, axisalignedbb, iblockaccess_a);

            if (i < 0) {
                return false;
            } else {
                for (BlockPosition blockposition2 : BlockPosition.betweenCornersInDirection(axisalignedbb, vec3d2)) {
                    if (longset.add(blockposition2.asLong()) && !iblockaccess_a.visit(blockposition2, i + 1)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private static int addCollisionsAlongTravel(LongSet longset, Vec3D vec3d, AxisAlignedBB axisalignedbb, IBlockAccess.a iblockaccess_a) {
        double d0 = axisalignedbb.getXsize();
        double d1 = axisalignedbb.getYsize();
        double d2 = axisalignedbb.getZsize();
        BaseBlockPosition baseblockposition = getFurthestCorner(vec3d);
        Vec3D vec3d1 = axisalignedbb.getCenter();
        Vec3D vec3d2 = new Vec3D(vec3d1.x() + d0 * 0.5D * (double) baseblockposition.getX(), vec3d1.y() + d1 * 0.5D * (double) baseblockposition.getY(), vec3d1.z() + d2 * 0.5D * (double) baseblockposition.getZ());
        Vec3D vec3d3 = vec3d2.subtract(vec3d);
        int i = MathHelper.floor(vec3d3.x);
        int j = MathHelper.floor(vec3d3.y);
        int k = MathHelper.floor(vec3d3.z);
        int l = MathHelper.sign(vec3d.x);
        int i1 = MathHelper.sign(vec3d.y);
        int j1 = MathHelper.sign(vec3d.z);
        double d3 = l == 0 ? Double.MAX_VALUE : (double) l / vec3d.x;
        double d4 = i1 == 0 ? Double.MAX_VALUE : (double) i1 / vec3d.y;
        double d5 = j1 == 0 ? Double.MAX_VALUE : (double) j1 / vec3d.z;
        double d6 = d3 * (l > 0 ? 1.0D - MathHelper.frac(vec3d3.x) : MathHelper.frac(vec3d3.x));
        double d7 = d4 * (i1 > 0 ? 1.0D - MathHelper.frac(vec3d3.y) : MathHelper.frac(vec3d3.y));
        double d8 = d5 * (j1 > 0 ? 1.0D - MathHelper.frac(vec3d3.z) : MathHelper.frac(vec3d3.z));
        int k1 = 0;

        while (d6 <= 1.0D || d7 <= 1.0D || d8 <= 1.0D) {
            if (d6 < d7) {
                if (d6 < d8) {
                    i += l;
                    d6 += d3;
                } else {
                    k += j1;
                    d8 += d5;
                }
            } else if (d7 < d8) {
                j += i1;
                d7 += d4;
            } else {
                k += j1;
                d8 += d5;
            }

            Optional<Vec3D> optional = AxisAlignedBB.clip((double) i, (double) j, (double) k, (double) (i + 1), (double) (j + 1), (double) (k + 1), vec3d3, vec3d2);

            if (!optional.isEmpty()) {
                ++k1;
                Vec3D vec3d4 = (Vec3D) optional.get();
                double d9 = MathHelper.clamp(vec3d4.x, (double) i + (double) 1.0E-5F, (double) i + 1.0D - (double) 1.0E-5F);
                double d10 = MathHelper.clamp(vec3d4.y, (double) j + (double) 1.0E-5F, (double) j + 1.0D - (double) 1.0E-5F);
                double d11 = MathHelper.clamp(vec3d4.z, (double) k + (double) 1.0E-5F, (double) k + 1.0D - (double) 1.0E-5F);
                int l1 = MathHelper.floor(d9 - d0 * (double) baseblockposition.getX());
                int i2 = MathHelper.floor(d10 - d1 * (double) baseblockposition.getY());
                int j2 = MathHelper.floor(d11 - d2 * (double) baseblockposition.getZ());
                int k2 = k1;

                for (BlockPosition blockposition : BlockPosition.betweenCornersInDirection(i, j, k, l1, i2, j2, vec3d)) {
                    if (longset.add(blockposition.asLong()) && !iblockaccess_a.visit(blockposition, k2)) {
                        return -1;
                    }
                }
            }
        }

        return k1;
    }

    private static BaseBlockPosition getFurthestCorner(Vec3D vec3d) {
        double d0 = Math.abs(Vec3D.X_AXIS.dot(vec3d));
        double d1 = Math.abs(Vec3D.Y_AXIS.dot(vec3d));
        double d2 = Math.abs(Vec3D.Z_AXIS.dot(vec3d));
        int i = vec3d.x >= 0.0D ? 1 : -1;
        int j = vec3d.y >= 0.0D ? 1 : -1;
        int k = vec3d.z >= 0.0D ? 1 : -1;

        return d0 <= d1 && d0 <= d2 ? new BaseBlockPosition(-i, -k, j) : (d1 <= d2 ? new BaseBlockPosition(k, -j, -i) : new BaseBlockPosition(-j, i, -k));
    }

    @FunctionalInterface
    public interface a {

        boolean visit(BlockPosition blockposition, int i);
    }
}
