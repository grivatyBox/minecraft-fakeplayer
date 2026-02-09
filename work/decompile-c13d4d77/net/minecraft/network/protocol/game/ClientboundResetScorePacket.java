package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<PacketListenerPlayOut> {

    public ClientboundResetScorePacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(), (String) packetdataserializer.readNullable(PacketDataSerializer::readUtf));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.owner);
        packetdataserializer.writeNullable(this.objectiveName, PacketDataSerializer::writeUtf);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleResetScore(this);
    }
}
