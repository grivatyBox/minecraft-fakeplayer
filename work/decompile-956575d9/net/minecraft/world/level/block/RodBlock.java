package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public abstract class RodBlock extends BlockDirectional {

    private static final Map<EnumDirection.EnumAxis, VoxelShape> SHAPES = VoxelShapes.rotateAllAxis(Block.cube(4.0D, 4.0D, 16.0D));

    protected RodBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected abstract MapCodec<? extends RodBlock> codec();

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) RodBlock.SHAPES.get(((EnumDirection) iblockdata.getValue(RodBlock.FACING)).getAxis());
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(RodBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(RodBlock.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return (IBlockData) iblockdata.setValue(RodBlock.FACING, enumblockmirror.mirror((EnumDirection) iblockdata.getValue(RodBlock.FACING)));
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
