package net.minecraft.network.protocol.common;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;

public record ClientboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ClientCommonPacketListener> {

    private static final int MAX_PAYLOAD_SIZE = 1048576;
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCustomPayloadPacket> GAMEPLAY_STREAM_CODEC = CustomPacketPayload.codec((minecraftkey) -> {
        return DiscardedPayload.codec(minecraftkey, 1048576);
    }, (List) SystemUtils.make(Lists.newArrayList(new CustomPacketPayload.c[]{new CustomPacketPayload.c(BrandPayload.TYPE, BrandPayload.STREAM_CODEC)}), (arraylist) -> {
    })).map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);
    public static final StreamCodec<PacketDataSerializer, ClientboundCustomPayloadPacket> CONFIG_STREAM_CODEC = CustomPacketPayload.codec((minecraftkey) -> {
        return DiscardedPayload.codec(minecraftkey, 1048576);
    }, List.of(new CustomPacketPayload.c(BrandPayload.TYPE, BrandPayload.STREAM_CODEC))).map(ClientboundCustomPayloadPacket::new, ClientboundCustomPayloadPacket::payload);

    @Override
    public PacketType<ClientboundCustomPayloadPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD;
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleCustomPayload(this);
    }
}
