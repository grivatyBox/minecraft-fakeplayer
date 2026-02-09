package net.minecraft.network.protocol.login;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.RegistryOps;

public record PacketLoginOutDisconnect(IChatBaseComponent reason) implements Packet<PacketLoginOutListener> {

    private static final RegistryOps<JsonElement> OPS = IRegistryCustom.EMPTY.<JsonElement>createSerializationContext(JsonOps.INSTANCE);
    public static final StreamCodec<ByteBuf, PacketLoginOutDisconnect> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.lenientJson(262144).apply(ByteBufCodecs.fromCodec(PacketLoginOutDisconnect.OPS, ComponentSerialization.CODEC)), PacketLoginOutDisconnect::reason, PacketLoginOutDisconnect::new);

    @Override
    public PacketType<PacketLoginOutDisconnect> type() {
        return LoginPacketTypes.CLIENTBOUND_LOGIN_DISCONNECT;
    }

    public void handle(PacketLoginOutListener packetloginoutlistener) {
        packetloginoutlistener.handleDisconnect(this);
    }
}
