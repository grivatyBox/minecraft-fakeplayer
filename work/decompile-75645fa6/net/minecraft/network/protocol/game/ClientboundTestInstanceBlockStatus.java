package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundTestInstanceBlockStatus(IChatBaseComponent status, Optional<BaseBlockPosition> size) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTestInstanceBlockStatus> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.STREAM_CODEC, ClientboundTestInstanceBlockStatus::status, ByteBufCodecs.optional(BaseBlockPosition.STREAM_CODEC), ClientboundTestInstanceBlockStatus::size, ClientboundTestInstanceBlockStatus::new);

    @Override
    public PacketType<ClientboundTestInstanceBlockStatus> type() {
        return GamePacketTypes.CLIENTBOUND_TEST_INSTANCE_BLOCK_STATUS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleTestInstanceBlockStatus(this);
    }
}
