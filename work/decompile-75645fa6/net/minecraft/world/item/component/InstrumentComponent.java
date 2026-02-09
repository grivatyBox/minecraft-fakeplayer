package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record InstrumentComponent(EitherHolder<Instrument> instrument) implements TooltipProvider {

    public static final Codec<InstrumentComponent> CODEC = EitherHolder.codec(Registries.INSTRUMENT, Instrument.CODEC).xmap(InstrumentComponent::new, InstrumentComponent::instrument);
    public static final StreamCodec<RegistryFriendlyByteBuf, InstrumentComponent> STREAM_CODEC = EitherHolder.streamCodec(Registries.INSTRUMENT, Instrument.STREAM_CODEC).map(InstrumentComponent::new, InstrumentComponent::instrument);

    public InstrumentComponent(Holder<Instrument> holder) {
        this(new EitherHolder(holder));
    }

    /** @deprecated */
    @Deprecated
    public InstrumentComponent(ResourceKey<Instrument> resourcekey) {
        this(new EitherHolder(resourcekey));
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        HolderLookup.a holderlookup_a = item_b.registries();

        if (holderlookup_a != null) {
            Optional<Holder<Instrument>> optional = this.unwrap(holderlookup_a);

            if (optional.isPresent()) {
                IChatMutableComponent ichatmutablecomponent = ((Instrument) ((Holder) optional.get()).value()).description().copy();

                ChatComponentUtils.mergeStyles(ichatmutablecomponent, ChatModifier.EMPTY.withColor(EnumChatFormat.GRAY));
                consumer.accept(ichatmutablecomponent);
            }

        }
    }

    public Optional<Holder<Instrument>> unwrap(HolderLookup.a holderlookup_a) {
        return this.instrument.unwrap(holderlookup_a);
    }
}
