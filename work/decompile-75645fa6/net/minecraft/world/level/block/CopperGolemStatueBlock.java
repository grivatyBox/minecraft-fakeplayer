package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.jetbrains.annotations.Nullable;

public class CopperGolemStatueBlock extends BlockTileEntity implements IBlockWaterlogged {

    public static final MapCodec<CopperGolemStatueBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(CopperGolemStatueBlock::getWeatheringState), propertiesCodec()).apply(instance, CopperGolemStatueBlock::new);
    });
    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.HORIZONTAL_FACING;
    public static final BlockStateEnum<CopperGolemStatueBlock.a> POSE = BlockProperties.COPPER_GOLEM_POSE;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.column(10.0D, 0.0D, 14.0D);
    private final WeatheringCopper.a weatheringState;

    @Override
    public MapCodec<? extends CopperGolemStatueBlock> codec() {
        return CopperGolemStatueBlock.CODEC;
    }

    public CopperGolemStatueBlock(WeatheringCopper.a weatheringcopper_a, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.weatheringState = weatheringcopper_a;
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(CopperGolemStatueBlock.FACING, EnumDirection.NORTH)).setValue(CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.a.STANDING)).setValue(CopperGolemStatueBlock.WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        super.createBlockStateDefinition(blockstatelist_a);
        blockstatelist_a.add(CopperGolemStatueBlock.FACING, CopperGolemStatueBlock.POSE, CopperGolemStatueBlock.WATERLOGGED);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(CopperGolemStatueBlock.FACING, blockactioncontext.getHorizontalDirection().getOpposite())).setValue(CopperGolemStatueBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(CopperGolemStatueBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(CopperGolemStatueBlock.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(CopperGolemStatueBlock.FACING)));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return CopperGolemStatueBlock.SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(IBlockData iblockdata) {
        return VoxelShapes.empty();
    }

    public WeatheringCopper.a getWeatheringState() {
        return this.weatheringState;
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (itemstack.is(TagsItem.AXES)) {
            return EnumInteractionResult.PASS;
        } else {
            this.updatePose(world, iblockdata, blockposition, entityhuman);
            return EnumInteractionResult.SUCCESS;
        }
    }

    void updatePose(World world, IBlockData iblockdata, BlockPosition blockposition, EntityHuman entityhuman) {
        world.playSound((Entity) null, blockposition, SoundEffects.COPPER_GOLEM_BECOME_STATUE, SoundCategory.BLOCKS);
        world.setBlock(blockposition, (IBlockData) iblockdata.setValue(CopperGolemStatueBlock.POSE, ((CopperGolemStatueBlock.a) iblockdata.getValue(CopperGolemStatueBlock.POSE)).getNextPose()), 3);
        world.gameEvent(entityhuman, (Holder) GameEvent.BLOCK_CHANGE, blockposition);
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return pathmode == PathMode.WATER && iblockdata.getFluidState().is(TagsFluid.WATER);
    }

    @Override
    public @Nullable TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new CopperGolemStatueBlockEntity(blockposition, iblockdata);
    }

    @Override
    public boolean shouldChangedStateKeepBlockEntity(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.COPPER_GOLEM_STATUES);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition, EnumDirection enumdirection) {
        return ((CopperGolemStatueBlock.a) iblockdata.getValue(CopperGolemStatueBlock.POSE)).ordinal() + 1;
    }

    @Override
    protected ItemStack getCloneItemStack(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        TileEntity tileentity = iworldreader.getBlockEntity(blockposition);

        if (tileentity instanceof CopperGolemStatueBlockEntity coppergolemstatueblockentity) {
            return coppergolemstatueblockentity.getItem(this.asItem().getDefaultInstance(), (CopperGolemStatueBlock.a) iblockdata.getValue(CopperGolemStatueBlock.POSE));
        } else {
            return super.getCloneItemStack(iworldreader, blockposition, iblockdata, flag);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        worldserver.updateNeighbourForOutputSignal(blockposition, iblockdata.getBlock());
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(CopperGolemStatueBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(CopperGolemStatueBlock.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    public static enum a implements INamable {

        STANDING("standing"), SITTING("sitting"), RUNNING("running"), STAR("star");

        public static final IntFunction<CopperGolemStatueBlock.a> BY_ID = ByIdMap.<CopperGolemStatueBlock.a>continuous(Enum::ordinal, values(), ByIdMap.a.ZERO);
        public static final Codec<CopperGolemStatueBlock.a> CODEC = INamable.<CopperGolemStatueBlock.a>fromEnum(CopperGolemStatueBlock.a::values);
        private final String name;

        private a(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public CopperGolemStatueBlock.a getNextPose() {
            return (CopperGolemStatueBlock.a) CopperGolemStatueBlock.a.BY_ID.apply(this.ordinal() + 1);
        }
    }
}
