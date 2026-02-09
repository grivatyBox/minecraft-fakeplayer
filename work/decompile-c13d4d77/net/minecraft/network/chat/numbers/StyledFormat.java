package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.EnumChatFormat;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;

public class StyledFormat implements NumberFormat {

    public static final NumberFormatType<StyledFormat> TYPE = new NumberFormatType<StyledFormat>() {
        private static final MapCodec<StyledFormat> CODEC = ChatModifier.ChatModifierSerializer.MAP_CODEC.xmap(StyledFormat::new, (styledformat) -> {
            return styledformat.style;
        });

        @Override
        public MapCodec<StyledFormat> mapCodec() {
            return null.CODEC;
        }

        public void writeToStream(PacketDataSerializer packetdataserializer, StyledFormat styledformat) {
            packetdataserializer.writeWithCodec(DynamicOpsNBT.INSTANCE, ChatModifier.ChatModifierSerializer.CODEC, styledformat.style);
        }

        @Override
        public StyledFormat readFromStream(PacketDataSerializer packetdataserializer) {
            ChatModifier chatmodifier = (ChatModifier) packetdataserializer.readWithCodecTrusted(DynamicOpsNBT.INSTANCE, ChatModifier.ChatModifierSerializer.CODEC);

            return new StyledFormat(chatmodifier);
        }
    };
    public static final StyledFormat NO_STYLE = new StyledFormat(ChatModifier.EMPTY);
    public static final StyledFormat SIDEBAR_DEFAULT = new StyledFormat(ChatModifier.EMPTY.withColor(EnumChatFormat.RED));
    public static final StyledFormat PLAYER_LIST_DEFAULT = new StyledFormat(ChatModifier.EMPTY.withColor(EnumChatFormat.YELLOW));
    final ChatModifier style;

    public StyledFormat(ChatModifier chatmodifier) {
        this.style = chatmodifier;
    }

    @Override
    public IChatMutableComponent format(int i) {
        return IChatBaseComponent.literal(Integer.toString(i)).withStyle(this.style);
    }

    @Override
    public NumberFormatType<StyledFormat> type() {
        return StyledFormat.TYPE;
    }
}
