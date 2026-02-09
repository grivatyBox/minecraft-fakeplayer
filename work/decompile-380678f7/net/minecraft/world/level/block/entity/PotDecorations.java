package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.IMaterial;

public record PotDecorations(Optional<Item> back, Optional<Item> left, Optional<Item> right, Optional<Item> front) implements TooltipProvider {

    public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Codec<PotDecorations> CODEC = BuiltInRegistries.ITEM.byNameCodec().sizeLimitedListOf(4).xmap(PotDecorations::new, PotDecorations::ordered);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list(4)).map(PotDecorations::new, PotDecorations::ordered);

    private PotDecorations(List<Item> list) {
        this(getItem(list, 0), getItem(list, 1), getItem(list, 2), getItem(list, 3));
    }

    public PotDecorations(Item item, Item item1, Item item2, Item item3) {
        this(List.of(item, item1, item2, item3));
    }

    private static Optional<Item> getItem(List<Item> list, int i) {
        if (i >= list.size()) {
            return Optional.empty();
        } else {
            Item item = (Item) list.get(i);

            return item == Items.BRICK ? Optional.empty() : Optional.of(item);
        }
    }

    public List<Item> ordered() {
        return Stream.of(this.back, this.left, this.right, this.front).map((optional) -> {
            return (Item) optional.orElse(Items.BRICK);
        }).toList();
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        if (!this.equals(PotDecorations.EMPTY)) {
            consumer.accept(CommonComponents.EMPTY);
            addSideDetailsToTooltip(consumer, this.front);
            addSideDetailsToTooltip(consumer, this.left);
            addSideDetailsToTooltip(consumer, this.right);
            addSideDetailsToTooltip(consumer, this.back);
        }
    }

    private static void addSideDetailsToTooltip(Consumer<IChatBaseComponent> consumer, Optional<Item> optional) {
        consumer.accept((new ItemStack((IMaterial) optional.orElse(Items.BRICK), 1)).getHoverName().plainCopy().withStyle(EnumChatFormat.GRAY));
    }
}
