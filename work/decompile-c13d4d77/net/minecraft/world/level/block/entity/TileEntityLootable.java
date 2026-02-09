package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;

public abstract class TileEntityLootable extends TileEntityContainer implements RandomizableContainer {

    @Nullable
    public MinecraftKey lootTable;
    public long lootTableSeed;

    protected TileEntityLootable(TileEntityTypes<?> tileentitytypes, BlockPosition blockposition, IBlockData iblockdata) {
        super(tileentitytypes, blockposition, iblockdata);
    }

    @Nullable
    @Override
    public MinecraftKey getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable MinecraftKey minecraftkey) {
        this.lootTable = minecraftkey;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long i) {
        this.lootTableSeed = i;
    }

    @Override
    public boolean isEmpty() {
        this.unpackLootTable((EntityHuman) null);
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int i) {
        this.unpackLootTable((EntityHuman) null);
        return (ItemStack) this.getItems().get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        this.unpackLootTable((EntityHuman) null);
        ItemStack itemstack = ContainerUtil.removeItem(this.getItems(), i, j);

        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        this.unpackLootTable((EntityHuman) null);
        return ContainerUtil.takeItem(this.getItems(), i);
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.unpackLootTable((EntityHuman) null);
        this.getItems().set(i, itemstack);
        if (itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return IInventory.stillValidBlockEntity(this, entityhuman);
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> nonnulllist);

    @Override
    public boolean canOpen(EntityHuman entityhuman) {
        return super.canOpen(entityhuman) && (this.lootTable == null || !entityhuman.isSpectator());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerinventory, EntityHuman entityhuman) {
        if (this.canOpen(entityhuman)) {
            this.unpackLootTable(playerinventory.player);
            return this.createMenu(i, playerinventory);
        } else {
            return null;
        }
    }
}
