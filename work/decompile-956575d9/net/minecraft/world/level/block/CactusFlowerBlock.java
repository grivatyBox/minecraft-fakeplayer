package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class CactusFlowerBlock extends VegetationBlock {

    public static final MapCodec<CactusFlowerBlock> CODEC = simpleCodec(CactusFlowerBlock::new);
    private static final VoxelShape SHAPE = Block.column(14.0D, 0.0D, 12.0D);

    @Override
    public MapCodec<? extends CactusFlowerBlock> codec() {
        return CactusFlowerBlock.CODEC;
    }

    public CactusFlowerBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return CactusFlowerBlock.SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        IBlockData iblockdata1 = iblockaccess.getBlockState(blockposition);

        return iblockdata1.is(Blocks.CACTUS) || iblockdata1.is(Blocks.FARMLAND) || iblockdata1.isFaceSturdy(iblockaccess, blockposition, EnumDirection.UP, EnumBlockSupport.CENTER);
    }
}
