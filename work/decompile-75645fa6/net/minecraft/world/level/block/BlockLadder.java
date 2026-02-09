package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockLadder extends Block implements IBlockWaterlogged {

    public static final MapCodec<BlockLadder> CODEC = simpleCodec(BlockLadder::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    public static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateHorizontal(Block.boxZ(16.0D, 13.0D, 16.0D));

    @Override
    public MapCodec<BlockLadder> codec() {
        return BlockLadder.CODEC;
    }

    protected BlockLadder(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockLadder.FACING, EnumDirection.NORTH)).setValue(BlockLadder.WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BlockLadder.SHAPES.get(iblockdata.getValue(BlockLadder.FACING));
    }

    private boolean canAttachTo(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = iblockaccess.getBlockState(blockposition);

        return iblockdata.isFaceSturdy(iblockaccess, blockposition, enumdirection);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockLadder.FACING);

        return this.canAttachTo(iworldreader, blockposition.relative(enumdirection.getOpposite()), enumdirection);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if (enumdirection.getOpposite() == iblockdata.getValue(BlockLadder.FACING) && !iblockdata.canSurvive(iworldreader, blockposition)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if ((Boolean) iblockdata.getValue(BlockLadder.WATERLOGGED)) {
                scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
            }

            return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
        }
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        if (!blockactioncontext.replacingClickedOnBlock()) {
            IBlockData iblockdata = blockactioncontext.getLevel().getBlockState(blockactioncontext.getClickedPos().relative(blockactioncontext.getClickedFace().getOpposite()));

            if (iblockdata.is(this) && iblockdata.getValue(BlockLadder.FACING) == blockactioncontext.getClickedFace()) {
                return null;
            }
        }

        IBlockData iblockdata1 = this.defaultBlockState();
        IWorldReader iworldreader = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        for (EnumDirection enumdirection : blockactioncontext.getNearestLookingDirections()) {
            if (enumdirection.getAxis().isHorizontal()) {
                iblockdata1 = (IBlockData) iblockdata1.setValue(BlockLadder.FACING, enumdirection.getOpposite());
                if (iblockdata1.canSurvive(iworldreader, blockposition)) {
                    return (IBlockData) iblockdata1.setValue(BlockLadder.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
                }
            }
        }

        return null;
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockLadder.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockLadder.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockLadder.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockLadder.FACING, BlockLadder.WATERLOGGED);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockLadder.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }
}
