package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<PacketListenerPlayIn> {

    public ServerboundContainerSlotStateChangedPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt(), packetdataserializer.readVarInt(), packetdataserializer.readBoolean());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.slotId);
        packetdataserializer.writeVarInt(this.containerId);
        packetdataserializer.writeBoolean(this.newState);
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleContainerSlotStateChanged(this);
    }
}
