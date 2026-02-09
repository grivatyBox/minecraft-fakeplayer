package net.minecraft.network.protocol.status;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.RegistryOps;

public record PacketStatusOutServerInfo(ServerPing status) implements Packet<PacketStatusOutListener> {

    private static final RegistryOps<JsonElement> OPS = IRegistryCustom.EMPTY.<JsonElement>createSerializationContext(JsonOps.INSTANCE);
    public static final StreamCodec<ByteBuf, PacketStatusOutServerInfo> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.lenientJson(32767).apply(ByteBufCodecs.fromCodec(PacketStatusOutServerInfo.OPS, ServerPing.CODEC)), PacketStatusOutServerInfo::status, PacketStatusOutServerInfo::new);

    @Override
    public PacketType<PacketStatusOutServerInfo> type() {
        return StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE;
    }

    public void handle(PacketStatusOutListener packetstatusoutlistener) {
        packetstatusoutlistener.handleStatusResponse(this);
    }
}
