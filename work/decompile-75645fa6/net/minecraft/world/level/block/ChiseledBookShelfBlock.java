package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPositionBlock;

public class ChiseledBookShelfBlock extends BlockTileEntity implements SelectableSlotContainer {

    public static final MapCodec<ChiseledBookShelfBlock> CODEC = simpleCodec(ChiseledBookShelfBlock::new);
    public static final BlockStateEnum<EnumDirection> FACING = BlockFacingHorizontal.FACING;
    public static final BlockStateBoolean SLOT_0_OCCUPIED = BlockProperties.SLOT_0_OCCUPIED;
    public static final BlockStateBoolean SLOT_1_OCCUPIED = BlockProperties.SLOT_1_OCCUPIED;
    public static final BlockStateBoolean SLOT_2_OCCUPIED = BlockProperties.SLOT_2_OCCUPIED;
    public static final BlockStateBoolean SLOT_3_OCCUPIED = BlockProperties.SLOT_3_OCCUPIED;
    public static final BlockStateBoolean SLOT_4_OCCUPIED = BlockProperties.SLOT_4_OCCUPIED;
    public static final BlockStateBoolean SLOT_5_OCCUPIED = BlockProperties.SLOT_5_OCCUPIED;
    private static final int MAX_BOOKS_IN_STORAGE = 6;
    private static final int BOOKS_PER_ROW = 3;
    public static final List<BlockStateBoolean> SLOT_OCCUPIED_PROPERTIES = List.of(ChiseledBookShelfBlock.SLOT_0_OCCUPIED, ChiseledBookShelfBlock.SLOT_1_OCCUPIED, ChiseledBookShelfBlock.SLOT_2_OCCUPIED, ChiseledBookShelfBlock.SLOT_3_OCCUPIED, ChiseledBookShelfBlock.SLOT_4_OCCUPIED, ChiseledBookShelfBlock.SLOT_5_OCCUPIED);

    @Override
    public MapCodec<ChiseledBookShelfBlock> codec() {
        return ChiseledBookShelfBlock.CODEC;
    }

    @Override
    public int getRows() {
        return 2;
    }

    @Override
    public int getColumns() {
        return 3;
    }

    public ChiseledBookShelfBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        IBlockData iblockdata = (IBlockData) (this.stateDefinition.any()).setValue(ChiseledBookShelfBlock.FACING, EnumDirection.NORTH);

        for (BlockStateBoolean blockstateboolean : ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES) {
            iblockdata = (IBlockData) iblockdata.setValue(blockstateboolean, false);
        }

        this.registerDefaultState(iblockdata);
    }

    @Override
    protected EnumInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity) {
            if (!itemstack.is(TagsItem.BOOKSHELF_BOOKS)) {
                return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
            } else {
                OptionalInt optionalint = this.getHitSlot(movingobjectpositionblock, (EnumDirection) iblockdata.getValue(ChiseledBookShelfBlock.FACING));

                if (optionalint.isEmpty()) {
                    return EnumInteractionResult.PASS;
                } else if ((Boolean) iblockdata.getValue((IBlockState) ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(optionalint.getAsInt()))) {
                    return EnumInteractionResult.TRY_WITH_EMPTY_HAND;
                } else {
                    addBook(world, blockposition, entityhuman, chiseledbookshelfblockentity, itemstack, optionalint.getAsInt());
                    return EnumInteractionResult.SUCCESS;
                }
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    @Override
    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        TileEntity tileentity = world.getBlockEntity(blockposition);

        if (tileentity instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity) {
            OptionalInt optionalint = this.getHitSlot(movingobjectpositionblock, (EnumDirection) iblockdata.getValue(ChiseledBookShelfBlock.FACING));

            if (optionalint.isEmpty()) {
                return EnumInteractionResult.PASS;
            } else if (!(Boolean) iblockdata.getValue((IBlockState) ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(optionalint.getAsInt()))) {
                return EnumInteractionResult.CONSUME;
            } else {
                removeBook(world, blockposition, entityhuman, chiseledbookshelfblockentity, optionalint.getAsInt());
                return EnumInteractionResult.SUCCESS;
            }
        } else {
            return EnumInteractionResult.PASS;
        }
    }

    private static void addBook(World world, BlockPosition blockposition, EntityHuman entityhuman, ChiseledBookShelfBlockEntity chiseledbookshelfblockentity, ItemStack itemstack, int i) {
        if (!world.isClientSide()) {
            entityhuman.awardStat(StatisticList.ITEM_USED.get(itemstack.getItem()));
            SoundEffect soundeffect = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEffects.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEffects.CHISELED_BOOKSHELF_INSERT;

            chiseledbookshelfblockentity.setItem(i, itemstack.consumeAndReturn(1, entityhuman));
            world.playSound((Entity) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static void removeBook(World world, BlockPosition blockposition, EntityHuman entityhuman, ChiseledBookShelfBlockEntity chiseledbookshelfblockentity, int i) {
        if (!world.isClientSide()) {
            ItemStack itemstack = chiseledbookshelfblockentity.removeItem(i, 1);
            SoundEffect soundeffect = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEffects.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEffects.CHISELED_BOOKSHELF_PICKUP;

            world.playSound((Entity) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!entityhuman.getInventory().add(itemstack)) {
                entityhuman.drop(itemstack, false);
            }

            world.gameEvent(entityhuman, (Holder) GameEvent.BLOCK_CHANGE, blockposition);
        }
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        return new ChiseledBookShelfBlockEntity(blockposition, iblockdata);
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(ChiseledBookShelfBlock.FACING);
        List list = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES;

        Objects.requireNonNull(blockstatelist_a);
        list.forEach((iblockstate) -> {
            blockstatelist_a.add(iblockstate);
        });
    }

    @Override
    protected void affectNeighborsAfterRemoval(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, boolean flag) {
        InventoryUtils.updateNeighboursAfterDestroy(iblockdata, worldserver, blockposition);
    }

    @Override
    public IBlockData getStateForPlacement(BlockActionContext blockactioncontext) {
        return (IBlockData) this.defaultBlockState().setValue(ChiseledBookShelfBlock.FACING, blockactioncontext.getHorizontalDirection().getOpposite());
    }

    @Override
    public IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return (IBlockData) iblockdata.setValue(ChiseledBookShelfBlock.FACING, enumblockrotation.rotate((EnumDirection) iblockdata.getValue(ChiseledBookShelfBlock.FACING)));
    }

    @Override
    public IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata.rotate(enumblockmirror.getRotation((EnumDirection) iblockdata.getValue(ChiseledBookShelfBlock.FACING)));
    }

    @Override
    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition, EnumDirection enumdirection) {
        if (world.isClientSide()) {
            return 0;
        } else {
            TileEntity tileentity = world.getBlockEntity(blockposition);

            if (tileentity instanceof ChiseledBookShelfBlockEntity) {
                ChiseledBookShelfBlockEntity chiseledbookshelfblockentity = (ChiseledBookShelfBlockEntity) tileentity;

                return chiseledbookshelfblockentity.getLastInteractedSlot() + 1;
            } else {
                return 0;
            }
        }
    }
}
