package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class TallDryGrassBlock extends DryVegetationBlock implements IBlockFragilePlantElement {

    public static final MapCodec<TallDryGrassBlock> CODEC = simpleCodec(TallDryGrassBlock::new);
    private static final VoxelShape SHAPE = Block.column(14.0D, 0.0D, 16.0D);

    @Override
    public MapCodec<TallDryGrassBlock> codec() {
        return TallDryGrassBlock.CODEC;
    }

    protected TallDryGrassBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return TallDryGrassBlock.SHAPE;
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return IBlockFragilePlantElement.hasSpreadableNeighbourPos(iworldreader, blockposition, Blocks.SHORT_DRY_GRASS.defaultBlockState());
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockFragilePlantElement.findSpreadableNeighbourPos(worldserver, blockposition, Blocks.SHORT_DRY_GRASS.defaultBlockState()).ifPresent((blockposition1) -> {
            worldserver.setBlockAndUpdate(blockposition1, Blocks.SHORT_DRY_GRASS.defaultBlockState());
        });
    }
}
