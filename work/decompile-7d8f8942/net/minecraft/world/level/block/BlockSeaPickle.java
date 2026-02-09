package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class BlockSeaPickle extends VegetationBlock implements IBlockFragilePlantElement, IBlockWaterlogged {

    public static final MapCodec<BlockSeaPickle> CODEC = simpleCodec(BlockSeaPickle::new);
    public static final int MAX_PICKLES = 4;
    public static final BlockStateInteger PICKLES = BlockProperties.PICKLES;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_ONE = Block.column(4.0D, 0.0D, 6.0D);
    private static final VoxelShape SHAPE_TWO = Block.column(10.0D, 0.0D, 6.0D);
    private static final VoxelShape SHAPE_THREE = Block.column(12.0D, 0.0D, 6.0D);
    private static final VoxelShape SHAPE_FOUR = Block.column(12.0D, 0.0D, 7.0D);

    @Override
    public MapCodec<BlockSeaPickle> codec() {
        return BlockSeaPickle.CODEC;
    }

    protected BlockSeaPickle(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockSeaPickle.PICKLES, 1)).setValue(BlockSeaPickle.WATERLOGGED, true));
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = blockactioncontext.getLevel().getBlockState(blockactioncontext.getClickedPos());

        if (iblockdata.is(this)) {
            return (IBlockData) iblockdata.setValue(BlockSeaPickle.PICKLES, Math.min(4, (Integer) iblockdata.getValue(BlockSeaPickle.PICKLES) + 1));
        } else {
            Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());
            boolean flag = fluid.getType() == FluidTypes.WATER;

            return (IBlockData) super.getStateForPlacement(blockactioncontext).setValue(BlockSeaPickle.WATERLOGGED, flag);
        }
    }

    public static boolean isDead(IBlockData iblockdata) {
        return !(Boolean) iblockdata.getValue(BlockSeaPickle.WATERLOGGED);
    }

    @Override
    protected boolean mayPlaceOn(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return !iblockdata.getCollisionShape(iblockaccess, blockposition).getFaceShape(EnumDirection.UP).isEmpty() || iblockdata.isFaceSturdy(iblockaccess, blockposition, EnumDirection.UP);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();

        return this.mayPlaceOn(iworldreader.getBlockState(blockposition1), iworldreader, blockposition1);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (!iblockdata.canSurvive(iworldreader, blockposition)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if ((Boolean) iblockdata.getValue(BlockSeaPickle.WATERLOGGED)) {
                scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
            }

            return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
        }
    }

    @Override
    protected boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return !blockactioncontext.isSecondaryUseActive() && blockactioncontext.getItemInHand().is(this.asItem()) && (Integer) iblockdata.getValue(BlockSeaPickle.PICKLES) < 4 ? true : super.canBeReplaced(iblockdata, blockactioncontext);
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        VoxelShape voxelshape;

        switch ((Integer) iblockdata.getValue(BlockSeaPickle.PICKLES)) {
            case 2:
                voxelshape = BlockSeaPickle.SHAPE_TWO;
                break;
            case 3:
                voxelshape = BlockSeaPickle.SHAPE_THREE;
                break;
            case 4:
                voxelshape = BlockSeaPickle.SHAPE_FOUR;
                break;
            default:
                voxelshape = BlockSeaPickle.SHAPE_ONE;
        }

        return voxelshape;
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockSeaPickle.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockSeaPickle.PICKLES, BlockSeaPickle.WATERLOGGED);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return !isDead(iblockdata) && iworldreader.getBlockState(blockposition.below()).is(TagsBlock.CORAL_BLOCKS);
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        int i = 5;
        int j = 1;
        int k = 2;
        int l = 0;
        int i1 = blockposition.getX() - 2;
        int j1 = 0;

        for (int k1 = 0; k1 < 5; ++k1) {
            for (int l1 = 0; l1 < j; ++l1) {
                int i2 = 2 + blockposition.getY() - 1;

                for (int j2 = i2 - 2; j2 < i2; ++j2) {
                    BlockPosition blockposition1 = new BlockPosition(i1 + k1, j2, blockposition.getZ() - j1 + l1);

                    if (blockposition1 != blockposition && randomsource.nextInt(6) == 0 && worldserver.getBlockState(blockposition1).is(Blocks.WATER)) {
                        IBlockData iblockdata1 = worldserver.getBlockState(blockposition1.below());

                        if (iblockdata1.is(TagsBlock.CORAL_BLOCKS)) {
                            worldserver.setBlock(blockposition1, (IBlockData) Blocks.SEA_PICKLE.defaultBlockState().setValue(BlockSeaPickle.PICKLES, randomsource.nextInt(4) + 1), 3);
                        }
                    }
                }
            }

            if (l < 2) {
                j += 2;
                ++j1;
            } else {
                j -= 2;
                --j1;
            }

            ++l;
        }

        worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockSeaPickle.PICKLES, 4), 2);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
