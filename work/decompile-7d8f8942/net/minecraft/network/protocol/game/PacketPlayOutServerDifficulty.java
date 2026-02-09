package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.EnumDifficulty;

public record PacketPlayOutServerDifficulty(EnumDifficulty difficulty, boolean locked) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<ByteBuf, PacketPlayOutServerDifficulty> STREAM_CODEC = StreamCodec.composite(EnumDifficulty.STREAM_CODEC, PacketPlayOutServerDifficulty::difficulty, ByteBufCodecs.BOOL, PacketPlayOutServerDifficulty::locked, PacketPlayOutServerDifficulty::new);

    @Override
    public PacketType<PacketPlayOutServerDifficulty> type() {
        return GamePacketTypes.CLIENTBOUND_CHANGE_DIFFICULTY;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleChangeDifficulty(this);
    }
}
