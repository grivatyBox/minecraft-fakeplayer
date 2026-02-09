package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum DebugEntityBlockIntersection {

    IN_BLOCK(0, 1610678016), IN_FLUID(1, 1610612991), IN_AIR(2, 1613968179);

    private static final IntFunction<DebugEntityBlockIntersection> BY_ID = ByIdMap.<DebugEntityBlockIntersection>continuous((debugentityblockintersection) -> {
        return debugentityblockintersection.id;
    }, values(), ByIdMap.a.ZERO);
    public static final StreamCodec<ByteBuf, DebugEntityBlockIntersection> STREAM_CODEC = ByteBufCodecs.idMapper(DebugEntityBlockIntersection.BY_ID, (debugentityblockintersection) -> {
        return debugentityblockintersection.id;
    });
    private final int id;
    private final int color;

    private DebugEntityBlockIntersection(final int i, final int j) {
        this.id = i;
        this.color = j;
    }

    public int color() {
        return this.color;
    }
}
