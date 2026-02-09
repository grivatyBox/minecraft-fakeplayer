package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class JavaOps implements DynamicOps<Object> {

    public static final JavaOps INSTANCE = new JavaOps();

    private JavaOps() {}

    public Object empty() {
        return null;
    }

    public Object emptyMap() {
        return Map.of();
    }

    public Object emptyList() {
        return List.of();
    }

    public <U> U convertTo(DynamicOps<U> dynamicops, Object object) {
        if (object == null) {
            return dynamicops.empty();
        } else if (object instanceof Map) {
            return this.convertMap(dynamicops, object);
        } else if (object instanceof ByteList) {
            ByteList bytelist = (ByteList) object;

            return dynamicops.createByteList(ByteBuffer.wrap(bytelist.toByteArray()));
        } else if (object instanceof IntList) {
            IntList intlist = (IntList) object;

            return dynamicops.createIntList(intlist.intStream());
        } else if (object instanceof LongList) {
            LongList longlist = (LongList) object;

            return dynamicops.createLongList(longlist.longStream());
        } else if (object instanceof List) {
            return this.convertList(dynamicops, object);
        } else if (object instanceof String) {
            String s = (String) object;

            return dynamicops.createString(s);
        } else if (object instanceof Boolean) {
            Boolean obool = (Boolean) object;

            return dynamicops.createBoolean(obool);
        } else if (object instanceof Byte) {
            Byte obyte = (Byte) object;

            return dynamicops.createByte(obyte);
        } else if (object instanceof Short) {
            Short oshort = (Short) object;

            return dynamicops.createShort(oshort);
        } else if (object instanceof Integer) {
            Integer integer = (Integer) object;

            return dynamicops.createInt(integer);
        } else if (object instanceof Long) {
            Long olong = (Long) object;

            return dynamicops.createLong(olong);
        } else if (object instanceof Float) {
            Float ofloat = (Float) object;

            return dynamicops.createFloat(ofloat);
        } else if (object instanceof Double) {
            Double odouble = (Double) object;

            return dynamicops.createDouble(odouble);
        } else if (object instanceof Number) {
            Number number = (Number) object;

            return dynamicops.createNumeric(number);
        } else {
            throw new IllegalStateException("Don't know how to convert " + object);
        }
    }

    public DataResult<Number> getNumberValue(Object object) {
        if (object instanceof Number) {
            Number number = (Number) object;

            return DataResult.success(number);
        } else {
            return DataResult.error(() -> {
                return "Not a number: " + object;
            });
        }
    }

    public Object createNumeric(Number number) {
        return number;
    }

    public Object createByte(byte b0) {
        return b0;
    }

    public Object createShort(short short0) {
        return short0;
    }

    public Object createInt(int i) {
        return i;
    }

    public Object createLong(long i) {
        return i;
    }

    public Object createFloat(float f) {
        return f;
    }

    public Object createDouble(double d0) {
        return d0;
    }

    public DataResult<Boolean> getBooleanValue(Object object) {
        if (object instanceof Boolean) {
            Boolean obool = (Boolean) object;

            return DataResult.success(obool);
        } else {
            return DataResult.error(() -> {
                return "Not a boolean: " + object;
            });
        }
    }

    public Object createBoolean(boolean flag) {
        return flag;
    }

    public DataResult<String> getStringValue(Object object) {
        if (object instanceof String) {
            String s = (String) object;

            return DataResult.success(s);
        } else {
            return DataResult.error(() -> {
                return "Not a string: " + object;
            });
        }
    }

    public Object createString(String s) {
        return s;
    }

    public DataResult<Object> mergeToList(Object object, Object object1) {
        if (object == this.empty()) {
            return DataResult.success(List.of(object1));
        } else if (object instanceof List) {
            List<?> list = (List) object;

            return list.isEmpty() ? DataResult.success(List.of(object1)) : DataResult.success(ImmutableList.builder().addAll(list).add(object1).build());
        } else {
            return DataResult.error(() -> {
                return "Not a list: " + object;
            });
        }
    }

    public DataResult<Object> mergeToList(Object object, List<Object> list) {
        if (object == this.empty()) {
            return DataResult.success(list);
        } else if (object instanceof List) {
            List<?> list1 = (List) object;

            return list1.isEmpty() ? DataResult.success(list) : DataResult.success(ImmutableList.builder().addAll(list1).addAll(list).build());
        } else {
            return DataResult.error(() -> {
                return "Not a list: " + object;
            });
        }
    }

    public DataResult<Object> mergeToMap(Object object, Object object1, Object object2) {
        if (object == this.empty()) {
            return DataResult.success(Map.of(object1, object2));
        } else if (object instanceof Map) {
            Map<?, ?> map = (Map) object;

            if (map.isEmpty()) {
                return DataResult.success(Map.of(object1, object2));
            } else {
                Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size() + 1);

                builder.putAll(map);
                builder.put(object1, object2);
                return DataResult.success(builder.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    public DataResult<Object> mergeToMap(Object object, Map<Object, Object> map) {
        if (object == this.empty()) {
            return DataResult.success(map);
        } else if (object instanceof Map) {
            Map<?, ?> map1 = (Map) object;

            if (map1.isEmpty()) {
                return DataResult.success(map);
            } else {
                Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map1.size() + map.size());

                builder.putAll(map1);
                builder.putAll(map);
                return DataResult.success(builder.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    private static Map<Object, Object> mapLikeToMap(MapLike<Object> maplike) {
        return (Map) maplike.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    public DataResult<Object> mergeToMap(Object object, MapLike<Object> maplike) {
        if (object == this.empty()) {
            return DataResult.success(mapLikeToMap(maplike));
        } else if (object instanceof Map) {
            Map<?, ?> map = (Map) object;

            if (map.isEmpty()) {
                return DataResult.success(mapLikeToMap(maplike));
            } else {
                Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size());

                builder.putAll(map);
                maplike.entries().forEach((pair) -> {
                    builder.put(pair.getFirst(), pair.getSecond());
                });
                return DataResult.success(builder.buildKeepingLast());
            }
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    static Stream<Pair<Object, Object>> getMapEntries(Map<?, ?> map) {
        return map.entrySet().stream().map((entry) -> {
            return Pair.of(entry.getKey(), entry.getValue());
        });
    }

    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object object) {
        if (object instanceof Map) {
            Map<?, ?> map = (Map) object;

            return DataResult.success(getMapEntries(map));
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    public DataResult<Consumer<BiConsumer<Object, Object>>> getMapEntries(Object object) {
        if (object instanceof Map) {
            Map<?, ?> map = (Map) object;

            Objects.requireNonNull(map);
            return DataResult.success(map::forEach);
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    public Object createMap(Stream<Pair<Object, Object>> stream) {
        return stream.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
    }

    public DataResult<MapLike<Object>> getMap(Object object) {
        if (object instanceof Map) {
            final Map<?, ?> map = (Map) object;

            return DataResult.success(new MapLike<Object>() {
                @Nullable
                public Object get(Object object1) {
                    return map.get(object1);
                }

                @Nullable
                public Object get(String s) {
                    return map.get(s);
                }

                public Stream<Pair<Object, Object>> entries() {
                    return JavaOps.getMapEntries(map);
                }

                public String toString() {
                    return "MapLike[" + map + "]";
                }
            });
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    public Object createMap(Map<Object, Object> map) {
        return map;
    }

    public DataResult<Stream<Object>> getStream(Object object) {
        if (object instanceof List) {
            List<?> list = (List) object;

            return DataResult.success(list.stream().map((object1) -> {
                return object1;
            }));
        } else {
            return DataResult.error(() -> {
                return "Not an list: " + object;
            });
        }
    }

    public DataResult<Consumer<Consumer<Object>>> getList(Object object) {
        if (object instanceof List) {
            List<?> list = (List) object;

            Objects.requireNonNull(list);
            return DataResult.success(list::forEach);
        } else {
            return DataResult.error(() -> {
                return "Not an list: " + object;
            });
        }
    }

    public Object createList(Stream<Object> stream) {
        return stream.toList();
    }

    public DataResult<ByteBuffer> getByteBuffer(Object object) {
        if (object instanceof ByteList) {
            ByteList bytelist = (ByteList) object;

            return DataResult.success(ByteBuffer.wrap(bytelist.toByteArray()));
        } else {
            return DataResult.error(() -> {
                return "Not a byte list: " + object;
            });
        }
    }

    public Object createByteList(ByteBuffer bytebuffer) {
        ByteBuffer bytebuffer1 = bytebuffer.duplicate().clear();
        ByteArrayList bytearraylist = new ByteArrayList();

        bytearraylist.size(bytebuffer1.capacity());
        bytebuffer1.get(0, bytearraylist.elements(), 0, bytearraylist.size());
        return bytearraylist;
    }

    public DataResult<IntStream> getIntStream(Object object) {
        if (object instanceof IntList) {
            IntList intlist = (IntList) object;

            return DataResult.success(intlist.intStream());
        } else {
            return DataResult.error(() -> {
                return "Not an int list: " + object;
            });
        }
    }

    public Object createIntList(IntStream intstream) {
        return IntArrayList.toList(intstream);
    }

    public DataResult<LongStream> getLongStream(Object object) {
        if (object instanceof LongList) {
            LongList longlist = (LongList) object;

            return DataResult.success(longlist.longStream());
        } else {
            return DataResult.error(() -> {
                return "Not a long list: " + object;
            });
        }
    }

    public Object createLongList(LongStream longstream) {
        return LongArrayList.toList(longstream);
    }

    public Object remove(Object object, String s) {
        if (object instanceof Map) {
            Map<?, ?> map = (Map) object;
            Map<Object, Object> map1 = new LinkedHashMap(map);

            map1.remove(s);
            return DataResult.success(Map.copyOf(map1));
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + object;
            });
        }
    }

    public RecordBuilder<Object> mapBuilder() {
        return new JavaOps.a<>(this);
    }

    public String toString() {
        return "Java";
    }

    private static final class a<T> extends AbstractUniversalBuilder<T, Builder<T, T>> {

        public a(DynamicOps<T> dynamicops) {
            super(dynamicops);
        }

        protected Builder<T, T> initBuilder() {
            return ImmutableMap.builder();
        }

        protected Builder<T, T> append(T t0, T t1, Builder<T, T> builder) {
            return builder.put(t0, t1);
        }

        protected DataResult<T> build(Builder<T, T> builder, T t0) {
            ImmutableMap immutablemap;

            try {
                immutablemap = builder.buildOrThrow();
            } catch (IllegalArgumentException illegalargumentexception) {
                return DataResult.error(() -> {
                    return "Can't build map: " + illegalargumentexception.getMessage();
                });
            }

            return this.ops().mergeToMap(t0, immutablemap);
        }
    }
}
