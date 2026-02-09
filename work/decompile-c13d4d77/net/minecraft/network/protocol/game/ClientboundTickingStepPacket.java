package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStepPacket(int tickSteps) implements Packet<PacketListenerPlayOut> {

    public ClientboundTickingStepPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readVarInt());
    }

    public static ClientboundTickingStepPacket from(TickRateManager tickratemanager) {
        return new ClientboundTickingStepPacket(tickratemanager.frozenTicksToRun());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.tickSteps);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTickingStep(this);
    }
}
