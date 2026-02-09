package net.minecraft.world.ticks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.ChunkCoordIntPair;

public record TickListChunk<T>(T type, BlockPosition pos, int delay, TickListPriority priority) {

    public static final Hash.Strategy<TickListChunk<?>> UNIQUE_TICK_HASH = new Hash.Strategy<TickListChunk<?>>() {
        public int hashCode(TickListChunk<?> ticklistchunk) {
            return 31 * ticklistchunk.pos().hashCode() + ticklistchunk.type().hashCode();
        }

        public boolean equals(@Nullable TickListChunk<?> ticklistchunk, @Nullable TickListChunk<?> ticklistchunk1) {
            return ticklistchunk == ticklistchunk1 ? true : (ticklistchunk != null && ticklistchunk1 != null ? ticklistchunk.type() == ticklistchunk1.type() && ticklistchunk.pos().equals(ticklistchunk1.pos()) : false);
        }
    };

    public static <T> Codec<TickListChunk<T>> codec(Codec<T> codec) {
        MapCodec<BlockPosition> mapcodec = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.INT.fieldOf("x").forGetter(BaseBlockPosition::getX), Codec.INT.fieldOf("y").forGetter(BaseBlockPosition::getY), Codec.INT.fieldOf("z").forGetter(BaseBlockPosition::getZ)).apply(instance, BlockPosition::new);
        });

        return RecordCodecBuilder.create((instance) -> {
            return instance.group(codec.fieldOf("i").forGetter(TickListChunk::type), mapcodec.forGetter(TickListChunk::pos), Codec.INT.fieldOf("t").forGetter(TickListChunk::delay), TickListPriority.CODEC.fieldOf("p").forGetter(TickListChunk::priority)).apply(instance, TickListChunk::new);
        });
    }

    public static <T> List<TickListChunk<T>> filterTickListForChunk(List<TickListChunk<T>> list, ChunkCoordIntPair chunkcoordintpair) {
        long i = chunkcoordintpair.toLong();

        return list.stream().filter((ticklistchunk) -> {
            return ChunkCoordIntPair.asLong(ticklistchunk.pos()) == i;
        }).toList();
    }

    public NextTickListEntry<T> unpack(long i, long j) {
        return new NextTickListEntry<T>(this.type, this.pos, i + (long) this.delay, this.priority, j);
    }

    public static <T> TickListChunk<T> probe(T t0, BlockPosition blockposition) {
        return new TickListChunk<T>(t0, blockposition, 0, TickListPriority.NORMAL);
    }
}
