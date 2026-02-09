package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.storage.WorldData;

public record PacketPlayOutSpawnPosition(WorldData.a respawnData) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutSpawnPosition> STREAM_CODEC = StreamCodec.composite(WorldData.a.STREAM_CODEC, PacketPlayOutSpawnPosition::respawnData, PacketPlayOutSpawnPosition::new);

    @Override
    public PacketType<PacketPlayOutSpawnPosition> type() {
        return GamePacketTypes.CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetSpawn(this);
    }
}
