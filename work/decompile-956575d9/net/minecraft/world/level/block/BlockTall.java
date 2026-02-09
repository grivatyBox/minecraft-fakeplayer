package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public abstract class BlockTall extends Block implements IBlockWaterlogged {

    public static final BlockStateBoolean NORTH = BlockSprawling.NORTH;
    public static final BlockStateBoolean EAST = BlockSprawling.EAST;
    public static final BlockStateBoolean SOUTH = BlockSprawling.SOUTH;
    public static final BlockStateBoolean WEST = BlockSprawling.WEST;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    public static final Map<EnumDirection, BlockStateBoolean> PROPERTY_BY_DIRECTION = (Map) BlockSprawling.PROPERTY_BY_DIRECTION.entrySet().stream().filter((entry) -> {
        return ((EnumDirection) entry.getKey()).getAxis().isHorizontal();
    }).collect(SystemUtils.toMap());
    private final Function<IBlockData, VoxelShape> collisionShapes;
    private final Function<IBlockData, VoxelShape> shapes;

    protected BlockTall(float f, float f1, float f2, float f3, float f4, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.collisionShapes = this.makeShapes(f, f4, f2, 0.0F, f4);
        this.shapes = this.makeShapes(f, f1, f2, 0.0F, f3);
    }

    @Override
    protected abstract MapCodec<? extends BlockTall> codec();

    protected Function<IBlockData, VoxelShape> makeShapes(float f, float f1, float f2, float f3, float f4) {
        VoxelShape voxelshape = Block.column((double) f, 0.0D, (double) f1);
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateHorizontal(Block.boxZ((double) f2, (double) f3, (double) f4, 0.0D, 8.0D));

        return this.getShapeForEachState((iblockdata) -> {
            VoxelShape voxelshape1 = voxelshape;

            for (Map.Entry<EnumDirection, BlockStateBoolean> map_entry : BlockTall.PROPERTY_BY_DIRECTION.entrySet()) {
                if ((Boolean) iblockdata.getValue((IBlockState) map_entry.getValue())) {
                    voxelshape1 = VoxelShapes.or(voxelshape1, (VoxelShape) map.get(map_entry.getKey()));
                }
            }

            return voxelshape1;
        }, new IBlockState[]{BlockTall.WATERLOGGED});
    }

    @Override
    protected boolean propagatesSkylightDown(IBlockData iblockdata) {
        return !(Boolean) iblockdata.getValue(BlockTall.WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.collisionShapes.apply(iblockdata);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockTall.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        switch (enumblockrotation) {
            case CLOCKWISE_180:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockTall.NORTH, (Boolean) iblockdata.getValue(BlockTall.SOUTH))).setValue(BlockTall.EAST, (Boolean) iblockdata.getValue(BlockTall.WEST))).setValue(BlockTall.SOUTH, (Boolean) iblockdata.getValue(BlockTall.NORTH))).setValue(BlockTall.WEST, (Boolean) iblockdata.getValue(BlockTall.EAST));
            case COUNTERCLOCKWISE_90:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockTall.NORTH, (Boolean) iblockdata.getValue(BlockTall.EAST))).setValue(BlockTall.EAST, (Boolean) iblockdata.getValue(BlockTall.SOUTH))).setValue(BlockTall.SOUTH, (Boolean) iblockdata.getValue(BlockTall.WEST))).setValue(BlockTall.WEST, (Boolean) iblockdata.getValue(BlockTall.NORTH));
            case CLOCKWISE_90:
                return (IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) iblockdata.setValue(BlockTall.NORTH, (Boolean) iblockdata.getValue(BlockTall.WEST))).setValue(BlockTall.EAST, (Boolean) iblockdata.getValue(BlockTall.NORTH))).setValue(BlockTall.SOUTH, (Boolean) iblockdata.getValue(BlockTall.EAST))).setValue(BlockTall.WEST, (Boolean) iblockdata.getValue(BlockTall.SOUTH));
            default:
                return iblockdata;
        }
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        switch (enumblockmirror) {
            case LEFT_RIGHT:
                return (IBlockData) ((IBlockData) iblockdata.setValue(BlockTall.NORTH, (Boolean) iblockdata.getValue(BlockTall.SOUTH))).setValue(BlockTall.SOUTH, (Boolean) iblockdata.getValue(BlockTall.NORTH));
            case FRONT_BACK:
                return (IBlockData) ((IBlockData) iblockdata.setValue(BlockTall.EAST, (Boolean) iblockdata.getValue(BlockTall.WEST))).setValue(BlockTall.WEST, (Boolean) iblockdata.getValue(BlockTall.EAST));
            default:
                return super.mirror(iblockdata, enumblockmirror);
        }
    }
}
