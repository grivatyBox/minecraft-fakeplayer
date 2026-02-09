package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.VarInt;

public class IdDispatchCodec<B extends ByteBuf, V, T> implements StreamCodec<B, V> {

    private static final int UNKNOWN_TYPE = -1;
    private final Function<V, ? extends T> typeGetter;
    private final List<IdDispatchCodec.c<B, V, T>> byId;
    private final Object2IntMap<T> toId;

    IdDispatchCodec(Function<V, ? extends T> function, List<IdDispatchCodec.c<B, V, T>> list, Object2IntMap<T> object2intmap) {
        this.typeGetter = function;
        this.byId = list;
        this.toId = object2intmap;
    }

    public V decode(B b0) {
        int i = VarInt.read(b0);

        if (i >= 0 && i < this.byId.size()) {
            IdDispatchCodec.c<B, V, T> iddispatchcodec_c = (IdDispatchCodec.c) this.byId.get(i);

            try {
                return (V) iddispatchcodec_c.serializer.decode(b0);
            } catch (Exception exception) {
                if (exception instanceof IdDispatchCodec.b) {
                    throw exception;
                } else {
                    throw new DecoderException("Failed to decode packet '" + String.valueOf(iddispatchcodec_c.type) + "'", exception);
                }
            }
        } else {
            throw new DecoderException("Received unknown packet id " + i);
        }
    }

    public void encode(B b0, V v0) {
        T t0 = (T) this.typeGetter.apply(v0);
        int i = this.toId.getOrDefault(t0, -1);

        if (i == -1) {
            throw new EncoderException("Sending unknown packet '" + String.valueOf(t0) + "'");
        } else {
            VarInt.write(b0, i);
            IdDispatchCodec.c<B, V, T> iddispatchcodec_c = (IdDispatchCodec.c) this.byId.get(i);

            try {
                StreamCodec<? super B, V> streamcodec = iddispatchcodec_c.serializer;

                streamcodec.encode(b0, v0);
            } catch (Exception exception) {
                if (exception instanceof IdDispatchCodec.b) {
                    throw exception;
                } else {
                    throw new EncoderException("Failed to encode packet '" + String.valueOf(t0) + "'", exception);
                }
            }
        }
    }

    public static <B extends ByteBuf, V, T> IdDispatchCodec.a<B, V, T> builder(Function<V, ? extends T> function) {
        return new IdDispatchCodec.a<B, V, T>(function);
    }

    public static class a<B extends ByteBuf, V, T> {

        private final List<IdDispatchCodec.c<B, V, T>> entries = new ArrayList();
        private final Function<V, ? extends T> typeGetter;

        a(Function<V, ? extends T> function) {
            this.typeGetter = function;
        }

        public IdDispatchCodec.a<B, V, T> add(T t0, StreamCodec<? super B, ? extends V> streamcodec) {
            this.entries.add(new IdDispatchCodec.c(streamcodec, t0));
            return this;
        }

        public IdDispatchCodec<B, V, T> build() {
            Object2IntOpenHashMap<T> object2intopenhashmap = new Object2IntOpenHashMap();

            object2intopenhashmap.defaultReturnValue(-2);

            for (IdDispatchCodec.c<B, V, T> iddispatchcodec_c : this.entries) {
                int i = object2intopenhashmap.size();
                int j = object2intopenhashmap.putIfAbsent(iddispatchcodec_c.type, i);

                if (j != -2) {
                    throw new IllegalStateException("Duplicate registration for type " + String.valueOf(iddispatchcodec_c.type));
                }
            }

            return new IdDispatchCodec<B, V, T>(this.typeGetter, List.copyOf(this.entries), object2intopenhashmap);
        }
    }

    private static record c<B, V, T>(StreamCodec<? super B, ? extends V> serializer, T type) {

    }

    public interface b {}
}
