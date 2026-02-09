package net.minecraft.network.protocol.common;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public record ServerboundCustomClickActionPacket(MinecraftKey id, Optional<NBTBase> payload) implements Packet<ServerCommonPacketListener> {

    private static final StreamCodec<ByteBuf, Optional<NBTBase>> UNTRUSTED_TAG_CODEC = ByteBufCodecs.optionalTagCodec(() -> {
        return new NBTReadLimiter(32768L, 16);
    }).apply(ByteBufCodecs.lengthPrefixed(65536));
    public static final StreamCodec<ByteBuf, ServerboundCustomClickActionPacket> STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, ServerboundCustomClickActionPacket::id, ServerboundCustomClickActionPacket.UNTRUSTED_TAG_CODEC, ServerboundCustomClickActionPacket::payload, ServerboundCustomClickActionPacket::new);

    @Override
    public PacketType<ServerboundCustomClickActionPacket> type() {
        return CommonPacketTypes.SERVERBOUND_CUSTOM_CLICK_ACTION;
    }

    public void handle(ServerCommonPacketListener servercommonpacketlistener) {
        servercommonpacketlistener.handleCustomClickAction(this);
    }
}
