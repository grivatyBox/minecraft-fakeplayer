package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityHopper;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class BlockHopper extends BlockTileEntity {

    public static final MapCodec<BlockHopper> CODEC = simpleCodec(BlockHopper::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.FACING_HOPPER;
    public static final BlockStateBoolean ENABLED = BlockProperties.ENABLED;
    private final Function<IBlockData, VoxelShape> shapes;
    private final Map<EnumDirection, VoxelShape> interactionShapes;

    @Override
    public MapCodec<BlockHopper> codec() {
        return BlockHopper.CODEC;
    }

    public BlockHopper(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(BlockHopper.FACING, EnumDirection.DOWN)).setValue(BlockHopper.ENABLED, true));
        VoxelShape voxelshape = Block.column(12.0D, 11.0D, 16.0D);

        this.shapes = this.makeShapes(voxelshape);
        this.interactionShapes = ImmutableMap.builderWithExpectedSize(5).putAll(VoxelShapes.rotateHorizontal(VoxelShapes.or(voxelshape, Block.boxZ(4.0D, 8.0D, 10.0D, 0.0D, 4.0D)))).put(EnumDirection.DOWN, voxelshape).build();
    }

    private Function<IBlockData, VoxelShape> makeShapes(VoxelShape voxelshape) {
        VoxelShape voxelshape1 = VoxelShapes.or(Block.column(16.0D, 10.0D, 16.0D), Block.column(8.0D, 4.0D, 10.0D));
        VoxelShape voxelshape2 = VoxelShapes.join(voxelshape1, voxelshape, OperatorBoolean.ONLY_FIRST);
        Map<EnumDirection, VoxelShape> map = VoxelShapes.rotateAll(Block.boxZ(4.0D, 4.0D, 8.0D, 0.0D, 8.0D), (new Vec3D(8.0D, 6.0D, 8.0D)).scale(0.0625D));

        return this.getShapeForEachState((iblockdata) -> {
            return VoxelShapes.or(voxelshape2, VoxelShapes.join((VoxelShape) map.get(iblockdata.getValue(BlockHopper.FACING)), VoxelShapes.block(), OperatorBoolean.AND));
        }, new IBlockState[]{BlockHopper.ENABLED});
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) this.shapes.apply(iblockdata);
    }

    @Override
    protected VoxelShape getInteractionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return (VoxelShape) this.interactionShapes.get(iblockdata.getValue(BlockHopper.FACING));
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        EnumDirection enumdirection = blockactioncontext.getClickedFace().getOpposite();

        return (IBlockData) ((IBlockData) this.defaultBlockState().setValue(BlockHopper.FACING, enumdirection.getAxis() == EnumDirection.EnumAxis.Y ? EnumDirection.DOWN : enumdirection)).setValue(BlockHopper.ENABLED, true);
    }

    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new TileEntityHopper(blockposition, iblockdata);
    }

    @Nullable
    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, IBlockData iblockdata, TileEntityTypes<T> tileentitytypes) {
        return world.isClientSide ? null : createTickerHelper(tileentitytypes, TileEntityTypes.HOPPER, TileEntityHopper::pushItemsTick);
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (!iblockdata1.is(iblockdata.getBlock())) {
            this.checkPoweredState(world, blockposition, iblockdata);
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        if (!world.isClientSide) {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityHopper) {
                TileEntityHopper tileentityhopper = (TileEntityHopper) tileentity;

                entityhuman.openMenu(tileentityhopper);
                entityhuman.awardStat(StatisticList.INSPECT_HOPPER);
            }
        }

        return EnumInteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        this.checkPoweredState(world, blockposition, iblockdata);
    }

    private void checkPoweredState(World world, BlockPosition blockposition, IBlockData iblockdata) {
        boolean flag = !world.hasNeighborSignal(blockposition);

        if (flag != (Boolean) iblockdata.getValue(BlockHopper.ENABLED)) {
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockHopper.ENABLED, flag), 2);
        }

    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        InventoryUtils.updateNeighboursAfterDestroy(iblockdata, worldserver, blockposition);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(blockposition));
    }

    @Override
    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(BlockHopper.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(BlockHopper.FACING)));
    }

    @Override
    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(BlockHopper.FACING)));
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockHopper.FACING, BlockHopper.ENABLED);
    }

    @Override
    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity, InsideBlockEffectApplier insideblockeffectapplier) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityHopper) {
            TileEntityHopper.entityInside(world, blockposition, iblockdata, entity, (TileEntityHopper) tileentity);
        }

    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return false;
    }
}
