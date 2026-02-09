package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class DynamicOpsWrapper<T> implements DynamicOps<T> {

    protected final DynamicOps<T> delegate;

    protected DynamicOpsWrapper(DynamicOps<T> dynamicops) {
        this.delegate = dynamicops;
    }

    public T empty() {
        return (T) this.delegate.empty();
    }

    public T emptyMap() {
        return (T) this.delegate.emptyMap();
    }

    public T emptyList() {
        return (T) this.delegate.emptyList();
    }

    public <U> U convertTo(DynamicOps<U> dynamicops, T t0) {
        return Objects.equals(dynamicops, this.delegate) ? t0 : this.delegate.convertTo(dynamicops, t0);
    }

    public DataResult<Number> getNumberValue(T t0) {
        return this.delegate.getNumberValue(t0);
    }

    public T createNumeric(Number number) {
        return (T) this.delegate.createNumeric(number);
    }

    public T createByte(byte b0) {
        return (T) this.delegate.createByte(b0);
    }

    public T createShort(short short0) {
        return (T) this.delegate.createShort(short0);
    }

    public T createInt(int i) {
        return (T) this.delegate.createInt(i);
    }

    public T createLong(long i) {
        return (T) this.delegate.createLong(i);
    }

    public T createFloat(float f) {
        return (T) this.delegate.createFloat(f);
    }

    public T createDouble(double d0) {
        return (T) this.delegate.createDouble(d0);
    }

    public DataResult<Boolean> getBooleanValue(T t0) {
        return this.delegate.getBooleanValue(t0);
    }

    public T createBoolean(boolean flag) {
        return (T) this.delegate.createBoolean(flag);
    }

    public DataResult<String> getStringValue(T t0) {
        return this.delegate.getStringValue(t0);
    }

    public T createString(String s) {
        return (T) this.delegate.createString(s);
    }

    public DataResult<T> mergeToList(T t0, T t1) {
        return this.delegate.mergeToList(t0, t1);
    }

    public DataResult<T> mergeToList(T t0, List<T> list) {
        return this.delegate.mergeToList(t0, list);
    }

    public DataResult<T> mergeToMap(T t0, T t1, T t2) {
        return this.delegate.mergeToMap(t0, t1, t2);
    }

    public DataResult<T> mergeToMap(T t0, MapLike<T> maplike) {
        return this.delegate.mergeToMap(t0, maplike);
    }

    public DataResult<T> mergeToMap(T t0, Map<T, T> map) {
        return this.delegate.mergeToMap(t0, map);
    }

    public DataResult<T> mergeToPrimitive(T t0, T t1) {
        return this.delegate.mergeToPrimitive(t0, t1);
    }

    public DataResult<Stream<Pair<T, T>>> getMapValues(T t0) {
        return this.delegate.getMapValues(t0);
    }

    public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T t0) {
        return this.delegate.getMapEntries(t0);
    }

    public T createMap(Map<T, T> map) {
        return (T) this.delegate.createMap(map);
    }

    public T createMap(Stream<Pair<T, T>> stream) {
        return (T) this.delegate.createMap(stream);
    }

    public DataResult<MapLike<T>> getMap(T t0) {
        return this.delegate.getMap(t0);
    }

    public DataResult<Stream<T>> getStream(T t0) {
        return this.delegate.getStream(t0);
    }

    public DataResult<Consumer<Consumer<T>>> getList(T t0) {
        return this.delegate.getList(t0);
    }

    public T createList(Stream<T> stream) {
        return (T) this.delegate.createList(stream);
    }

    public DataResult<ByteBuffer> getByteBuffer(T t0) {
        return this.delegate.getByteBuffer(t0);
    }

    public T createByteList(ByteBuffer bytebuffer) {
        return (T) this.delegate.createByteList(bytebuffer);
    }

    public DataResult<IntStream> getIntStream(T t0) {
        return this.delegate.getIntStream(t0);
    }

    public T createIntList(IntStream intstream) {
        return (T) this.delegate.createIntList(intstream);
    }

    public DataResult<LongStream> getLongStream(T t0) {
        return this.delegate.getLongStream(t0);
    }

    public T createLongList(LongStream longstream) {
        return (T) this.delegate.createLongList(longstream);
    }

    public T remove(T t0, String s) {
        return (T) this.delegate.remove(t0, s);
    }

    public boolean compressMaps() {
        return this.delegate.compressMaps();
    }

    public ListBuilder<T> listBuilder() {
        return new DynamicOpsWrapper.a(this.delegate.listBuilder());
    }

    public RecordBuilder<T> mapBuilder() {
        return new DynamicOpsWrapper.b(this.delegate.mapBuilder());
    }

    protected class a implements ListBuilder<T> {

        private final ListBuilder<T> original;

        protected a(final ListBuilder listbuilder) {
            this.original = listbuilder;
        }

        public DynamicOps<T> ops() {
            return DynamicOpsWrapper.this;
        }

        public DataResult<T> build(T t0) {
            return this.original.build(t0);
        }

        public ListBuilder<T> add(T t0) {
            this.original.add(t0);
            return this;
        }

        public ListBuilder<T> add(DataResult<T> dataresult) {
            this.original.add(dataresult);
            return this;
        }

        public <E> ListBuilder<T> add(E e0, Encoder<E> encoder) {
            this.original.add(encoder.encodeStart(this.ops(), e0));
            return this;
        }

        public <E> ListBuilder<T> addAll(Iterable<E> iterable, Encoder<E> encoder) {
            iterable.forEach((object) -> {
                this.original.add(encoder.encode(object, this.ops(), this.ops().empty()));
            });
            return this;
        }

        public ListBuilder<T> withErrorsFrom(DataResult<?> dataresult) {
            this.original.withErrorsFrom(dataresult);
            return this;
        }

        public ListBuilder<T> mapError(UnaryOperator<String> unaryoperator) {
            this.original.mapError(unaryoperator);
            return this;
        }

        public DataResult<T> build(DataResult<T> dataresult) {
            return this.original.build(dataresult);
        }
    }

    protected class b implements RecordBuilder<T> {

        private final RecordBuilder<T> original;

        protected b(final RecordBuilder recordbuilder) {
            this.original = recordbuilder;
        }

        public DynamicOps<T> ops() {
            return DynamicOpsWrapper.this;
        }

        public RecordBuilder<T> add(T t0, T t1) {
            this.original.add(t0, t1);
            return this;
        }

        public RecordBuilder<T> add(T t0, DataResult<T> dataresult) {
            this.original.add(t0, dataresult);
            return this;
        }

        public RecordBuilder<T> add(DataResult<T> dataresult, DataResult<T> dataresult1) {
            this.original.add(dataresult, dataresult1);
            return this;
        }

        public RecordBuilder<T> add(String s, T t0) {
            this.original.add(s, t0);
            return this;
        }

        public RecordBuilder<T> add(String s, DataResult<T> dataresult) {
            this.original.add(s, dataresult);
            return this;
        }

        public <E> RecordBuilder<T> add(String s, E e0, Encoder<E> encoder) {
            return this.original.add(s, encoder.encodeStart(this.ops(), e0));
        }

        public RecordBuilder<T> withErrorsFrom(DataResult<?> dataresult) {
            this.original.withErrorsFrom(dataresult);
            return this;
        }

        public RecordBuilder<T> setLifecycle(Lifecycle lifecycle) {
            this.original.setLifecycle(lifecycle);
            return this;
        }

        public RecordBuilder<T> mapError(UnaryOperator<String> unaryoperator) {
            this.original.mapError(unaryoperator);
            return this;
        }

        public DataResult<T> build(T t0) {
            return this.original.build(t0);
        }

        public DataResult<T> build(DataResult<T> dataresult) {
            return this.original.build(dataresult);
        }
    }
}
