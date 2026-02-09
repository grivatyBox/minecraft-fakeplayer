package net.minecraft.world.entity.npc;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface InventoryCarrier {

    String TAG_INVENTORY = "Inventory";

    InventorySubcontainer getInventory();

    static void pickUpItem(WorldServer worldserver, EntityInsentient entityinsentient, InventoryCarrier inventorycarrier, EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItem();

        if (entityinsentient.wantsToPickUp(worldserver, itemstack)) {
            InventorySubcontainer inventorysubcontainer = inventorycarrier.getInventory();
            boolean flag = inventorysubcontainer.canAddItem(itemstack);

            if (!flag) {
                return;
            }

            entityinsentient.onItemPickup(entityitem);
            int i = itemstack.getCount();
            ItemStack itemstack1 = inventorysubcontainer.addItem(itemstack);

            entityinsentient.take(entityitem, i - itemstack1.getCount());
            if (itemstack1.isEmpty()) {
                entityitem.discard();
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }

    }

    default void readInventoryFromTag(ValueInput valueinput) {
        valueinput.list("Inventory", ItemStack.CODEC).ifPresent((valueinput_a) -> {
            this.getInventory().fromItemList(valueinput_a);
        });
    }

    default void writeInventoryToTag(ValueOutput valueoutput) {
        this.getInventory().storeAsItemList(valueoutput.list("Inventory", ItemStack.CODEC));
    }
}
