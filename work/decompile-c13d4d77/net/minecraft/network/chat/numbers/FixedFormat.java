package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;

public class FixedFormat implements NumberFormat {

    public static final NumberFormatType<FixedFormat> TYPE = new NumberFormatType<FixedFormat>() {
        private static final MapCodec<FixedFormat> CODEC = ComponentSerialization.CODEC.fieldOf("value").xmap(FixedFormat::new, (fixedformat) -> {
            return fixedformat.value;
        });

        @Override
        public MapCodec<FixedFormat> mapCodec() {
            return null.CODEC;
        }

        public void writeToStream(PacketDataSerializer packetdataserializer, FixedFormat fixedformat) {
            packetdataserializer.writeComponent(fixedformat.value);
        }

        @Override
        public FixedFormat readFromStream(PacketDataSerializer packetdataserializer) {
            IChatBaseComponent ichatbasecomponent = packetdataserializer.readComponentTrusted();

            return new FixedFormat(ichatbasecomponent);
        }
    };
    final IChatBaseComponent value;

    public FixedFormat(IChatBaseComponent ichatbasecomponent) {
        this.value = ichatbasecomponent;
    }

    @Override
    public IChatMutableComponent format(int i) {
        return this.value.copy();
    }

    @Override
    public NumberFormatType<FixedFormat> type() {
        return FixedFormat.TYPE;
    }
}
