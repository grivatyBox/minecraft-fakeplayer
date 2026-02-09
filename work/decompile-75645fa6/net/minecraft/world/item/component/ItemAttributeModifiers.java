package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.function.TriConsumer;

public record ItemAttributeModifiers(List<ItemAttributeModifiers.c> modifiers) {

    public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of());
    public static final Codec<ItemAttributeModifiers> CODEC = ItemAttributeModifiers.c.CODEC.listOf().xmap(ItemAttributeModifiers::new, ItemAttributeModifiers::modifiers);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(ItemAttributeModifiers.c.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemAttributeModifiers::modifiers, ItemAttributeModifiers::new);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = (DecimalFormat) SystemUtils.make(new DecimalFormat("#.##"), (decimalformat) -> {
        decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    public static ItemAttributeModifiers.a builder() {
        return new ItemAttributeModifiers.a();
    }

    public ItemAttributeModifiers withModifierAdded(Holder<AttributeBase> holder, AttributeModifier attributemodifier, EquipmentSlotGroup equipmentslotgroup) {
        ImmutableList.Builder<ItemAttributeModifiers.c> immutablelist_builder = ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);

        for (ItemAttributeModifiers.c itemattributemodifiers_c : this.modifiers) {
            if (!itemattributemodifiers_c.matches(holder, attributemodifier.id())) {
                immutablelist_builder.add(itemattributemodifiers_c);
            }
        }

        immutablelist_builder.add(new ItemAttributeModifiers.c(holder, attributemodifier, equipmentslotgroup));
        return new ItemAttributeModifiers(immutablelist_builder.build());
    }

    public void forEach(EquipmentSlotGroup equipmentslotgroup, TriConsumer<Holder<AttributeBase>, AttributeModifier, ItemAttributeModifiers.b> triconsumer) {
        for (ItemAttributeModifiers.c itemattributemodifiers_c : this.modifiers) {
            if (itemattributemodifiers_c.slot.equals(equipmentslotgroup)) {
                triconsumer.accept(itemattributemodifiers_c.attribute, itemattributemodifiers_c.modifier, itemattributemodifiers_c.display);
            }
        }

    }

    public void forEach(EquipmentSlotGroup equipmentslotgroup, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        for (ItemAttributeModifiers.c itemattributemodifiers_c : this.modifiers) {
            if (itemattributemodifiers_c.slot.equals(equipmentslotgroup)) {
                biconsumer.accept(itemattributemodifiers_c.attribute, itemattributemodifiers_c.modifier);
            }
        }

    }

    public void forEach(EnumItemSlot enumitemslot, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        for (ItemAttributeModifiers.c itemattributemodifiers_c : this.modifiers) {
            if (itemattributemodifiers_c.slot.test(enumitemslot)) {
                biconsumer.accept(itemattributemodifiers_c.attribute, itemattributemodifiers_c.modifier);
            }
        }

    }

    public double compute(double d0, EnumItemSlot enumitemslot) {
        double d1 = d0;

        for (ItemAttributeModifiers.c itemattributemodifiers_c : this.modifiers) {
            if (itemattributemodifiers_c.slot.test(enumitemslot)) {
                double d2 = itemattributemodifiers_c.modifier.amount();
                double d3;

                switch (itemattributemodifiers_c.modifier.operation()) {
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

    public interface b {

        Codec<ItemAttributeModifiers.b> CODEC = ItemAttributeModifiers.b.d.CODEC.dispatch("type", ItemAttributeModifiers.b::type, (itemattributemodifiers_b_d) -> {
            return itemattributemodifiers_b_d.codec;
        });
        StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.b> STREAM_CODEC = ItemAttributeModifiers.b.d.STREAM_CODEC.cast().dispatch(ItemAttributeModifiers.b::type, ItemAttributeModifiers.b.d::streamCodec);

        static ItemAttributeModifiers.b attributeModifiers() {
            return ItemAttributeModifiers.b.a.INSTANCE;
        }

        static ItemAttributeModifiers.b hidden() {
            return ItemAttributeModifiers.b.b.INSTANCE;
        }

        static ItemAttributeModifiers.b override(IChatBaseComponent ichatbasecomponent) {
            return new ItemAttributeModifiers.b.c(ichatbasecomponent);
        }

        ItemAttributeModifiers.b.d type();

        void apply(Consumer<IChatBaseComponent> consumer, @Nullable EntityHuman entityhuman, Holder<AttributeBase> holder, AttributeModifier attributemodifier);

        public static enum d implements INamable {

            DEFAULT("default", 0, ItemAttributeModifiers.b.a.CODEC, ItemAttributeModifiers.b.a.STREAM_CODEC), HIDDEN("hidden", 1, ItemAttributeModifiers.b.b.CODEC, ItemAttributeModifiers.b.b.STREAM_CODEC), OVERRIDE("override", 2, ItemAttributeModifiers.b.c.CODEC, ItemAttributeModifiers.b.c.STREAM_CODEC);

            static final Codec<ItemAttributeModifiers.b.d> CODEC = INamable.<ItemAttributeModifiers.b.d>fromEnum(ItemAttributeModifiers.b.d::values);
            private static final IntFunction<ItemAttributeModifiers.b.d> BY_ID = ByIdMap.<ItemAttributeModifiers.b.d>continuous(ItemAttributeModifiers.b.d::id, values(), ByIdMap.a.ZERO);
            static final StreamCodec<ByteBuf, ItemAttributeModifiers.b.d> STREAM_CODEC = ByteBufCodecs.idMapper(ItemAttributeModifiers.b.d.BY_ID, ItemAttributeModifiers.b.d::id);
            private final String name;
            private final int id;
            final MapCodec<? extends ItemAttributeModifiers.b> codec;
            private final StreamCodec<RegistryFriendlyByteBuf, ? extends ItemAttributeModifiers.b> streamCodec;

            private d(final String s, final int i, final MapCodec mapcodec, final StreamCodec streamcodec) {
                this.name = s;
                this.id = i;
                this.codec = mapcodec;
                this.streamCodec = streamcodec;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }

            private int id() {
                return this.id;
            }

            private StreamCodec<RegistryFriendlyByteBuf, ? extends ItemAttributeModifiers.b> streamCodec() {
                return this.streamCodec;
            }
        }

        public static record a() implements ItemAttributeModifiers.b {

            static final ItemAttributeModifiers.b.a INSTANCE = new ItemAttributeModifiers.b.a();
            static final MapCodec<ItemAttributeModifiers.b.a> CODEC = MapCodec.unit(ItemAttributeModifiers.b.a.INSTANCE);
            static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.b.a> STREAM_CODEC = StreamCodec.<RegistryFriendlyByteBuf, ItemAttributeModifiers.b.a>unit(ItemAttributeModifiers.b.a.INSTANCE);

            @Override
            public ItemAttributeModifiers.b.d type() {
                return ItemAttributeModifiers.b.d.DEFAULT;
            }

            @Override
            public void apply(Consumer<IChatBaseComponent> consumer, @Nullable EntityHuman entityhuman, Holder<AttributeBase> holder, AttributeModifier attributemodifier) {
                double d0 = attributemodifier.amount();
                boolean flag = false;

                if (entityhuman != null) {
                    if (attributemodifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                        d0 += entityhuman.getAttributeBaseValue(GenericAttributes.ATTACK_DAMAGE);
                        flag = true;
                    } else if (attributemodifier.is(Item.BASE_ATTACK_SPEED_ID)) {
                        d0 += entityhuman.getAttributeBaseValue(GenericAttributes.ATTACK_SPEED);
                        flag = true;
                    }
                }

                double d1;

                if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    if (holder.is(GenericAttributes.KNOCKBACK_RESISTANCE)) {
                        d1 = d0 * 10.0D;
                    } else {
                        d1 = d0;
                    }
                } else {
                    d1 = d0 * 100.0D;
                }

                if (flag) {
                    consumer.accept(CommonComponents.space().append((IChatBaseComponent) IChatBaseComponent.translatable("attribute.modifier.equals." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId()))).withStyle(EnumChatFormat.DARK_GREEN));
                } else if (d0 > 0.0D) {
                    consumer.accept(IChatBaseComponent.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId())).withStyle(((AttributeBase) holder.value()).getStyle(true)));
                } else if (d0 < 0.0D) {
                    consumer.accept(IChatBaseComponent.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId())).withStyle(((AttributeBase) holder.value()).getStyle(false)));
                }

            }
        }

        public static record b() implements ItemAttributeModifiers.b {

            static final ItemAttributeModifiers.b.b INSTANCE = new ItemAttributeModifiers.b.b();
            static final MapCodec<ItemAttributeModifiers.b.b> CODEC = MapCodec.unit(ItemAttributeModifiers.b.b.INSTANCE);
            static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.b.b> STREAM_CODEC = StreamCodec.<RegistryFriendlyByteBuf, ItemAttributeModifiers.b.b>unit(ItemAttributeModifiers.b.b.INSTANCE);

            @Override
            public ItemAttributeModifiers.b.d type() {
                return ItemAttributeModifiers.b.d.HIDDEN;
            }

            @Override
            public void apply(Consumer<IChatBaseComponent> consumer, @Nullable EntityHuman entityhuman, Holder<AttributeBase> holder, AttributeModifier attributemodifier) {}
        }

        public static record c(IChatBaseComponent component) implements ItemAttributeModifiers.b {

            static final MapCodec<ItemAttributeModifiers.b.c> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(ComponentSerialization.CODEC.fieldOf("value").forGetter(ItemAttributeModifiers.b.c::component)).apply(instance, ItemAttributeModifiers.b.c::new);
            });
            static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.b.c> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.STREAM_CODEC, ItemAttributeModifiers.b.c::component, ItemAttributeModifiers.b.c::new);

            @Override
            public ItemAttributeModifiers.b.d type() {
                return ItemAttributeModifiers.b.d.OVERRIDE;
            }

            @Override
            public void apply(Consumer<IChatBaseComponent> consumer, @Nullable EntityHuman entityhuman, Holder<AttributeBase> holder, AttributeModifier attributemodifier) {
                consumer.accept(this.component);
            }
        }
    }

    public static record c(Holder<AttributeBase> attribute, AttributeModifier modifier, EquipmentSlotGroup slot, ItemAttributeModifiers.b display) {

        public static final Codec<ItemAttributeModifiers.c> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(AttributeBase.CODEC.fieldOf("type").forGetter(ItemAttributeModifiers.c::attribute), AttributeModifier.MAP_CODEC.forGetter(ItemAttributeModifiers.c::modifier), EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(ItemAttributeModifiers.c::slot), ItemAttributeModifiers.b.CODEC.optionalFieldOf("display", ItemAttributeModifiers.b.a.INSTANCE).forGetter(ItemAttributeModifiers.c::display)).apply(instance, ItemAttributeModifiers.c::new);
        });
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.c> STREAM_CODEC = StreamCodec.composite(AttributeBase.STREAM_CODEC, ItemAttributeModifiers.c::attribute, AttributeModifier.STREAM_CODEC, ItemAttributeModifiers.c::modifier, EquipmentSlotGroup.STREAM_CODEC, ItemAttributeModifiers.c::slot, ItemAttributeModifiers.b.STREAM_CODEC, ItemAttributeModifiers.c::display, ItemAttributeModifiers.c::new);

        public c(Holder<AttributeBase> holder, AttributeModifier attributemodifier, EquipmentSlotGroup equipmentslotgroup) {
            this(holder, attributemodifier, equipmentslotgroup, ItemAttributeModifiers.b.attributeModifiers());
        }

        public boolean matches(Holder<AttributeBase> holder, MinecraftKey minecraftkey) {
            return holder.equals(this.attribute) && this.modifier.is(minecraftkey);
        }
    }

    public static class a {

        private final ImmutableList.Builder<ItemAttributeModifiers.c> entries = ImmutableList.builder();

        a() {}

        public ItemAttributeModifiers.a add(Holder<AttributeBase> holder, AttributeModifier attributemodifier, EquipmentSlotGroup equipmentslotgroup) {
            this.entries.add(new ItemAttributeModifiers.c(holder, attributemodifier, equipmentslotgroup));
            return this;
        }

        public ItemAttributeModifiers.a add(Holder<AttributeBase> holder, AttributeModifier attributemodifier, EquipmentSlotGroup equipmentslotgroup, ItemAttributeModifiers.b itemattributemodifiers_b) {
            this.entries.add(new ItemAttributeModifiers.c(holder, attributemodifier, equipmentslotgroup, itemattributemodifiers_b));
            return this;
        }

        public ItemAttributeModifiers build() {
            return new ItemAttributeModifiers(this.entries.build());
        }
    }
}
