package net.minecraft.world.entity.player;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;

public class PlayerEquipment extends EntityEquipment {

    private final EntityHuman player;

    public PlayerEquipment(EntityHuman entityhuman) {
        this.player = entityhuman;
    }

    @Override
    public ItemStack set(EnumItemSlot enumitemslot, ItemStack itemstack) {
        return enumitemslot == EnumItemSlot.MAINHAND ? this.player.getInventory().setSelectedItem(itemstack) : super.set(enumitemslot, itemstack);
    }

    @Override
    public ItemStack get(EnumItemSlot enumitemslot) {
        return enumitemslot == EnumItemSlot.MAINHAND ? this.player.getInventory().getSelectedItem() : super.get(enumitemslot);
    }

    @Override
    public boolean isEmpty() {
        return this.player.getInventory().getSelectedItem().isEmpty() && super.isEmpty();
    }
}
