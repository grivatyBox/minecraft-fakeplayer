package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<PacketListenerPlayOut> {

    public ClientboundTickingStatePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readFloat(), packetdataserializer.readBoolean());
    }

    public static ClientboundTickingStatePacket from(TickRateManager tickratemanager) {
        return new ClientboundTickingStatePacket(tickratemanager.tickrate(), tickratemanager.isFrozen());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeFloat(this.tickRate);
        packetdataserializer.writeBoolean(this.isFrozen);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTickingState(this);
    }
}
