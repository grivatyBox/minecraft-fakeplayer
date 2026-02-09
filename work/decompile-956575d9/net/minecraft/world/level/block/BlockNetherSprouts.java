package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockNetherSprouts extends VegetationBlock {

    public static final MapCodec<BlockNetherSprouts> CODEC = simpleCodec(BlockNetherSprouts::new);
    private static final VoxelShape SHAPE = Block.column(12.0D, 0.0D, 3.0D);

    @Override
    public MapCodec<BlockNetherSprouts> codec() {
        return BlockNetherSprouts.CODEC;
    }

    public BlockNetherSprouts(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockNetherSprouts.SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(TagsBlock.NYLIUM) || iblockdata.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(iblockdata, iblockaccess, blockposition);
    }
}
