package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;

public record ServerboundTestInstanceBlockActionPacket(BlockPosition pos, ServerboundTestInstanceBlockActionPacket.a action, TestInstanceBlockEntity.a data) implements Packet<PacketListenerPlayIn> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTestInstanceBlockActionPacket> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC, ServerboundTestInstanceBlockActionPacket::pos, ServerboundTestInstanceBlockActionPacket.a.STREAM_CODEC, ServerboundTestInstanceBlockActionPacket::action, TestInstanceBlockEntity.a.STREAM_CODEC, ServerboundTestInstanceBlockActionPacket::data, ServerboundTestInstanceBlockActionPacket::new);

    public ServerboundTestInstanceBlockActionPacket(BlockPosition blockposition, ServerboundTestInstanceBlockActionPacket.a serverboundtestinstanceblockactionpacket_a, Optional<ResourceKey<GameTestInstance>> optional, BaseBlockPosition baseblockposition, EnumBlockRotation enumblockrotation, boolean flag) {
        this(blockposition, serverboundtestinstanceblockactionpacket_a, new TestInstanceBlockEntity.a(optional, baseblockposition, enumblockrotation, flag, TestInstanceBlockEntity.b.CLEARED, Optional.empty()));
    }

    @Override
    public PacketType<ServerboundTestInstanceBlockActionPacket> type() {
        return GamePacketTypes.SERVERBOUND_TEST_INSTANCE_BLOCK_ACTION;
    }

    public void handle(PacketListenerPlayIn packetlistenerplayin) {
        packetlistenerplayin.handleTestInstanceBlockAction(this);
    }

    public static enum a {

        INIT(0), QUERY(1), SET(2), RESET(3), SAVE(4), EXPORT(5), RUN(6);

        private static final IntFunction<ServerboundTestInstanceBlockActionPacket.a> BY_ID = ByIdMap.<ServerboundTestInstanceBlockActionPacket.a>continuous((serverboundtestinstanceblockactionpacket_a) -> {
            return serverboundtestinstanceblockactionpacket_a.id;
        }, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, ServerboundTestInstanceBlockActionPacket.a> STREAM_CODEC = ByteBufCodecs.idMapper(ServerboundTestInstanceBlockActionPacket.a.BY_ID, (serverboundtestinstanceblockactionpacket_a) -> {
            return serverboundtestinstanceblockactionpacket_a.id;
        });
        private final int id;

        private a(final int i) {
            this.id = i;
        }
    }
}
