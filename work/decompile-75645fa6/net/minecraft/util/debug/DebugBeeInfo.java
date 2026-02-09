package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DebugBeeInfo(Optional<BlockPosition> hivePos, Optional<BlockPosition> flowerPos, int travelTicks, List<BlockPosition> blacklistedHives) {

    public static final StreamCodec<ByteBuf, DebugBeeInfo> STREAM_CODEC = StreamCodec.composite(BlockPosition.STREAM_CODEC.apply(ByteBufCodecs::optional), DebugBeeInfo::hivePos, BlockPosition.STREAM_CODEC.apply(ByteBufCodecs::optional), DebugBeeInfo::flowerPos, ByteBufCodecs.VAR_INT, DebugBeeInfo::travelTicks, BlockPosition.STREAM_CODEC.apply(ByteBufCodecs.list()), DebugBeeInfo::blacklistedHives, DebugBeeInfo::new);

    public boolean hasHive(BlockPosition blockposition) {
        return this.hivePos.isPresent() && blockposition.equals(this.hivePos.get());
    }
}
