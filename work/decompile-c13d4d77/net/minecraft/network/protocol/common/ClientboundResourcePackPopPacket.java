package net.minecraft.network.protocol.common;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPopPacket(Optional<UUID> id) implements Packet<ClientCommonPacketListener> {

    public ClientboundResourcePackPopPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readOptional(PacketDataSerializer::readUUID));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeOptional(this.id, PacketDataSerializer::writeUUID);
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleResourcePackPop(this);
    }
}
