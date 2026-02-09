package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockRoots extends VegetationBlock {

    public static final MapCodec<BlockRoots> CODEC = simpleCodec(BlockRoots::new);
    private static final VoxelShape SHAPE = Block.column(12.0D, 0.0D, 13.0D);

    @Override
    public MapCodec<BlockRoots> codec() {
        return BlockRoots.CODEC;
    }

    protected BlockRoots(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockRoots.SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(TagsBlock.NYLIUM) || iblockdata.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(iblockdata, iblockaccess, blockposition);
    }
}
