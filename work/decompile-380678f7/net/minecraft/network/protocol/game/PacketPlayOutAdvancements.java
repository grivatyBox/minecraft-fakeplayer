package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class PacketPlayOutAdvancements implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutAdvancements> STREAM_CODEC = Packet.<RegistryFriendlyByteBuf, PacketPlayOutAdvancements>codec(PacketPlayOutAdvancements::write, PacketPlayOutAdvancements::new);
    private final boolean reset;
    private final List<AdvancementHolder> added;
    private final Set<MinecraftKey> removed;
    private final Map<MinecraftKey, AdvancementProgress> progress;
    private final boolean showAdvancements;

    public PacketPlayOutAdvancements(boolean flag, Collection<AdvancementHolder> collection, Set<MinecraftKey> set, Map<MinecraftKey, AdvancementProgress> map, boolean flag1) {
        this.reset = flag;
        this.added = List.copyOf(collection);
        this.removed = Set.copyOf(set);
        this.progress = Map.copyOf(map);
        this.showAdvancements = flag1;
    }

    private PacketPlayOutAdvancements(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        this.reset = registryfriendlybytebuf.readBoolean();
        this.added = (List) AdvancementHolder.LIST_STREAM_CODEC.decode(registryfriendlybytebuf);
        this.removed = (Set) registryfriendlybytebuf.readCollection(Sets::newLinkedHashSetWithExpectedSize, PacketDataSerializer::readResourceLocation);
        this.progress = registryfriendlybytebuf.<MinecraftKey, AdvancementProgress>readMap(PacketDataSerializer::readResourceLocation, AdvancementProgress::fromNetwork);
        this.showAdvancements = registryfriendlybytebuf.readBoolean();
    }

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeBoolean(this.reset);
        AdvancementHolder.LIST_STREAM_CODEC.encode(registryfriendlybytebuf, this.added);
        registryfriendlybytebuf.writeCollection(this.removed, PacketDataSerializer::writeResourceLocation);
        registryfriendlybytebuf.writeMap(this.progress, PacketDataSerializer::writeResourceLocation, (packetdataserializer, advancementprogress) -> {
            advancementprogress.serializeToNetwork(packetdataserializer);
        });
        registryfriendlybytebuf.writeBoolean(this.showAdvancements);
    }

    @Override
    public PacketType<PacketPlayOutAdvancements> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateAdvancementsPacket(this);
    }

    public List<AdvancementHolder> getAdded() {
        return this.added;
    }

    public Set<MinecraftKey> getRemoved() {
        return this.removed;
    }

    public Map<MinecraftKey, AdvancementProgress> getProgress() {
        return this.progress;
    }

    public boolean shouldReset() {
        return this.reset;
    }

    public boolean shouldShowAdvancements() {
        return this.showAdvancements;
    }
}
