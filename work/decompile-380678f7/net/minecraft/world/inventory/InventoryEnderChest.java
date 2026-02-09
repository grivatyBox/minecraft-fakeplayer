package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntityEnderChest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class InventoryEnderChest extends InventorySubcontainer {

    @Nullable
    private TileEntityEnderChest activeChest;

    public InventoryEnderChest() {
        super(27);
    }

    public void setActiveChest(TileEntityEnderChest tileentityenderchest) {
        this.activeChest = tileentityenderchest;
    }

    public boolean isActiveChest(TileEntityEnderChest tileentityenderchest) {
        return this.activeChest == tileentityenderchest;
    }

    public void fromSlots(ValueInput.a<ItemStackWithSlot> valueinput_a) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, ItemStack.EMPTY);
        }

        for (ItemStackWithSlot itemstackwithslot : valueinput_a) {
            if (itemstackwithslot.isValidInContainer(this.getContainerSize())) {
                this.setItem(itemstackwithslot.slot(), itemstackwithslot.stack());
            }
        }

    }

    public void storeAsSlots(ValueOutput.a<ItemStackWithSlot> valueoutput_a) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);

            if (!itemstack.isEmpty()) {
                valueoutput_a.add(new ItemStackWithSlot(i, itemstack));
            }
        }

    }

    @Override
    public boolean stillValid(EntityHuman entityhuman) {
        return this.activeChest != null && !this.activeChest.stillValid(entityhuman) ? false : super.stillValid(entityhuman);
    }

    @Override
    public void startOpen(EntityHuman entityhuman) {
        if (this.activeChest != null) {
            this.activeChest.startOpen(entityhuman);
        }

        super.startOpen(entityhuman);
    }

    @Override
    public void stopOpen(EntityHuman entityhuman) {
        if (this.activeChest != null) {
            this.activeChest.stopOpen(entityhuman);
        }

        super.stopOpen(entityhuman);
        this.activeChest = null;
    }
}
