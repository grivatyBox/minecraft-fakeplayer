package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;

public record DebugStructureInfo(StructureBoundingBox boundingBox, List<DebugStructureInfo.a> pieces) {

    public static final StreamCodec<ByteBuf, DebugStructureInfo> STREAM_CODEC = StreamCodec.composite(StructureBoundingBox.STREAM_CODEC, DebugStructureInfo::boundingBox, DebugStructureInfo.a.STREAM_CODEC.apply(ByteBufCodecs.list()), DebugStructureInfo::pieces, DebugStructureInfo::new);

    public static record a(StructureBoundingBox boundingBox, boolean isStart) {

        public static final StreamCodec<ByteBuf, DebugStructureInfo.a> STREAM_CODEC = StreamCodec.composite(StructureBoundingBox.STREAM_CODEC, DebugStructureInfo.a::boundingBox, ByteBufCodecs.BOOL, DebugStructureInfo.a::isStart, DebugStructureInfo.a::new);
    }
}
