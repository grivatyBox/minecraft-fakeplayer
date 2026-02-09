package net.minecraft.world.item.component;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ItemContainerContents implements TooltipProvider {

    private static final int NO_SLOT = -1;
    private static final int MAX_SIZE = 256;
    public static final ItemContainerContents EMPTY = new ItemContainerContents(NonNullList.create());
    public static final Codec<ItemContainerContents> CODEC = ItemContainerContents.a.CODEC.sizeLimitedListOf(256).xmap(ItemContainerContents::fromSlots, ItemContainerContents::asSlots);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemContainerContents> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(256)).map(ItemContainerContents::new, (itemcontainercontents) -> {
        return itemcontainercontents.items;
    });
    private final NonNullList<ItemStack> items;
    private final int hashCode;

    private ItemContainerContents(NonNullList<ItemStack> nonnulllist) {
        if (nonnulllist.size() > 256) {
            throw new IllegalArgumentException("Got " + nonnulllist.size() + " items, but maximum is 256");
        } else {
            this.items = nonnulllist;
            this.hashCode = ItemStack.hashStackList(nonnulllist);
        }
    }

    private ItemContainerContents(int i) {
        this(NonNullList.withSize(i, ItemStack.EMPTY));
    }

    private ItemContainerContents(List<ItemStack> list) {
        this(list.size());

        for (int i = 0; i < list.size(); ++i) {
            this.items.set(i, (ItemStack) list.get(i));
        }

    }

    private static ItemContainerContents fromSlots(List<ItemContainerContents.a> list) {
        OptionalInt optionalint = list.stream().mapToInt(ItemContainerContents.a::index).max();

        if (optionalint.isEmpty()) {
            return ItemContainerContents.EMPTY;
        } else {
            ItemContainerContents itemcontainercontents = new ItemContainerContents(optionalint.getAsInt() + 1);

            for (ItemContainerContents.a itemcontainercontents_a : list) {
                itemcontainercontents.items.set(itemcontainercontents_a.index(), itemcontainercontents_a.item());
            }

            return itemcontainercontents;
        }
    }

    public static ItemContainerContents fromItems(List<ItemStack> list) {
        int i = findLastNonEmptySlot(list);

        if (i == -1) {
            return ItemContainerContents.EMPTY;
        } else {
            ItemContainerContents itemcontainercontents = new ItemContainerContents(i + 1);

            for (int j = 0; j <= i; ++j) {
                itemcontainercontents.items.set(j, ((ItemStack) list.get(j)).copy());
            }

            return itemcontainercontents;
        }
    }

    private static int findLastNonEmptySlot(List<ItemStack> list) {
        for (int i = list.size() - 1; i >= 0; --i) {
            if (!((ItemStack) list.get(i)).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private List<ItemContainerContents.a> asSlots() {
        List<ItemContainerContents.a> list = new ArrayList();

        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack = this.items.get(i);

            if (!itemstack.isEmpty()) {
                list.add(new ItemContainerContents.a(i, itemstack));
            }
        }

        return list;
    }

    public void copyInto(NonNullList<ItemStack> nonnulllist) {
        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = i < this.items.size() ? (ItemStack) this.items.get(i) : ItemStack.EMPTY;

            nonnulllist.set(i, itemstack.copy());
        }

    }

    public ItemStack copyOne() {
        return this.items.isEmpty() ? ItemStack.EMPTY : ((ItemStack) this.items.get(0)).copy();
    }

    public Stream<ItemStack> stream() {
        return this.items.stream().map(ItemStack::copy);
    }

    public Stream<ItemStack> nonEmptyStream() {
        return this.items.stream().filter((itemstack) -> {
            return !itemstack.isEmpty();
        }).map(ItemStack::copy);
    }

    public Iterable<ItemStack> nonEmptyItems() {
        return Iterables.filter(this.items, (itemstack) -> {
            return !itemstack.isEmpty();
        });
    }

    public Iterable<ItemStack> nonEmptyItemsCopy() {
        return Iterables.transform(this.nonEmptyItems(), ItemStack::copy);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof ItemContainerContents) {
                ItemContainerContents itemcontainercontents = (ItemContainerContents) object;

                if (ItemStack.listMatches(this.items, itemcontainercontents.items)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        int i = 0;
        int j = 0;

        for (ItemStack itemstack : this.nonEmptyItems()) {
            ++j;
            if (i <= 4) {
                ++i;
                consumer.accept(IChatBaseComponent.translatable("item.container.item_count", itemstack.getHoverName(), itemstack.getCount()));
            }
        }

        if (j - i > 0) {
            consumer.accept(IChatBaseComponent.translatable("item.container.more_items", j - i).withStyle(EnumChatFormat.ITALIC));
        }

    }

    private static record a(int index, ItemStack item) {

        public static final Codec<ItemContainerContents.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.intRange(0, 255).fieldOf("slot").forGetter(ItemContainerContents.a::index), ItemStack.CODEC.fieldOf("item").forGetter(ItemContainerContents.a::item)).apply(instance, ItemContainerContents.a::new);
        });
    }
}
