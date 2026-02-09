package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.SideChainPart;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public class ShelfBlock extends BlockTileEntity implements SelectableSlotContainer, SideChainPartBlock, IBlockWaterlogged {

    public static final MapCodec<ShelfBlock> CODEC = simpleCodec(ShelfBlock::new);
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;
    public static final BlockStateEnum<EnumDirection> FACING = BlockProperties.HORIZONTAL_FACING;
    public static final BlockStateEnum<SideChainPart> SIDE_CHAIN_PART = BlockProperties.SIDE_CHAIN_PART;
    public static final BlockStateBoolean WATERLOGGED = BlockProperties.WATERLOGGED;
    private static final Map<EnumDirection, VoxelShape> SHAPES = VoxelShapes.rotateHorizontal(VoxelShapes.or(Block.box(0.0D, 12.0D, 11.0D, 16.0D, 16.0D, 13.0D), Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D), Block.box(0.0D, 0.0D, 11.0D, 16.0D, 4.0D, 13.0D)));

    @Override
    public MapCodec<ShelfBlock> codec() {
        return ShelfBlock.CODEC;
    }

    public ShelfBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) ((IBlockData) ((IBlockData) ((IBlockData) (this.stateDefinition.any()).setValue(ShelfBlock.FACING, EnumDirection.NORTH)).setValue(ShelfBlock.POWERED, false)).setValue(ShelfBlock.SIDE_CHAIN_PART, SideChainPart.UNCONNECTED)).setValue(ShelfBlock.WATERLOGGED, false));
    }

    @Override
    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return (VoxelShape) ShelfBlock.SHAPES.get(iblockdata.getValue(ShelfBlock.FACING));
    }

    @Override
    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        return pathmode == PathMode.WATER && iblockdata.getFluidState().is(TagsFluid.WATER);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new ShelfBlockEntity(blockposition, iblockdata);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(ShelfBlock.FACING, ShelfBlock.POWERED, ShelfBlock.SIDE_CHAIN_PART, ShelfBlock.WATERLOGGED);
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        InventoryUtils.updateNeighboursAfterDestroy(iblockdata, worldserver, blockposition);
        this.updateNeighborsAfterPoweringDown(worldserver, blockposition, iblockdata);
    }

    @Override
    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, @Nullable Orientation orientation, boolean flag) {
        if (!world.isClientSide()) {
            boolean flag1 = world.hasNeighborSignal(blockposition);

            if ((Boolean) iblockdata.getValue(ShelfBlock.POWERED) != flag1) {
                IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(ShelfBlock.POWERED, flag1);

                if (!flag1) {
                    iblockdata1 = (IBlockData) iblockdata1.setValue(ShelfBlock.SIDE_CHAIN_PART, SideChainPart.UNCONNECTED);
                }

                world.setBlock(blockposition, iblockdata1, 3);
                this.playSound(world, blockposition, flag1 ? SoundEffects.SHELF_ACTIVATE : SoundEffects.SHELF_DEACTIVATE);
                world.gameEvent(flag1 ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, blockposition, GameEvent.a.of(iblockdata1));
            }

        }
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        Fluid fluid = blockactioncontext.getLevel().getFluidState(blockactioncontext.getClickedPos());

        return (IBlockData) ((IBlockData) ((IBlockData) this.defaultBlockState().setValue(ShelfBlock.FACING, blockactioncontext.getHorizontalDirection().getOpposite())).setValue(ShelfBlock.POWERED, blockactioncontext.getLevel().hasNeighborSignal(blockactioncontext.getClickedPos()))).setValue(ShelfBlock.WATERLOGGED, fluid.getType() == FluidTypes.WATER);
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(ShelfBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(ShelfBlock.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(ShelfBlock.FACING)));
    }

    @Override
    public int getRows() {
        return 1;
    }

    @Override
    public int getColumns() {
        return 3;
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof ShelfBlockEntity shelfblockentity) {
            if (!enumhand.equals(EnumHand.OFF_HAND)) {
                OptionalInt optionalint = this.getHitSlot(movingobjectpositionblock, (EnumDirection) iblockdata.getValue(ShelfBlock.FACING));

                if (optionalint.isEmpty()) {
                    return EnumInteractionResult.PASS;
                }

                if (world.isClientSide()) {
                    return EnumInteractionResult.SUCCESS;
                }

                PlayerInventory playerinventory = entityhuman.getInventory();

                if (!(Boolean) iblockdata.getValue(ShelfBlock.POWERED)) {
                    boolean flag = swapSingleItem(itemstack, entityhuman, shelfblockentity, optionalint.getAsInt(), playerinventory);

                    if (flag) {
                        this.playSound(world, blockposition, itemstack.isEmpty() ? SoundEffects.SHELF_TAKE_ITEM : SoundEffects.SHELF_SINGLE_SWAP);
                    } else {
                        if (itemstack.isEmpty()) {
                            return EnumInteractionResult.PASS;
                        }

                        this.playSound(world, blockposition, SoundEffects.SHELF_PLACE_ITEM);
                    }

                    return EnumInteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
                }

                ItemStack itemstack1 = playerinventory.getSelectedItem();
                boolean flag1 = this.swapHotbar(world, blockposition, playerinventory);

                if (!flag1) {
                    return EnumInteractionResult.CONSUME;
                }

                this.playSound(world, blockposition, SoundEffects.SHELF_MULTI_SWAP);
                if (itemstack1 == playerinventory.getSelectedItem()) {
                    return EnumInteractionResult.SUCCESS;
                }

                return EnumInteractionResult.SUCCESS.heldItemTransformedTo(playerinventory.getSelectedItem());
            }
        }

        return EnumInteractionResult.PASS;
    }

    private static boolean swapSingleItem(ItemStack itemstack, EntityHuman entityhuman, ShelfBlockEntity shelfblockentity, int i, PlayerInventory playerinventory) {
        ItemStack itemstack1 = shelfblockentity.swapItemNoUpdate(i, itemstack);
        ItemStack itemstack2 = entityhuman.hasInfiniteMaterials() && itemstack1.isEmpty() ? itemstack.copy() : itemstack1;

        playerinventory.setItem(playerinventory.getSelectedSlot(), itemstack2);
        playerinventory.setChanged();
        shelfblockentity.setChanged(GameEvent.ITEM_INTERACT_FINISH);
        return !itemstack1.isEmpty();
    }

    private boolean swapHotbar(World world, BlockPosition blockposition, PlayerInventory playerinventory) {
        List<BlockPosition> list = this.getAllBlocksConnectedTo(world, blockposition);

        if (list.isEmpty()) {
            return false;
        } else {
            boolean flag = false;

            for (int i = 0; i < list.size(); ++i) {
                ShelfBlockEntity shelfblockentity = (ShelfBlockEntity) world.getBlockEntity((BlockPosition) list.get(i));

                if (shelfblockentity != null) {
                    for (int j = 0; j < shelfblockentity.getContainerSize(); ++j) {
                        int k = 9 - (list.size() - i) * shelfblockentity.getContainerSize() + j;

                        if (k >= 0 && k <= playerinventory.getContainerSize()) {
                            ItemStack itemstack = playerinventory.removeItemNoUpdate(k);
                            ItemStack itemstack1 = shelfblockentity.swapItemNoUpdate(j, itemstack);

                            if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
                                playerinventory.setItem(k, itemstack1);
                                flag = true;
                            }
                        }
                    }

                    playerinventory.setChanged();
                    shelfblockentity.setChanged(GameEvent.ENTITY_INTERACT);
                }
            }

            return flag;
        }
    }

    @Override
    public SideChainPart getSideChainPart(IBlockData iblockdata) {
        return (SideChainPart) iblockdata.getValue(ShelfBlock.SIDE_CHAIN_PART);
    }

    @Override
    public IBlockData setSideChainPart(IBlockData iblockdata, SideChainPart sidechainpart) {
        return (IBlockData) iblockdata.setValue(ShelfBlock.SIDE_CHAIN_PART, sidechainpart);
    }

    @Override
    public EnumDirection getFacing(IBlockData iblockdata) {
        return (EnumDirection) iblockdata.getValue(ShelfBlock.FACING);
    }

    @Override
    public boolean isConnectable(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.WOODEN_SHELVES) && iblockdata.hasProperty(ShelfBlock.POWERED) && (Boolean) iblockdata.getValue(ShelfBlock.POWERED);
    }

    @Override
    public int getMaxChainLength() {
        return 3;
    }

    @Override
    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if ((Boolean) iblockdata.getValue(ShelfBlock.POWERED)) {
            this.updateSelfAndNeighborsOnPoweringUp(world, blockposition, iblockdata, iblockdata1);
        } else {
            this.updateNeighborsAfterPoweringDown(world, blockposition, iblockdata);
        }

    }

    private void playSound(GeneratorAccess generatoraccess, BlockPosition blockposition, SoundEffect soundeffect) {
        generatoraccess.playSound((Entity) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected Fluid getFluidState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(ShelfBlock.WATERLOGGED) ? FluidTypes.WATER.getSource(false) : super.getFluidState(iblockdata);
    }

    @Override
    protected IBlockData updateShape(IBlockData iblockdata, IWorldReader iworldreader, ScheduledTickAccess scheduledtickaccess, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition blockposition1, IBlockData iblockdata1, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(ShelfBlock.WATERLOGGED)) {
            scheduledtickaccess.scheduleTick(blockposition, (FluidType) FluidTypes.WATER, FluidTypes.WATER.getTickDelay(iworldreader));
        }

        return super.updateShape(iblockdata, iworldreader, scheduledtickaccess, blockposition, enumdirection, blockposition1, iblockdata1, randomsource);
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition, EnumDirection enumdirection) {
        if (world.isClientSide()) {
            return 0;
        } else if (enumdirection != ((EnumDirection) iblockdata.getValue(ShelfBlock.FACING)).getOpposite()) {
            return 0;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof ShelfBlockEntity) {
                ShelfBlockEntity shelfblockentity = (ShelfBlockEntity) tileentity;
                int i = shelfblockentity.getItem(0).isEmpty() ? 0 : 1;
                int j = shelfblockentity.getItem(1).isEmpty() ? 0 : 1;
                int k = shelfblockentity.getItem(2).isEmpty() ? 0 : 1;

                return i | j << 1 | k << 2;
            } else {
                return 0;
            }
        }
    }
}
