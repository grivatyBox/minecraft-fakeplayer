package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends TileEntity implements ListBackedContainer {

    public static final int MAX_BOOKS_IN_STORAGE = 6;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_LAST_INTERACTED_SLOT = -1;
    private final NonNullList<ItemStack> items;
    public int lastInteractedSlot;

    public ChiseledBookShelfBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.CHISELED_BOOKSHELF, blockposition, iblockdata);
        this.items = NonNullList.<ItemStack>withSize(6, ItemStack.EMPTY);
        this.lastInteractedSlot = -1;
    }

    private void updateState(int i) {
        if (i >= 0 && i < 6) {
            this.lastInteractedSlot = i;
            IBlockData iblockdata = this.getBlockState();

            for (int j = 0; j < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++j) {
                boolean flag = !this.getItem(j).isEmpty();
                BlockStateBoolean blockstateboolean = (BlockStateBoolean) ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);

                iblockdata = (IBlockData) iblockdata.setValue(blockstateboolean, flag);
            }

            ((World) Objects.requireNonNull(this.level)).setBlock(this.worldPosition, iblockdata, 3);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.a.of(iblockdata));
        } else {
            ChiseledBookShelfBlockEntity.LOGGER.error("Expected slot 0-5, got {}", i);
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueinput) {
        super.loadAdditional(valueinput);
        this.items.clear();
        ContainerUtil.loadAllItems(valueinput, this.items);
        this.lastInteractedSlot = valueinput.getIntOr("last_interacted_slot", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput valueoutput) {
        super.saveAdditional(valueoutput);
        ContainerUtil.saveAllItems(valueoutput, this.items, true);
        valueoutput.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean acceptsItemType(ItemStack itemstack) {
        return itemstack.is(TagsItem.BOOKSHELF_BOOKS);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemstack = (ItemStack) Objects.requireNonNullElse((ItemStack) this.getItems().get(i), ItemStack.EMPTY);

        this.getItems().set(i, ItemStack.EMPTY);
        if (!itemstack.isEmpty()) {
            this.updateState(i);
        }

        return itemstack;
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        if (this.acceptsItemType(itemstack)) {
            this.getItems().set(i, itemstack);
            this.updateState(i);
        } else if (itemstack.isEmpty()) {
            this.removeItem(i, this.getMaxStackSize());
        }

    }

    @Override
    public boolean canTakeItem(IInventory iinventory, int i, ItemStack itemstack) {
        return iinventory.hasAnyMatching((itemstack1) -> {
            return itemstack1.isEmpty() ? true : ItemStack.isSameItemSameComponents(itemstack, itemstack1) && itemstack1.getCount() + itemstack.getCount() <= iinventory.getMaxStackSize(itemstack1);
        });
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return IInventory.stillValidBlockEntity(this, entityhuman);
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        super.applyImplicitComponents(datacomponentgetter);
        ((ItemContainerContents) datacomponentgetter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyInto(this.items);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueoutput) {
        valueoutput.discard("Items");
    }
}
