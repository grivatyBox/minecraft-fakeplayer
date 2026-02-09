package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ItemAttributeModifiers(List<ItemAttributeModifiers.b> modifiers) {

    public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of());
    public static final Codec<ItemAttributeModifiers> CODEC = ItemAttributeModifiers.b.CODEC.listOf().xmap(ItemAttributeModifiers::new, ItemAttributeModifiers::modifiers);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(ItemAttributeModifiers.b.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemAttributeModifiers::modifiers, ItemAttributeModifiers::new);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = (DecimalFormat) SystemUtils.make(new DecimalFormat("#.##"), (decimalformat) -> {
        decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    public static ItemAttributeModifiers.a builder() {
        return new ItemAttributeModifiers.a();
    }

    public ItemAttributeModifiers withModifierAdded(Holder<AttributeBase> holder, AttributeModifier attributemodifier, EquipmentSlotGroup equipmentslotgroup) {
        ImmutableList.Builder<ItemAttributeModifiers.b> immutablelist_builder = ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);

        for (ItemAttributeModifiers.b itemattributemodifiers_b : this.modifiers) {
            if (!itemattributemodifiers_b.matches(holder, attributemodifier.id())) {
                immutablelist_builder.add(itemattributemodifiers_b);
            }
        }

        immutablelist_builder.add(new ItemAttributeModifiers.b(holder, attributemodifier, equipmentslotgroup));
        return new ItemAttributeModifiers(immutablelist_builder.build());
    }

    public void forEach(EquipmentSlotGroup equipmentslotgroup, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        for (ItemAttributeModifiers.b itemattributemodifiers_b : this.modifiers) {
            if (itemattributemodifiers_b.slot.equals(equipmentslotgroup)) {
                biconsumer.accept(itemattributemodifiers_b.attribute, itemattributemodifiers_b.modifier);
            }
        }

    }

    public void forEach(EnumItemSlot enumitemslot, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        for (ItemAttributeModifiers.b itemattributemodifiers_b : this.modifiers) {
            if (itemattributemodifiers_b.slot.test(enumitemslot)) {
                biconsumer.accept(itemattributemodifiers_b.attribute, itemattributemodifiers_b.modifier);
            }
        }

    }

    public double compute(double d0, EnumItemSlot enumitemslot) {
        double d1 = d0;

        for (ItemAttributeModifiers.b itemattributemodifiers_b : this.modifiers) {
            if (itemattributemodifiers_b.slot.test(enumitemslot)) {
                double d2 = itemattributemodifiers_b.modifier.amount();
                double d3;

                switch (itemattributemodifiers_b.modifier.operation()) {
                    case ADD_VALUE:
                        d3 = d2;
                        break;
                    case ADD_MULTIPLIED_BASE:
                        d3 = d2 * d0;
                        break;
                    case ADD_MULTIPLIED_TOTAL:
                        d3 = d2 * d1;
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }

                d1 += d3;
            }
        }

        return d1;
    }

    public static record b(Holder<AttributeBase> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {

        public static final Codec<ItemAttributeModifiers.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(AttributeBase.CODEC.fieldOf("type").forGetter(ItemAttributeModifiers.b::attribute), AttributeModifier.MAP_CODEC.forGetter(ItemAttributeModifiers.b::modifier), EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(ItemAttributeModifiers.b::slot)).apply(instance, ItemAttributeModifiers.b::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.b> STREAM_CODEC = StreamCodec.composite(AttributeBase.STREAM_CODEC, ItemAttributeModifiers.b::attribute, AttributeModifier.STREAM_CODEC, ItemAttributeModifiers.b::modifier, EquipmentSlotGroup.STREAM_CODEC, ItemAttributeModifiers.b::slot, ItemAttributeModifiers.b::new);

        public boolean matches(Holder<AttributeBase> holder, MinecraftKey minecraftkey) {
            return holder.equals(this.attribute) && this.modifier.is(minecraftkey);
        }
    }

    public static class a {

        private final ImmutableList.Builder<ItemAttributeModifiers.b> entries = ImmutableList.builder();

        a() {}

        public ItemAttributeModifiers.a add(Holder<AttributeBase> holder, AttributeModifier attributemodifier, EquipmentSlotGroup equipmentslotgroup) {
            this.entries.add(new ItemAttributeModifiers.b(holder, attributemodifier, equipmentslotgroup));
            return this;
        }

        public ItemAttributeModifiers build() {
            return new ItemAttributeModifiers(this.entries.build());
        }
    }
}
