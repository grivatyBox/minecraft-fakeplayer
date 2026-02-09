package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.InventoryClickType;

public record PacketPlayInWindowClick(int containerId, int stateId, short slotNum, byte buttonNum, InventoryClickType clickType, Int2ObjectMap<HashedStack> changedSlots, HashedStack carriedItem) implements Packet<PacketListenerPlayIn> {

    private static final int MAX_SLOT_COUNT = 128;
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<HashedStack>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(Int2ObjectOpenHashMap::new, ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue), HashedStack.STREAM_CODEC, 128);
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayInWindowClick> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.CONTAINER_ID, PacketPlayInWindowClick::containerId, ByteBufCodecs.VAR_INT, PacketPlayInWindowClick::stateId, ByteBufCodecs.SHORT, PacketPlayInWindowClick::slotNum, ByteBufCodecs.BYTE, PacketPlayInWindowClick::buttonNum, InventoryClickType.STREAM_CODEC, PacketPlayInWindowClick::clickType, PacketPlayInWindowClick.SLOTS_STREAM_CODEC, PacketPlayInWindowClick::changedSlots, HashedStack.STREAM_CODEC, PacketPlayInWindowClick::carriedItem, PacketPlayInWindowClick::new);

    public PacketPlayInWindowClick(int i, int j, short short0, byte b0, InventoryClickType inventoryclicktype, Int2ObjectMap<HashedStack> int2objectmap, HashedStack hashedstack) {
        int2objectmap = Int2ObjectMaps.unmodifiable(int2objectmap);
        this.containerId = i;
        this.stateId = j;
        this.slotNum = short0;
        this.buttonNum = b0;
        this.clickType = inventoryclicktype;
        this.changedSlots = int2objectmap;
        this.carriedItem = hashedstack;
    }

    @Override
    public PacketType<PacketPlayInWindowClick> type() {
        return GamePacketTypes.SERVERBOUND_CONTAINER_CLICK;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleContainerClick(this);
    }
}
