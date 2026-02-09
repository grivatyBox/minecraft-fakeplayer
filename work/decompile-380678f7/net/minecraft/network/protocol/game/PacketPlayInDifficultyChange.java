package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumDifficulty;

public record PacketPlayInDifficultyChange(EnumDifficulty difficulty) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<ByteBuf, PacketPlayInDifficultyChange> STREAM_CODEC = StreamCodec.composite(EnumDifficulty.STREAM_CODEC, PacketPlayInDifficultyChange::difficulty, PacketPlayInDifficultyChange::new);

    @Override
    public PacketType<PacketPlayInDifficultyChange> type() {
        return GamePacketTypes.SERVERBOUND_CHANGE_DIFFICULTY;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleChangeDifficulty(this);
    }
}
