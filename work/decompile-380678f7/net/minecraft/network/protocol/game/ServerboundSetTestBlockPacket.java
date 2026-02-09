package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.state.properties.TestBlockMode;

public record ServerboundSetTestBlockPacket(BlockPosition position, TestBlockMode mode, String message) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<PacketDataSerializer, ServerboundSetTestBlockPacket> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, ServerboundSetTestBlockPacket::position, TestBlockMode.STREAM_CODEC, ServerboundSetTestBlockPacket::mode, ByteBufCodecs.STRING_UTF8, ServerboundSetTestBlockPacket::message, ServerboundSetTestBlockPacket::new);

    @Override
    public PacketType<ServerboundSetTestBlockPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_TEST_BLOCK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleSetTestBlock(this);
    }
}
