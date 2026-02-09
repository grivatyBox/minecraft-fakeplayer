package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundGameTestHighlightPosPacket(BlockPosition absolutePos, BlockPosition relativePos) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<ByteBuf, ClientboundGameTestHighlightPosPacket> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, ClientboundGameTestHighlightPosPacket::absolutePos, BlockPosition.STREAM_CODEC, ClientboundGameTestHighlightPosPacket::relativePos, ClientboundGameTestHighlightPosPacket::new);

    @Override
    public PacketType<ClientboundGameTestHighlightPosPacket> type() {
        return GamePacketTypes.CLIENTBOUND_GAME_TEST_HIGHLIGHT_POS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleGameTestHighlightPos(this);
    }
}
