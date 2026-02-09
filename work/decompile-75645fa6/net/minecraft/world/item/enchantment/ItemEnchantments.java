package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ItemEnchantments implements TooltipProvider {

    public static final ItemEnchantments EMPTY = new ItemEnchantments(new Object2IntOpenHashMap());
    private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(1, 255);
    public static final Codec<ItemEnchantments> CODEC = Codec.unboundedMap(Enchantment.CODEC, ItemEnchantments.LEVEL_CODEC).xmap((map) -> {
        return new ItemEnchantments(new Object2IntOpenHashMap(map));
    }, (itemenchantments) -> {
        return itemenchantments.enchantments;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantments> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(Object2IntOpenHashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT), (itemenchantments) -> {
        return itemenchantments.enchantments;
    }, ItemEnchantments::new);
    final Object2IntOpenHashMap<Holder<Enchantment>> enchantments;

    ItemEnchantments(Object2IntOpenHashMap<Holder<Enchantment>> object2intopenhashmap) {
        this.enchantments = object2intopenhashmap;
        ObjectIterator objectiterator = object2intopenhashmap.object2IntEntrySet().iterator();

        while (objectiterator.hasNext()) {
            Object2IntMap.Entry<Holder<Enchantment>> object2intmap_entry = (Entry) objectiterator.next();
            int i = object2intmap_entry.getIntValue();

            if (i < 0 || i > 255) {
                String s = String.valueOf(object2intmap_entry.getKey());

                throw new IllegalArgumentException("Enchantment " + s + " has invalid level " + i);
            }
        }

    }

    public int getLevel(Holder<Enchantment> holder) {
        return this.enchantments.getInt(holder);
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        HolderLookup.a holderlookup_a = item_b.registries();
        HolderSet<Enchantment> holderset = getTagOrEmpty(holderlookup_a, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

        for (Holder<Enchantment> holder : holderset) {
            int i = this.enchantments.getInt(holder);

            if (i > 0) {
                consumer.accept(Enchantment.getFullname(holder, i));
            }
        }

        ObjectIterator objectiterator = this.enchantments.object2IntEntrySet().iterator();

        while (objectiterator.hasNext()) {
            Object2IntMap.Entry<Holder<Enchantment>> object2intmap_entry = (Entry) objectiterator.next();
            Holder<Enchantment> holder1 = (Holder) object2intmap_entry.getKey();

            if (!holderset.contains(holder1)) {
                consumer.accept(Enchantment.getFullname((Holder) object2intmap_entry.getKey(), object2intmap_entry.getIntValue()));
            }
        }

    }

    private static <T> HolderSet<T> getTagOrEmpty(@Nullable HolderLookup.a holderlookup_a, ResourceKey<IRegistry<T>> resourcekey, TagKey<T> tagkey) {
        if (holderlookup_a != null) {
            Optional<HolderSet.Named<T>> optional = holderlookup_a.lookupOrThrow(resourcekey).get(tagkey);

            if (optional.isPresent()) {
                return (HolderSet) optional.get();
            }
        }

        return HolderSet.direct();
    }

    public Set<Holder<Enchantment>> keySet() {
        return Collections.unmodifiableSet(this.enchantments.keySet());
    }

    public Set<Object2IntMap.Entry<Holder<Enchantment>>> entrySet() {
        return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
    }

    public int size() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof ItemEnchantments) {
            ItemEnchantments itemenchantments = (ItemEnchantments) object;

            return this.enchantments.equals(itemenchantments.enchantments);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.enchantments.hashCode();
    }

    public String toString() {
        return "ItemEnchantments{enchantments=" + String.valueOf(this.enchantments) + "}";
    }

    public static class a {

        private final Object2IntOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap();

        public a(ItemEnchantments itemenchantments) {
            this.enchantments.putAll(itemenchantments.enchantments);
        }

        public void set(Holder<Enchantment> holder, int i) {
            if (i <= 0) {
                this.enchantments.removeInt(holder);
            } else {
                this.enchantments.put(holder, Math.min(i, 255));
            }

        }

        public void upgrade(Holder<Enchantment> holder, int i) {
            if (i > 0) {
                this.enchantments.merge(holder, Math.min(i, 255), Integer::max);
            }

        }

        public void removeIf(Predicate<Holder<Enchantment>> predicate) {
            this.enchantments.keySet().removeIf(predicate);
        }

        public int getLevel(Holder<Enchantment> holder) {
            return this.enchantments.getOrDefault(holder, 0);
        }

        public Set<Holder<Enchantment>> keySet() {
            return this.enchantments.keySet();
        }

        public ItemEnchantments toImmutable() {
            return new ItemEnchantments(this.enchantments);
        }
    }
}
