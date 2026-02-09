package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.debug.DebugSubscription;

public record ClientboundDebugBlockValuePacket(BlockPosition blockPos, DebugSubscription.b<?> update) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDebugBlockValuePacket> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, ClientboundDebugBlockValuePacket::blockPos, DebugSubscription.b.STREAM_CODEC, ClientboundDebugBlockValuePacket::update, ClientboundDebugBlockValuePacket::new);

    @Override
    public PacketType<ClientboundDebugBlockValuePacket> type() {
        return GamePacketTypes.CLIENTBOUND_DEBUG_BLOCK_VALUE;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleDebugBlockValue(this);
    }
}
