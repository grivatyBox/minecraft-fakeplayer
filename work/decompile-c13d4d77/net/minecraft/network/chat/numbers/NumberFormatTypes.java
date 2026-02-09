package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;

public class NumberFormatTypes {

    public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE.byNameCodec().dispatchMap(NumberFormat::type, (numberformattype) -> {
        return numberformattype.mapCodec().codec();
    });
    public static final Codec<NumberFormat> CODEC = NumberFormatTypes.MAP_CODEC.codec();

    public NumberFormatTypes() {}

    public static NumberFormatType<?> bootstrap(IRegistry<NumberFormatType<?>> iregistry) {
        NumberFormatType<?> numberformattype = (NumberFormatType) IRegistry.register(iregistry, "blank", BlankFormat.TYPE);

        IRegistry.register(iregistry, "styled", StyledFormat.TYPE);
        IRegistry.register(iregistry, "fixed", FixedFormat.TYPE);
        return numberformattype;
    }

    public static <T extends NumberFormat> void writeToStream(PacketDataSerializer packetdataserializer, T t0) {
        NumberFormatType<T> numberformattype = t0.type();

        packetdataserializer.writeId(BuiltInRegistries.NUMBER_FORMAT_TYPE, numberformattype);
        numberformattype.writeToStream(packetdataserializer, t0);
    }

    public static NumberFormat readFromStream(PacketDataSerializer packetdataserializer) {
        NumberFormatType<?> numberformattype = (NumberFormatType) packetdataserializer.readById((Registry) BuiltInRegistries.NUMBER_FORMAT_TYPE);

        return numberformattype.readFromStream(packetdataserializer);
    }
}
