package net.minecraft.network.protocol.common;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPushPacket(UUID id, String url, String hash, boolean required, @Nullable IChatBaseComponent prompt) implements Packet<ClientCommonPacketListener> {

    public static final int MAX_HASH_LENGTH = 40;

    public ClientboundResourcePackPushPacket(UUID uuid, String s, String s1, boolean flag, @Nullable IChatBaseComponent ichatbasecomponent) {
        if (s1.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + s1.length() + ")");
        } else {
            this.id = uuid;
            this.url = s;
            this.hash = s1;
            this.required = flag;
            this.prompt = ichatbasecomponent;
        }
    }

    public ClientboundResourcePackPushPacket(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUUID(), packetdataserializer.readUtf(), packetdataserializer.readUtf(40), packetdataserializer.readBoolean(), (IChatBaseComponent) packetdataserializer.readNullable(PacketDataSerializer::readComponentTrusted));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUUID(this.id);
        packetdataserializer.writeUtf(this.url);
        packetdataserializer.writeUtf(this.hash);
        packetdataserializer.writeBoolean(this.required);
        packetdataserializer.writeNullable(this.prompt, PacketDataSerializer::writeComponent);
    }

    public void handle(ClientCommonPacketListener clientcommonpacketlistener) {
        clientcommonpacketlistener.handleResourcePackPush(this);
    }
}
