package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;

public class BlankFormat implements NumberFormat {

    public static final BlankFormat INSTANCE = new BlankFormat();
    public static final NumberFormatType<BlankFormat> TYPE = new NumberFormatType<BlankFormat>() {
        private static final MapCodec<BlankFormat> CODEC = MapCodec.unit(BlankFormat.INSTANCE);

        @Override
        public MapCodec<BlankFormat> mapCodec() {
            return null.CODEC;
        }

        public void writeToStream(PacketDataSerializer packetdataserializer, BlankFormat blankformat) {}

        @Override
        public BlankFormat readFromStream(PacketDataSerializer packetdataserializer) {
            return BlankFormat.INSTANCE;
        }
    };

    public BlankFormat() {}

    @Override
    public IChatMutableComponent format(int i) {
        return IChatBaseComponent.empty();
    }

    @Override
    public NumberFormatType<BlankFormat> type() {
        return BlankFormat.TYPE;
    }
}
