package net.minecraft.network.protocol.common;

import net.minecraft.network.ClientboundPacketListener;

public interface ClientCommonPacketListener extends ClientboundPacketListener {

    void handleKeepAlive(ClientboundKeepAlivePacket clientboundkeepalivepacket);

    void handlePing(ClientboundPingPacket clientboundpingpacket);

    void handleCustomPayload(ClientboundCustomPayloadPacket clientboundcustompayloadpacket);

    void handleDisconnect(ClientboundDisconnectPacket clientbounddisconnectpacket);

    void handleResourcePackPush(ClientboundResourcePackPushPacket clientboundresourcepackpushpacket);

    void handleResourcePackPop(ClientboundResourcePackPopPacket clientboundresourcepackpoppacket);

    void handleUpdateTags(ClientboundUpdateTagsPacket clientboundupdatetagspacket);
}
