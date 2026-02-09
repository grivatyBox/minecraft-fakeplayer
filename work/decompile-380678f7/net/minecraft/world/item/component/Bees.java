package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.TileEntityBeehive;

public record Bees(List<TileEntityBeehive.c> bees) implements TooltipProvider {

    public static final Codec<Bees> CODEC = TileEntityBeehive.c.LIST_CODEC.xmap(Bees::new, Bees::bees);
    public static final StreamCodec<ByteBuf, Bees> STREAM_CODEC = TileEntityBeehive.c.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Bees::new, Bees::bees);
    public static final Bees EMPTY = new Bees(List.of());

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        consumer.accept(IChatBaseComponent.translatable("container.beehive.bees", this.bees.size(), 3).withStyle(EnumChatFormat.GRAY));
    }
}
