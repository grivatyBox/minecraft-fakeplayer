package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class DryVegetationBlock extends VegetationBlock {

    public static final MapCodec<DryVegetationBlock> CODEC = simpleCodec(DryVegetationBlock::new);
    private static final VoxelShape SHAPE = Block.column(12.0D, 0.0D, 13.0D);
    private static final int IDLE_SOUND_CHANCE = 150;
    private static final int IDLE_SOUND_BADLANDS_DECREASED_CHANCE = 5;

    @Override
    public MapCodec<? extends DryVegetationBlock> codec() {
        return DryVegetationBlock.CODEC;
    }

    protected DryVegetationBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return DryVegetationBlock.SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.is(TagsBlock.DRY_VEGETATION_MAY_PLACE_ON);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(150) == 0) {
            IBlockData iblockdata1 = world.getBlockState(blockposition.below());

            if ((iblockdata1.is(Blocks.RED_SAND) || iblockdata1.is(TagsBlock.TERRACOTTA)) && randomsource.nextInt(5) != 0) {
                return;
            }

            IBlockData iblockdata2 = world.getBlockState(blockposition.below(2));

            if (iblockdata1.is(TagsBlock.PLAYS_AMBIENT_DESERT_BLOCK_SOUNDS) && iblockdata2.is(TagsBlock.PLAYS_AMBIENT_DESERT_BLOCK_SOUNDS)) {
                world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.DEAD_BUSH_IDLE, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            }
        }

    }
}
