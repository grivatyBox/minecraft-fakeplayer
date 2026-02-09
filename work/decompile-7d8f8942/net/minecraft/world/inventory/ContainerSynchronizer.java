package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface ContainerSynchronizer {

    void sendInitialData(Container container, List<ItemStack> list, ItemStack itemstack, int[] aint);

    void sendSlotChange(Container container, int i, ItemStack itemstack);

    void sendCarriedChange(Container container, ItemStack itemstack);

    void sendDataChange(Container container, int i, int j);

    RemoteSlot createSlot();
}
