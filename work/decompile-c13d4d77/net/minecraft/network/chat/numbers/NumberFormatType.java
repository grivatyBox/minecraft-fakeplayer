package net.minecraft.network.chat.numbers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.PacketDataSerializer;

public interface NumberFormatType<T extends NumberFormat> {

    MapCodec<T> mapCodec();

    void writeToStream(PacketDataSerializer packetdataserializer, T t0);

    T readFromStream(PacketDataSerializer packetdataserializer);
}
