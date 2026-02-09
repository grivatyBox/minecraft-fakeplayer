package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;

public record PacketPlayOutScoreboardScore(String owner, String objectiveName, int score, @Nullable IChatBaseComponent display, @Nullable NumberFormat numberFormat) implements Packet<PacketListenerPlayOut> {

    public PacketPlayOutScoreboardScore(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(), packetdataserializer.readUtf(), packetdataserializer.readVarInt(), (IChatBaseComponent) packetdataserializer.readNullable(PacketDataSerializer::readComponentTrusted), (NumberFormat) packetdataserializer.readNullable(NumberFormatTypes::readFromStream));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.owner);
        packetdataserializer.writeUtf(this.objectiveName);
        packetdataserializer.writeVarInt(this.score);
        packetdataserializer.writeNullable(this.display, PacketDataSerializer::writeComponent);
        packetdataserializer.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetScore(this);
    }
}
