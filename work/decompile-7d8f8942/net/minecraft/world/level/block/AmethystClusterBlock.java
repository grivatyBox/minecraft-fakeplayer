package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
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

public class AmethystClusterBlock extends AmethystBlock implements IBlockWaterlogged {

    public static final MapCodec<AmethystClusterBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.FLOAT.fieldOf("height").forGetter((amethystclusterblock) -> {
            return amethystclusterblock.height;
        }), Codec.FLOAT.fieldOf("width").forGetter((amethystclusterblock) -> {
            return amethystclusterblock.width;
        }), propertiesCodec()).apply(instance, AmethystClusterBlock::new);
    });
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.FACING;
    private final float height;
    private final float width;
    private final Map<EnumDirection, VoxelShape> shapes;

    @Override
    public MapCodec<AmethystClusterBlock> codec() {
        return AmethystClusterBlock.CODEC;
    }

    public AmethystClusterBlock(float f, float f1, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) this.defaultBlockState().setValue(AmethystClusterBlock.WATERLOGGED, false)).setValue(AmethystClusterBlock.FACING, EnumDirection.UP));
        this.shapes = VoxelShapes.rotateAll(Block.boxZ((double) f1, (double) (16.0F - f), 16.0D));
        this.height = f;
        this.width = f1;
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.get(iblockdata.getValue(AmethystClusterBlock.FACING));
    }

    @Override
    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        EnumDirection enumdirection = (EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING);
        BlockPosition blockposition1 = blockposition.relative(enumdirection.getOpposite());

        return iworldreader.getBlockState(blockposition1).isFaceSturdy(iworldreader, blockposition1, enumdirection);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(AmethystClusterBlock.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return enumdirection == ((EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING)).getOpposite() && !iblockdata.canSurvive(iworldreader, blockposition) ? Blocks.AIR.defaultBlockState() : super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Nullable
    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        GeneratorAccess generatoraccess = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();

        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(AmethystClusterBlock.WATERLOGGED, generatoraccess.getFluidState(blockposition).getType() == FluidTypes.WATER)).setValue(AmethystClusterBlock.FACING, blockactioncontext.getClickedFace());
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(AmethystClusterBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(AmethystClusterBlock.FACING)));
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(AmethystClusterBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(AmethystClusterBlock.WATERLOGGED, AmethystClusterBlock.FACING);
    }
}
