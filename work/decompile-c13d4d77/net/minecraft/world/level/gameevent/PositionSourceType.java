package net.minecraft.world.level.gameevent;

import com.mojang.serialization.Codec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;

public interface PositionSourceType<T extends PositionSource> {

    PositionSourceType<BlockPositionSource> BLOCK = register("block", new BlockPositionSource.a());
    PositionSourceType<EntityPositionSource> ENTITY = register("entity", new EntityPositionSource.a());

    T read(PacketDataSerializer packetdataserializer);

    void write(PacketDataSerializer packetdataserializer, T t0);

    Codec<T> codec();

    static <S extends PositionSourceType<T>, T extends PositionSource> S register(String s, S s0) {
        return (PositionSourceType) IRegistry.register(BuiltInRegistries.POSITION_SOURCE_TYPE, s, s0);
    }

    static PositionSource fromNetwork(PacketDataSerializer packetdataserializer) {
        PositionSourceType<?> positionsourcetype = (PositionSourceType) packetdataserializer.readById((Registry) BuiltInRegistries.POSITION_SOURCE_TYPE);

        if (positionsourcetype == null) {
            throw new IllegalArgumentException("Unknown position source type");
        } else {
            return positionsourcetype.read(packetdataserializer);
        }
    }

    static <T extends PositionSource> void toNetwork(T t0, PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeId(BuiltInRegistries.POSITION_SOURCE_TYPE, t0.getType());
        t0.getType().write(packetdataserializer, t0);
    }
}
