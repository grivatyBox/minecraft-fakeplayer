package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.syncher.DataWatcher;

public record PacketPlayOutEntityMetadata(int id, List<DataWatcher.c<?>> packedItems) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutEntityMetadata> STREAM_CODEC = Packet.<RegistryFriendlyByteBuf, PacketPlayOutEntityMetadata>codec(PacketPlayOutEntityMetadata::write, PacketPlayOutEntityMetadata::new);
    public static final int EOF_MARKER = 255;

    private PacketPlayOutEntityMetadata(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this(registryfriendlybytebuf.readVarInt(), unpack(registryfriendlybytebuf));
    }

    private static void pack(List<DataWatcher.c<?>> list, RegistryFriendlyByteBuf registryfriendlybytebuf) {
        for (DataWatcher.c<?> datawatcher_c : list) {
            datawatcher_c.write(registryfriendlybytebuf);
        }

        registryfriendlybytebuf.writeByte(255);
    }

    private static List<DataWatcher.c<?>> unpack(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        List<DataWatcher.c<?>> list = new ArrayList();

        int i;

        while ((i = registryfriendlybytebuf.readUnsignedByte()) != 255) {
            list.add(DataWatcher.c.read(registryfriendlybytebuf, i));
        }

        return list;
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeVarInt(this.id);
        pack(this.packedItems, registryfriendlybytebuf);
    }

    @Override
    public PacketType<PacketPlayOutEntityMetadata> type() {
        return GamePacketTypes.CLIENTBOUND_SET_ENTITY_DATA;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetEntityData(this);
    }
}
