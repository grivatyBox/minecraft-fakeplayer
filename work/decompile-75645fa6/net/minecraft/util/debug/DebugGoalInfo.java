package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DebugGoalInfo(List<DebugGoalInfo.a> goals) {

    public static final StreamCodec<ByteBuf, DebugGoalInfo> STREAM_CODEC = StreamCodec.composite(DebugGoalInfo.a.STREAM_CODEC.apply(ByteBufCodecs.list()), DebugGoalInfo::goals, DebugGoalInfo::new);

    public static record a(int priority, boolean isRunning, String name) {

        public static final StreamCodec<ByteBuf, DebugGoalInfo.a> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, DebugGoalInfo.a::priority, ByteBufCodecs.BOOL, DebugGoalInfo.a::isRunning, ByteBufCodecs.stringUtf8(255), DebugGoalInfo.a::name, DebugGoalInfo.a::new);
    }
}
