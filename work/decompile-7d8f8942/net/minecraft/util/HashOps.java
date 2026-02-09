package net.minecraft.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class HashOps implements DynamicOps<HashCode> {

    private static final byte TAG_EMPTY = 1;
    private static final byte TAG_MAP_START = 2;
    private static final byte TAG_MAP_END = 3;
    private static final byte TAG_LIST_START = 4;
    private static final byte TAG_LIST_END = 5;
    private static final byte TAG_BYTE = 6;
    private static final byte TAG_SHORT = 7;
    private static final byte TAG_INT = 8;
    private static final byte TAG_LONG = 9;
    private static final byte TAG_FLOAT = 10;
    private static final byte TAG_DOUBLE = 11;
    private static final byte TAG_STRING = 12;
    private static final byte TAG_BOOLEAN = 13;
    private static final byte TAG_BYTE_ARRAY_START = 14;
    private static final byte TAG_BYTE_ARRAY_END = 15;
    private static final byte TAG_INT_ARRAY_START = 16;
    private static final byte TAG_INT_ARRAY_END = 17;
    private static final byte TAG_LONG_ARRAY_START = 18;
    private static final byte TAG_LONG_ARRAY_END = 19;
    private static final byte[] EMPTY_PAYLOAD = new byte[]{1};
    private static final byte[] FALSE_PAYLOAD = new byte[]{13, 0};
    private static final byte[] TRUE_PAYLOAD = new byte[]{13, 1};
    public static final byte[] EMPTY_MAP_PAYLOAD = new byte[]{2, 3};
    public static final byte[] EMPTY_LIST_PAYLOAD = new byte[]{4, 5};
    private static final DataResult<Object> UNSUPPORTED_OPERATION_ERROR = DataResult.error(() -> {
        return "Unsupported operation";
    });
    private static final Comparator<HashCode> HASH_COMPARATOR = Comparator.comparingLong(HashCode::padToLong);
    private static final Comparator<Map.Entry<HashCode, HashCode>> MAP_ENTRY_ORDER = Entry.comparingByKey(HashOps.HASH_COMPARATOR).thenComparing(Entry.comparingByValue(HashOps.HASH_COMPARATOR));
    private static final Comparator<Pair<HashCode, HashCode>> MAPLIKE_ENTRY_ORDER = Comparator.comparing(Pair::getFirst, HashOps.HASH_COMPARATOR).thenComparing(Pair::getSecond, HashOps.HASH_COMPARATOR);
    public static final HashOps CRC32C_INSTANCE = new HashOps(Hashing.crc32c());
    final HashFunction hashFunction;
    final HashCode empty;
    private final HashCode emptyMap;
    private final HashCode emptyList;
    private final HashCode trueHash;
    private final HashCode falseHash;

    public HashOps(HashFunction hashfunction) {
        this.hashFunction = hashfunction;
        this.empty = hashfunction.hashBytes(HashOps.EMPTY_PAYLOAD);
        this.emptyMap = hashfunction.hashBytes(HashOps.EMPTY_MAP_PAYLOAD);
        this.emptyList = hashfunction.hashBytes(HashOps.EMPTY_LIST_PAYLOAD);
        this.falseHash = hashfunction.hashBytes(HashOps.FALSE_PAYLOAD);
        this.trueHash = hashfunction.hashBytes(HashOps.TRUE_PAYLOAD);
    }

    public HashCode empty() {
        return this.empty;
    }

    public HashCode emptyMap() {
        return this.emptyMap;
    }

    public HashCode emptyList() {
        return this.emptyList;
    }

    public HashCode createNumeric(Number number) {
        Objects.requireNonNull(number);
        byte b0 = 0;
        HashCode hashcode;

        //$FF: b0->value
        //0->java/lang/Byte
        //1->java/lang/Short
        //2->java/lang/Integer
        //3->java/lang/Long
        //4->java/lang/Double
        //5->java/lang/Float
        switch (number.typeSwitch<invokedynamic>(number, b0)) {
            case 0:
                Byte obyte = (Byte)number;

                hashcode = this.createByte(obyte);
                break;
            case 1:
                Short oshort = (Short)number;

                hashcode = this.createShort(oshort);
                break;
            case 2:
                Integer integer = (Integer)number;

                hashcode = this.createInt(integer);
                break;
            case 3:
                Long olong = (Long)number;

                hashcode = this.createLong(olong);
                break;
            case 4:
                Double odouble = (Double)number;

                hashcode = this.createDouble(odouble);
                break;
            case 5:
                Float ofloat = (Float)number;

                hashcode = this.createFloat(ofloat);
                break;
            default:
                hashcode = this.createDouble(number.doubleValue());
        }

        return hashcode;
    }

    public HashCode createByte(byte b0) {
        return this.hashFunction.newHasher(2).putByte((byte) 6).putByte(b0).hash();
    }

    public HashCode createShort(short short0) {
        return this.hashFunction.newHasher(3).putByte((byte) 7).putShort(short0).hash();
    }

    public HashCode createInt(int i) {
        return this.hashFunction.newHasher(5).putByte((byte) 8).putInt(i).hash();
    }

    public HashCode createLong(long i) {
        return this.hashFunction.newHasher(9).putByte((byte) 9).putLong(i).hash();
    }

    public HashCode createFloat(float f) {
        return this.hashFunction.newHasher(5).putByte((byte) 10).putFloat(f).hash();
    }

    public HashCode createDouble(double d0) {
        return this.hashFunction.newHasher(9).putByte((byte) 11).putDouble(d0).hash();
    }

    public HashCode createString(String s) {
        return this.hashFunction.newHasher().putByte((byte) 12).putInt(s.length()).putUnencodedChars(s).hash();
    }

    public HashCode createBoolean(boolean flag) {
        return flag ? this.trueHash : this.falseHash;
    }

    private static Hasher hashMap(Hasher hasher, Map<HashCode, HashCode> map) {
        hasher.putByte((byte) 2);
        map.entrySet().stream().sorted(HashOps.MAP_ENTRY_ORDER).forEach((entry) -> {
            hasher.putBytes(((HashCode) entry.getKey()).asBytes()).putBytes(((HashCode) entry.getValue()).asBytes());
        });
        hasher.putByte((byte) 3);
        return hasher;
    }

    static Hasher hashMap(Hasher hasher, Stream<Pair<HashCode, HashCode>> stream) {
        hasher.putByte((byte) 2);
        stream.sorted(HashOps.MAPLIKE_ENTRY_ORDER).forEach((pair) -> {
            hasher.putBytes(((HashCode) pair.getFirst()).asBytes()).putBytes(((HashCode) pair.getSecond()).asBytes());
        });
        hasher.putByte((byte) 3);
        return hasher;
    }

    public HashCode createMap(Stream<Pair<HashCode, HashCode>> stream) {
        return hashMap(this.hashFunction.newHasher(), stream).hash();
    }

    public HashCode createMap(Map<HashCode, HashCode> map) {
        return hashMap(this.hashFunction.newHasher(), map).hash();
    }

    public HashCode createList(Stream<HashCode> stream) {
        Hasher hasher = this.hashFunction.newHasher();

        hasher.putByte((byte) 4);
        stream.forEach((hashcode) -> {
            hasher.putBytes(hashcode.asBytes());
        });
        hasher.putByte((byte) 5);
        return hasher.hash();
    }

    public HashCode createByteList(ByteBuffer bytebuffer) {
        Hasher hasher = this.hashFunction.newHasher();

        hasher.putByte((byte) 14);
        hasher.putBytes(bytebuffer);
        hasher.putByte((byte) 15);
        return hasher.hash();
    }

    public HashCode createIntList(IntStream intstream) {
        Hasher hasher = this.hashFunction.newHasher();

        hasher.putByte((byte) 16);
        Objects.requireNonNull(hasher);
        intstream.forEach(hasher::putInt);
        hasher.putByte((byte) 17);
        return hasher.hash();
    }

    public HashCode createLongList(LongStream longstream) {
        Hasher hasher = this.hashFunction.newHasher();

        hasher.putByte((byte) 18);
        Objects.requireNonNull(hasher);
        longstream.forEach(hasher::putLong);
        hasher.putByte((byte) 19);
        return hasher.hash();
    }

    public HashCode remove(HashCode hashcode, String s) {
        return hashcode;
    }

    public RecordBuilder<HashCode> mapBuilder() {
        return new HashOps.b();
    }

    public ListBuilder<HashCode> listBuilder() {
        return new HashOps.a();
    }

    public String toString() {
        return "Hash " + String.valueOf(this.hashFunction);
    }

    public <U> U convertTo(DynamicOps<U> dynamicops, HashCode hashcode) {
        throw new UnsupportedOperationException("Can't convert from this type");
    }

    public Number getNumberValue(HashCode hashcode, Number number) {
        return number;
    }

    public HashCode set(HashCode hashcode, String s, HashCode hashcode1) {
        return hashcode;
    }

    public HashCode update(HashCode hashcode, String s, Function<HashCode, HashCode> function) {
        return hashcode;
    }

    public HashCode updateGeneric(HashCode hashcode, HashCode hashcode1, Function<HashCode, HashCode> function) {
        return hashcode;
    }

    private static <T> DataResult<T> unsupported() {
        return HashOps.UNSUPPORTED_OPERATION_ERROR;
    }

    public DataResult<HashCode> get(HashCode hashcode, String s) {
        return unsupported();
    }

    public DataResult<HashCode> getGeneric(HashCode hashcode, HashCode hashcode1) {
        return unsupported();
    }

    public DataResult<Number> getNumberValue(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<Boolean> getBooleanValue(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<String> getStringValue(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<HashCode> mergeToList(HashCode hashcode, HashCode hashcode1) {
        return unsupported();
    }

    public DataResult<HashCode> mergeToList(HashCode hashcode, List<HashCode> list) {
        return unsupported();
    }

    public DataResult<HashCode> mergeToMap(HashCode hashcode, HashCode hashcode1, HashCode hashcode2) {
        return unsupported();
    }

    public DataResult<HashCode> mergeToMap(HashCode hashcode, Map<HashCode, HashCode> map) {
        return unsupported();
    }

    public DataResult<HashCode> mergeToMap(HashCode hashcode, MapLike<HashCode> maplike) {
        return unsupported();
    }

    public DataResult<Stream<Pair<HashCode, HashCode>>> getMapValues(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<Consumer<BiConsumer<HashCode, HashCode>>> getMapEntries(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<Stream<HashCode>> getStream(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<Consumer<Consumer<HashCode>>> getList(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<MapLike<HashCode>> getMap(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<ByteBuffer> getByteBuffer(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<IntStream> getIntStream(HashCode hashcode) {
        return unsupported();
    }

    public DataResult<LongStream> getLongStream(HashCode hashcode) {
        return unsupported();
    }

    private final class b extends RecordBuilder.AbstractUniversalBuilder<HashCode, List<Pair<HashCode, HashCode>>> {

        public b() {
            super(HashOps.this);
        }

        protected List<Pair<HashCode, HashCode>> initBuilder() {
            return new ArrayList();
        }

        protected List<Pair<HashCode, HashCode>> append(HashCode hashcode, HashCode hashcode1, List<Pair<HashCode, HashCode>> list) {
            list.add(Pair.of(hashcode, hashcode1));
            return list;
        }

        protected DataResult<HashCode> build(List<Pair<HashCode, HashCode>> list, HashCode hashcode) {
            assert hashcode.equals(HashOps.this.empty());

            return DataResult.success(HashOps.hashMap(HashOps.this.hashFunction.newHasher(), list.stream()).hash());
        }
    }

    private class a extends AbstractListBuilder<HashCode, Hasher> {

        public a() {
            super(HashOps.this);
        }

        @Override
        protected Hasher initBuilder() {
            return HashOps.this.hashFunction.newHasher().putByte((byte) 4);
        }

        protected Hasher append(Hasher hasher, HashCode hashcode) {
            return hasher.putBytes(hashcode.asBytes());
        }

        protected DataResult<HashCode> build(Hasher hasher, HashCode hashcode) {
            assert hashcode.equals(HashOps.this.empty);

            hasher.putByte((byte) 5);
            return DataResult.success(hasher.hash());
        }
    }
}
