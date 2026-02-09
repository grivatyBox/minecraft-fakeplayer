package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public record PacketPlayOutWindowItems(int containerId, int stateId, List<ItemStack> items, ItemStack carriedItem) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutWindowItems> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.CONTAINER_ID, PacketPlayOutWindowItems::containerId, ByteBufCodecs.VAR_INT, PacketPlayOutWindowItems::stateId, ItemStack.OPTIONAL_LIST_STREAM_CODEC, PacketPlayOutWindowItems::items, ItemStack.OPTIONAL_STREAM_CODEC, PacketPlayOutWindowItems::carriedItem, PacketPlayOutWindowItems::new);

    @Override
    public PacketType<PacketPlayOutWindowItems> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleContainerContent(this);
    }
}
