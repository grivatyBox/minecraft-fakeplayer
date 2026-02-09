package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;

public class FlowerBedBlock extends VegetationBlock implements IBlockFragilePlantElement, SegmentableBlock {

    public static final MapCodec<FlowerBedBlock> CODEC = simpleCodec(FlowerBedBlock::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.HORIZONTAL_FACING;
    public static final BlockStateInteger AMOUNT = BlockProperties.FLOWER_AMOUNT;
    private final Function<IBlockData, VoxelShape> shapes;

    @Override
    public MapCodec<FlowerBedBlock> codec() {
        return FlowerBedBlock.CODEC;
    }

    protected FlowerBedBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(FlowerBedBlock.FACING, EnumDirection.NORTH)).setValue(FlowerBedBlock.AMOUNT, 1));
        this.shapes = this.makeShapes();
    }

    private Function<IBlockData, VoxelShape> makeShapes() {
        return this.getShapeForEachState(this.getShapeCalculator(FlowerBedBlock.FACING, FlowerBedBlock.AMOUNT));
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(FlowerBedBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(FlowerBedBlock.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(FlowerBedBlock.FACING)));
    }

    @Override
    public boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return this.canBeReplaced(iblockdata, blockactioncontext, FlowerBedBlock.AMOUNT) ? true : super.canBeReplaced(iblockdata, blockactioncontext);
    }

    @Override
    public VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    public double getShapeHeight() {
        return 3.0D;
    }

    @Override
    public BlockStateInteger getSegmentAmountProperty() {
        return FlowerBedBlock.AMOUNT;
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return this.getStateForPlacement(blockactioncontext, this, FlowerBedBlock.AMOUNT, FlowerBedBlock.FACING);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(FlowerBedBlock.FACING, FlowerBedBlock.AMOUNT);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        int i = (Integer) iblockdata.getValue(FlowerBedBlock.AMOUNT);

        if (i < 4) {
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(FlowerBedBlock.AMOUNT, i + 1), 2);
        } else {
            popResource(worldserver, blockposition, new ItemStack(this));
        }

    }
}
