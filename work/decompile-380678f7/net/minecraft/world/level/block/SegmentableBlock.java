package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;

public interface SegmentableBlock {

    int MIN_SEGMENT = 1;
    int MAX_SEGMENT = 4;
    BlockStateInteger AMOUNT = BlockProperties.SEGMENT_AMOUNT;

    default Function<IBlockData, VoxelShape> getShapeCalculator(BlockStateEnum<EnumDirection> blockstateenum, BlockStateInteger blockstateinteger) {
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateHorizontal(Block.box(0.0D, 0.0D, 0.0D, 8.0D, this.getShapeHeight(), 8.0D));

        return (iblockdata) -> {
            VoxelShape voxelshape = VoxelShapes.empty();
            EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(blockstateenum);
            int i = (Integer) iblockdata.getValue(blockstateinteger);

            for (int j = 0; j < i; ++j) {
                voxelshape = VoxelShapes.or(voxelshape, (VoxelShape) map.get(enumdirection));
                enumdirection = enumdirection.getCounterClockWise();
            }

            return voxelshape.singleEncompassing();
        };
    }

    default BlockStateInteger getSegmentAmountProperty() {
        return SegmentableBlock.AMOUNT;
    }

    default double getShapeHeight() {
        return 1.0D;
    }

    default boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext, BlockStateInteger blockstateinteger) {
        return !blockactioncontext.isSecondaryUseActive() && blockactioncontext.getItemInHand().is(iblockdata.getBlock().asItem()) && (Integer) iblockdata.getValue(blockstateinteger) < 4;
    }

    default IBlockData getStateForPlacement(BlockActionContext blockactioncontext, Block block, BlockStateInteger blockstateinteger, BlockStateEnum<EnumDirection> blockstateenum) {
        IBlockData iblockdata = blockactioncontext.getLevel().getBlockState(blockactioncontext.getClickedPos());

        return iblockdata.is(block) ? (IBlockData) iblockdata.setValue(blockstateinteger, Math.min(4, (Integer) iblockdata.getValue(blockstateinteger) + 1)) : (IBlockData) block.defaultBlockState().setValue(blockstateenum, blockactioncontext.getHorizontalDirection().getOpposite());
    }
}
