package net.minecraft.world.level.block.entity;

import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.item.ItemStack;

public interface ListBackedContainer extends IInventory {

    NonNullList<ItemStack> getItems();

    default int count() {
        return (int) this.getItems().stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    default int getContainerSize() {
        return this.getItems().size();
    }

    @Override
    default void clearContent() {
        this.getItems().clear();
    }

    @Override
    default boolean isEmpty() {
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    default ItemStack getItem(int i) {
        return (ItemStack) this.getItems().get(i);
    }

    @Override
    default ItemStack removeItem(int i, int j) {
        ItemStack itemstack = ContainerUtil.removeItem(this.getItems(), i, j);

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    default ItemStack removeItemNoUpdate(int i) {
        return ContainerUtil.removeItem(this.getItems(), i, this.getMaxStackSize());
    }

    @Override
    default boolean canPlaceItem(int i, ItemStack itemstack) {
        return this.acceptsItemType(itemstack) && (this.getItem(i).isEmpty() || this.getItem(i).getCount() < this.getMaxStackSize(itemstack));
    }

    default boolean acceptsItemType(ItemStack itemstack) {
        return true;
    }

    @Override
    default void setItem(int i, ItemStack itemstack) {
        this.setItemNoUpdate(i, itemstack);
        this.setChanged();
    }

    default void setItemNoUpdate(int i, ItemStack itemstack) {
        this.getItems().set(i, itemstack);
        itemstack.limitSize(this.getMaxStackSize(itemstack));
    }
}
