package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntityEnderChest;

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

    @Override
    public void fromTag(NBTTagList nbttaglist, HolderLookup.a holderlookup_a) {
        for (int i = 0; i < this.getContainerSize(); ++i) {
            this.setItem(i, ItemStack.EMPTY);
        }

        for (int j = 0; j < nbttaglist.size(); ++j) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundOrEmpty(j);
            int k = nbttagcompound.getByteOr("Slot", (byte) 0) & 255;

            if (k >= 0 && k < this.getContainerSize()) {
                this.setItem(k, (ItemStack) ItemStack.parse(holderlookup_a, nbttagcompound).orElse(ItemStack.EMPTY));
            }
        }

    }

    @Override
    public NBTTagList createTag(HolderLookup.a holderlookup_a) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.getContainerSize(); ++i) {
            ItemStack itemstack = this.getItem(i);

            if (!itemstack.isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.putByte("Slot", (byte) i);
                nbttaglist.add(itemstack.save(holderlookup_a, nbttagcompound));
            }
        }

        return nbttaglist;
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
