package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ContainerUtil {

    public static final String TAG_ITEMS = "Items";

    public ContainerUtil() {}

    public static ItemStack removeItem(List<ItemStack> list, int i, int j) {
        return i >= 0 && i < list.size() && !((ItemStack) list.get(i)).isEmpty() && j > 0 ? ((ItemStack) list.get(i)).split(j) : ItemStack.EMPTY;
    }

    public static ItemStack takeItem(List<ItemStack> list, int i) {
        return i >= 0 && i < list.size() ? (ItemStack) list.set(i, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    public static void saveAllItems(ValueOutput valueoutput, NonNullList<ItemStack> nonnulllist) {
        saveAllItems(valueoutput, nonnulllist, true);
    }

    public static void saveAllItems(ValueOutput valueoutput, NonNullList<ItemStack> nonnulllist, boolean flag) {
        ValueOutput.a<ItemStackWithSlot> valueoutput_a = valueoutput.<ItemStackWithSlot>list("Items", ItemStackWithSlot.CODEC);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = nonnulllist.get(i);

            if (!itemstack.isEmpty()) {
                valueoutput_a.add(new ItemStackWithSlot(i, itemstack));
            }
        }

        if (valueoutput_a.isEmpty() && !flag) {
            valueoutput.discard("Items");
        }

    }

    public static void loadAllItems(ValueInput valueinput, NonNullList<ItemStack> nonnulllist) {
        for (ItemStackWithSlot itemstackwithslot : valueinput.listOrEmpty("Items", ItemStackWithSlot.CODEC)) {
            if (itemstackwithslot.isValidInContainer(nonnulllist.size())) {
                nonnulllist.set(itemstackwithslot.slot(), itemstackwithslot.stack());
            }
        }

    }

    public static int clearOrCountMatchingItems(IInventory iinventory, Predicate<ItemStack> predicate, int i, boolean flag) {
        int j = 0;

        for (int k = 0; k < iinventory.getContainerSize(); ++k) {
            ItemStack itemstack = iinventory.getItem(k);
            int l = clearOrCountMatchingItems(itemstack, predicate, i - j, flag);

            if (l > 0 && !flag && itemstack.isEmpty()) {
                iinventory.setItem(k, ItemStack.EMPTY);
            }

            j += l;
        }

        return j;
    }

    public static int clearOrCountMatchingItems(ItemStack itemstack, Predicate<ItemStack> predicate, int i, boolean flag) {
        if (!itemstack.isEmpty() && predicate.test(itemstack)) {
            if (flag) {
                return itemstack.getCount();
            } else {
                int j = i < 0 ? itemstack.getCount() : Math.min(i, itemstack.getCount());

                itemstack.shrink(j);
                return j;
            }
        } else {
            return 0;
        }
    }
}
