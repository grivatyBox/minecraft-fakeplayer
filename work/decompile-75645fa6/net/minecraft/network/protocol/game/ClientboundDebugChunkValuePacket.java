package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.world.level.ChunkCoordIntPair;

public record ClientboundDebugChunkValuePacket(ChunkCoordIntPair chunkPos, DebugSubscription.b<?> update) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDebugChunkValuePacket> STREAM_CODEC = StreamCodec.composite(ChunkCoordIntPair.STREAM_CODEC, ClientboundDebugChunkValuePacket::chunkPos, DebugSubscription.b.STREAM_CODEC, ClientboundDebugChunkValuePacket::update, ClientboundDebugChunkValuePacket::new);

    @Override
    public PacketType<ClientboundDebugChunkValuePacket> type() {
        return GamePacketTypes.CLIENTBOUND_DEBUG_CHUNK_VALUE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleDebugChunkValue(this);
    }
}
