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
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockCoralFanWallAbstract extends BlockCoralFanAbstract {

    public static final MapCodec<BlockCoralFanWallAbstract> CODEC = simpleCodec(BlockCoralFanWallAbstract::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    private static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateHorizontal(Block.boxZ(16.0D, 8.0D, 5.0D, 16.0D));

    @Override
    public MapCodec<? extends BlockCoralFanWallAbstract> codec() {
        return BlockCoralFanWallAbstract.CODEC;
    }

    protected BlockCoralFanWallAbstract(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockCoralFanWallAbstract.FACING, EnumDirection.NORTH)).setValue(BlockCoralFanWallAbstract.WATERLOGGED, true));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) BlockCoralFanWallAbstract.SHAPES.get(iblockdata.getValue(BlockCoralFanWallAbstract.FACING));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockCoralFanWallAbstract.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockCoralFanWallAbstract.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockCoralFanWallAbstract.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockCoralFanWallAbstract.FACING, BlockCoralFanWallAbstract.WATERLOGGED);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockCoralFanWallAbstract.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return enumdirection.getOpposite() == iblockdata.getValue(BlockCoralFanWallAbstract.FACING) && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : iblockdata;
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(BlockCoralFanWallAbstract.FACING);
        BlockPosition blockposition1 = blockposition.relative(enumdirection.getOpposite());
        IBlockData iblockdata1 = iworldreader.getBlockState(blockposition1);

        return iblockdata1.isFaceSturdy(iworldreader, blockposition1, enumdirection);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = super.getStateForPlacement(blockactioncontext);
        IWorldReader iworldreader = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();

        for (EnumDirection enumdirection : aenumdirection) {
            if (enumdirection.getAxis().isHorizontal()) {
                iblockdata = (IBlockData) iblockdata.setValue(BlockCoralFanWallAbstract.FACING, enumdirection.getOpposite());
                if (iblockdata.canSurvive(iworldreader, blockposition)) {
                    return iblockdata;
                }
            }
        }

        return null;
    }
}
