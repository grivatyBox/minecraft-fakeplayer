package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import java.util.List;
import java.util.SequencedSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TooltipDisplay(boolean hideTooltip, SequencedSet<DataComponentType<?>> hiddenComponents) {

    private static final Codec<SequencedSet<DataComponentType<?>>> COMPONENT_SET_CODEC = DataComponentType.CODEC.listOf().xmap(ReferenceLinkedOpenHashSet::new, List::copyOf);
    public static final Codec<TooltipDisplay> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(TooltipDisplay::hideTooltip), TooltipDisplay.COMPONENT_SET_CODEC.optionalFieldOf("hidden_components", ReferenceSortedSets.emptySet()).forGetter(TooltipDisplay::hiddenComponents)).apply(instance, TooltipDisplay::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, TooltipDisplay> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TooltipDisplay::hideTooltip, DataComponentType.STREAM_CODEC.apply(ByteBufCodecs.collection(ReferenceLinkedOpenHashSet::new)), TooltipDisplay::hiddenComponents, TooltipDisplay::new);
    public static final TooltipDisplay DEFAULT = new TooltipDisplay(false, ReferenceSortedSets.emptySet());

    public TooltipDisplay withHidden(DataComponentType<?> datacomponenttype, boolean flag) {
        if (this.hiddenComponents.contains(datacomponenttype) == flag) {
            return this;
        } else {
            SequencedSet<DataComponentType<?>> sequencedset = new ReferenceLinkedOpenHashSet(this.hiddenComponents);

            if (flag) {
                sequencedset.add(datacomponenttype);
            } else {
                sequencedset.remove(datacomponenttype);
            }

            return new TooltipDisplay(this.hideTooltip, sequencedset);
        }
    }

    public boolean shows(DataComponentType<?> datacomponenttype) {
        return !this.hideTooltip && !this.hiddenComponents.contains(datacomponenttype);
    }
}
