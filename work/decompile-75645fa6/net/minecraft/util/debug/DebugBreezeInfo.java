package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DebugBreezeInfo(Optional<Integer> attackTarget, Optional<BlockPosition> jumpTarget) {

    public static final StreamCodec<ByteBuf, DebugBreezeInfo> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs::optional), DebugBreezeInfo::attackTarget, BlockPosition.STREAM_CODEC.apply(ByteBufCodecs::optional), DebugBreezeInfo::jumpTarget, DebugBreezeInfo::new);
}
