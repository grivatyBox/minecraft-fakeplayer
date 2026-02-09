package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.TooltipProvider;

public record MapId(int id) implements TooltipProvider {

    public static final Codec<MapId> CODEC = Codec.INT.xmap(MapId::new, MapId::id);
    public static final StreamCodec<ByteBuf, MapId> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(MapId::new, MapId::id);
    private static final IChatBaseComponent LOCKED_TEXT = IChatBaseComponent.translatable("filled_map.locked").withStyle(EnumChatFormat.GRAY);

    public String key() {
        return "map_" + this.id;
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag, DataComponentGetter datacomponentgetter) {
        WorldMap worldmap = item_b.mapData(this);

        if (worldmap == null) {
            consumer.accept(IChatBaseComponent.translatable("filled_map.unknown").withStyle(EnumChatFormat.GRAY));
        } else {
            MapPostProcessing mappostprocessing = (MapPostProcessing) datacomponentgetter.get(DataComponents.MAP_POST_PROCESSING);

            if (datacomponentgetter.get(DataComponents.CUSTOM_NAME) == null && mappostprocessing == null) {
                consumer.accept(IChatBaseComponent.translatable("filled_map.id", this.id).withStyle(EnumChatFormat.GRAY));
            }

            if (worldmap.locked || mappostprocessing == MapPostProcessing.LOCK) {
                consumer.accept(MapId.LOCKED_TEXT);
            }

            if (tooltipflag.isAdvanced()) {
                int i = mappostprocessing == MapPostProcessing.SCALE ? 1 : 0;
                int j = Math.min(worldmap.scale + i, 4);

                consumer.accept(IChatBaseComponent.translatable("filled_map.scale", 1 << j).withStyle(EnumChatFormat.GRAY));
                consumer.accept(IChatBaseComponent.translatable("filled_map.level", j, 4).withStyle(EnumChatFormat.GRAY));
            }

        }
    }
}
