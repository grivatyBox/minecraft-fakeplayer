package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class LeafLitterBlock extends VegetationBlock implements SegmentableBlock {

    public static final MapCodec<LeafLitterBlock> CODEC = simpleCodec(LeafLitterBlock::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.HORIZONTAL_FACING;
    private final Function<IBlockData, VoxelShape> shapes;

    public LeafLitterBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(LeafLitterBlock.FACING, EnumDirection.NORTH)).setValue(this.getSegmentAmountProperty(), 1));
        this.shapes = this.makeShapes();
    }

    private Function<IBlockData, VoxelShape> makeShapes() {
        return this.getShapeForEachState(this.getShapeCalculator(LeafLitterBlock.FACING, this.getSegmentAmountProperty()));
    }

    @Override
    protected MapCodec<LeafLitterBlock> codec() {
        return LeafLitterBlock.CODEC;
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(LeafLitterBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(LeafLitterBlock.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(LeafLitterBlock.FACING)));
    }

    @Override
    public boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return this.canBeReplaced(iblockdata, blockactioncontext, this.getSegmentAmountProperty()) ? true : super.canBeReplaced(iblockdata, blockactioncontext);
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.below();

        return iworldreader.getBlockState(blockposition1).isFaceSturdy(iworldreader, blockposition1, EnumDirection.UP);
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return this.getStateForPlacement(blockactioncontext, this, this.getSegmentAmountProperty(), LeafLitterBlock.FACING);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(LeafLitterBlock.FACING, this.getSegmentAmountProperty());
    }
}
