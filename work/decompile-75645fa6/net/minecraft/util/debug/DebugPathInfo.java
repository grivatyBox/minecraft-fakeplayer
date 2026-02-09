package net.minecraft.util.debug;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.PathEntity;

public record DebugPathInfo(PathEntity path, float maxNodeDistance) {

    public static final StreamCodec<PacketDataSerializer, DebugPathInfo> STREAM_CODEC = StreamCodec.composite(PathEntity.STREAM_CODEC, DebugPathInfo::path, ByteBufCodecs.FLOAT, DebugPathInfo::maxNodeDistance, DebugPathInfo::new);
}
