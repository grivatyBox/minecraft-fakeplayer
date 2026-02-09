package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import org.slf4j.Logger;

public final class NBTTagCompound implements NBTBase {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<NBTTagCompound> CODEC = Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
        NBTBase nbtbase = (NBTBase) dynamic.convert(DynamicOpsNBT.INSTANCE).getValue();

        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            return DataResult.success(nbttagcompound == dynamic.getValue() ? nbttagcompound.copy() : nbttagcompound);
        } else {
            return DataResult.error(() -> {
                return "Not a compound tag: " + String.valueOf(nbtbase);
            });
        }
    }, (nbttagcompound) -> {
        return new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.copy());
    });
    private static final int SELF_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
    public static final NBTTagType<NBTTagCompound> TYPE = new NBTTagType.b<NBTTagCompound>() {
        @Override
        public NBTTagCompound load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.pushDepth();

            NBTTagCompound nbttagcompound;

            try {
                nbttagcompound = loadCompound(datainput, nbtreadlimiter);
            } finally {
                nbtreadlimiter.popDepth();
            }

            return nbttagcompound;
        }

        private static NBTTagCompound loadCompound(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(48L);
            Map<String, NBTBase> map = Maps.newHashMap();

            byte b0;

            while ((b0 = datainput.readByte()) != 0) {
                String s = readString(datainput, nbtreadlimiter);
                NBTBase nbtbase = NBTTagCompound.readNamedTagData(NBTTagTypes.getType(b0), s, datainput, nbtreadlimiter);

                if (map.put(s, nbtbase) == null) {
                    nbtreadlimiter.accountBytes(36L);
                }
            }

            return new NBTTagCompound(map);
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.pushDepth();

            StreamTagVisitor.b streamtagvisitor_b;

            try {
                streamtagvisitor_b = parseCompound(datainput, streamtagvisitor, nbtreadlimiter);
            } finally {
                nbtreadlimiter.popDepth();
            }

            return streamtagvisitor_b;
        }

        private static StreamTagVisitor.b parseCompound(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(48L);

            while (true) {
                byte b0;

                if ((b0 = datainput.readByte()) != 0) {
                    NBTTagType<?> nbttagtype = NBTTagTypes.getType(b0);

                    switch (streamtagvisitor.visitEntry(nbttagtype)) {
                        case HALT:
                            return StreamTagVisitor.b.HALT;
                        case BREAK:
                            NBTTagString.skipString(datainput);
                            nbttagtype.skip(datainput, nbtreadlimiter);
                            break;
                        case SKIP:
                            NBTTagString.skipString(datainput);
                            nbttagtype.skip(datainput, nbtreadlimiter);
                            continue;
                        default:
                            String s = readString(datainput, nbtreadlimiter);

                            switch (streamtagvisitor.visitEntry(nbttagtype, s)) {
                                case HALT:
                                    return StreamTagVisitor.b.HALT;
                                case BREAK:
                                    nbttagtype.skip(datainput, nbtreadlimiter);
                                    break;
                                case SKIP:
                                    nbttagtype.skip(datainput, nbtreadlimiter);
                                    continue;
                                default:
                                    nbtreadlimiter.accountBytes(36L);
                                    switch (nbttagtype.parse(datainput, streamtagvisitor, nbtreadlimiter)) {
                                        case HALT:
                                            return StreamTagVisitor.b.HALT;
                                        case BREAK:
                                        default:
                                            continue;
                                    }
                            }
                    }
                }

                if (b0 != 0) {
                    while ((b0 = datainput.readByte()) != 0) {
                        NBTTagString.skipString(datainput);
                        NBTTagTypes.getType(b0).skip(datainput, nbtreadlimiter);
                    }
                }

                return streamtagvisitor.visitContainerEnd();
            }
        }

        private static String readString(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            String s = datainput.readUTF();

            nbtreadlimiter.accountBytes(28L);
            nbtreadlimiter.accountBytes(2L, (long) s.length());
            return s;
        }

        @Override
        public void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.pushDepth();

            byte b0;

            try {
                while ((b0 = datainput.readByte()) != 0) {
                    NBTTagString.skipString(datainput);
                    NBTTagTypes.getType(b0).skip(datainput, nbtreadlimiter);
                }
            } finally {
                nbtreadlimiter.popDepth();
            }

        }

        @Override
        public String getName() {
            return "COMPOUND";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Compound";
        }
    };
    private final Map<String, NBTBase> tags;

    NBTTagCompound(Map<String, NBTBase> map) {
        this.tags = map;
    }

    public NBTTagCompound() {
        this(new HashMap());
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        for (String s : this.tags.keySet()) {
            NBTBase nbtbase = (NBTBase) this.tags.get(s);

            writeNamedTag(s, nbtbase, dataoutput);
        }

        dataoutput.writeByte(0);
    }

    @Override
    public int sizeInBytes() {
        int i = 48;

        for (Map.Entry<String, NBTBase> map_entry : this.tags.entrySet()) {
            i += 28 + 2 * ((String) map_entry.getKey()).length();
            i += 36;
            i += ((NBTBase) map_entry.getValue()).sizeInBytes();
        }

        return i;
    }

    public Set<String> keySet() {
        return this.tags.keySet();
    }

    public Set<Map.Entry<String, NBTBase>> entrySet() {
        return this.tags.entrySet();
    }

    public Collection<NBTBase> values() {
        return this.tags.values();
    }

    public void forEach(BiConsumer<String, NBTBase> biconsumer) {
        this.tags.forEach(biconsumer);
    }

    @Override
    public byte getId() {
        return 10;
    }

    @Override
    public NBTTagType<NBTTagCompound> getType() {
        return NBTTagCompound.TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    @Nullable
    public NBTBase put(String s, NBTBase nbtbase) {
        return (NBTBase) this.tags.put(s, nbtbase);
    }

    public void putByte(String s, byte b0) {
        this.tags.put(s, NBTTagByte.valueOf(b0));
    }

    public void putShort(String s, short short0) {
        this.tags.put(s, NBTTagShort.valueOf(short0));
    }

    public void putInt(String s, int i) {
        this.tags.put(s, NBTTagInt.valueOf(i));
    }

    public void putLong(String s, long i) {
        this.tags.put(s, NBTTagLong.valueOf(i));
    }

    public void putFloat(String s, float f) {
        this.tags.put(s, NBTTagFloat.valueOf(f));
    }

    public void putDouble(String s, double d0) {
        this.tags.put(s, NBTTagDouble.valueOf(d0));
    }

    public void putString(String s, String s1) {
        this.tags.put(s, NBTTagString.valueOf(s1));
    }

    public void putByteArray(String s, byte[] abyte) {
        this.tags.put(s, new NBTTagByteArray(abyte));
    }

    public void putIntArray(String s, int[] aint) {
        this.tags.put(s, new NBTTagIntArray(aint));
    }

    public void putLongArray(String s, long[] along) {
        this.tags.put(s, new NBTTagLongArray(along));
    }

    public void putBoolean(String s, boolean flag) {
        this.tags.put(s, NBTTagByte.valueOf(flag));
    }

    @Nullable
    public NBTBase get(String s) {
        return (NBTBase) this.tags.get(s);
    }

    public boolean contains(String s) {
        return this.tags.containsKey(s);
    }

    private Optional<NBTBase> getOptional(String s) {
        return Optional.ofNullable((NBTBase) this.tags.get(s));
    }

    public Optional<Byte> getByte(String s) {
        return this.getOptional(s).flatMap(NBTBase::asByte);
    }

    public byte getByteOr(String s, byte b0) {
        Object object = this.tags.get(s);

        if (object instanceof NBTNumber nbtnumber) {
            return nbtnumber.byteValue();
        } else {
            return b0;
        }
    }

    public Optional<Short> getShort(String s) {
        return this.getOptional(s).flatMap(NBTBase::asShort);
    }

    public short getShortOr(String s, short short0) {
        Object object = this.tags.get(s);

        if (object instanceof NBTNumber nbtnumber) {
            return nbtnumber.shortValue();
        } else {
            return short0;
        }
    }

    public Optional<Integer> getInt(String s) {
        return this.getOptional(s).flatMap(NBTBase::asInt);
    }

    public int getIntOr(String s, int i) {
        Object object = this.tags.get(s);

        if (object instanceof NBTNumber nbtnumber) {
            return nbtnumber.intValue();
        } else {
            return i;
        }
    }

    public Optional<Long> getLong(String s) {
        return this.getOptional(s).flatMap(NBTBase::asLong);
    }

    public long getLongOr(String s, long i) {
        Object object = this.tags.get(s);

        if (object instanceof NBTNumber nbtnumber) {
            return nbtnumber.longValue();
        } else {
            return i;
        }
    }

    public Optional<Float> getFloat(String s) {
        return this.getOptional(s).flatMap(NBTBase::asFloat);
    }

    public float getFloatOr(String s, float f) {
        Object object = this.tags.get(s);

        if (object instanceof NBTNumber nbtnumber) {
            return nbtnumber.floatValue();
        } else {
            return f;
        }
    }

    public Optional<Double> getDouble(String s) {
        return this.getOptional(s).flatMap(NBTBase::asDouble);
    }

    public double getDoubleOr(String s, double d0) {
        Object object = this.tags.get(s);

        if (object instanceof NBTNumber nbtnumber) {
            return nbtnumber.doubleValue();
        } else {
            return d0;
        }
    }

    public Optional<String> getString(String s) {
        return this.getOptional(s).flatMap(NBTBase::asString);
    }

    public String getStringOr(String s, String s1) {
        Object object = this.tags.get(s);

        if (object instanceof NBTTagString nbttagstring) {
            NBTTagString nbttagstring1 = nbttagstring;

            try {
                s2 = nbttagstring1.value();
            } catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }

            String s3 = s2;

            return s3;
        } else {
            return s1;
        }
    }

    public Optional<byte[]> getByteArray(String s) {
        Object object = this.tags.get(s);

        if (object instanceof NBTTagByteArray nbttagbytearray) {
            return Optional.of(nbttagbytearray.getAsByteArray());
        } else {
            return Optional.empty();
        }
    }

    public Optional<int[]> getIntArray(String s) {
        Object object = this.tags.get(s);

        if (object instanceof NBTTagIntArray nbttagintarray) {
            return Optional.of(nbttagintarray.getAsIntArray());
        } else {
            return Optional.empty();
        }
    }

    public Optional<long[]> getLongArray(String s) {
        Object object = this.tags.get(s);

        if (object instanceof NBTTagLongArray nbttaglongarray) {
            return Optional.of(nbttaglongarray.getAsLongArray());
        } else {
            return Optional.empty();
        }
    }

    public Optional<NBTTagCompound> getCompound(String s) {
        Object object = this.tags.get(s);

        if (object instanceof NBTTagCompound nbttagcompound) {
            return Optional.of(nbttagcompound);
        } else {
            return Optional.empty();
        }
    }

    public NBTTagCompound getCompoundOrEmpty(String s) {
        return (NBTTagCompound) this.getCompound(s).orElseGet(NBTTagCompound::new);
    }

    public Optional<NBTTagList> getList(String s) {
        Object object = this.tags.get(s);

        if (object instanceof NBTTagList nbttaglist) {
            return Optional.of(nbttaglist);
        } else {
            return Optional.empty();
        }
    }

    public NBTTagList getListOrEmpty(String s) {
        return (NBTTagList) this.getList(s).orElseGet(NBTTagList::new);
    }

    public Optional<Boolean> getBoolean(String s) {
        return this.getOptional(s).flatMap(NBTBase::asBoolean);
    }

    public boolean getBooleanOr(String s, boolean flag) {
        return this.getByteOr(s, (byte) (flag ? 1 : 0)) != 0;
    }

    public void remove(String s) {
        this.tags.remove(s);
    }

    @Override
    public String toString() {
        StringTagVisitor stringtagvisitor = new StringTagVisitor();

        stringtagvisitor.visitCompound(this);
        return stringtagvisitor.build();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    protected NBTTagCompound shallowCopy() {
        return new NBTTagCompound(new HashMap(this.tags));
    }

    @Override
    public NBTTagCompound copy() {
        HashMap<String, NBTBase> hashmap = new HashMap();

        this.tags.forEach((s, nbtbase) -> {
            hashmap.put(s, nbtbase.copy());
        });
        return new NBTTagCompound(hashmap);
    }

    @Override
    public Optional<NBTTagCompound> asCompound() {
        return Optional.of(this);
    }

    public boolean equals(Object object) {
        return this == object ? true : object instanceof NBTTagCompound && Objects.equals(this.tags, ((NBTTagCompound) object).tags);
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String s, NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getId());
        if (nbtbase.getId() != 0) {
            dataoutput.writeUTF(s);
            nbtbase.write(dataoutput);
        }
    }

    static NBTBase readNamedTagData(NBTTagType<?> nbttagtype, String s, DataInput datainput, NBTReadLimiter nbtreadlimiter) {
        try {
            return nbttagtype.load(datainput, nbtreadlimiter);
        } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("NBT Tag");

            crashreportsystemdetails.setDetail("Tag name", s);
            crashreportsystemdetails.setDetail("Tag type", nbttagtype.getName());
            throw new ReportedNbtException(crashreport);
        }
    }

    public NBTTagCompound merge(NBTTagCompound nbttagcompound) {
        for (String s : nbttagcompound.tags.keySet()) {
            NBTBase nbtbase = (NBTBase) nbttagcompound.tags.get(s);

            if (nbtbase instanceof NBTTagCompound nbttagcompound1) {
                Object object = this.tags.get(s);

                if (object instanceof NBTTagCompound nbttagcompound2) {
                    nbttagcompound2.merge(nbttagcompound1);
                    continue;
                }
            }

            this.put(s, nbtbase.copy());
        }

        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitCompound(this);
    }

    @Override
    public StreamTagVisitor.b accept(StreamTagVisitor streamtagvisitor) {
        for (Map.Entry<String, NBTBase> map_entry : this.tags.entrySet()) {
            NBTBase nbtbase = (NBTBase) map_entry.getValue();
            NBTTagType<?> nbttagtype = nbtbase.getType();
            StreamTagVisitor.a streamtagvisitor_a = streamtagvisitor.visitEntry(nbttagtype);

            switch (streamtagvisitor_a) {
                case HALT:
                    return StreamTagVisitor.b.HALT;
                case BREAK:
                    return streamtagvisitor.visitContainerEnd();
                case SKIP:
                    break;
                default:
                    streamtagvisitor_a = streamtagvisitor.visitEntry(nbttagtype, (String) map_entry.getKey());
                    switch (streamtagvisitor_a) {
                        case HALT:
                            return StreamTagVisitor.b.HALT;
                        case BREAK:
                            return streamtagvisitor.visitContainerEnd();
                        case SKIP:
                            break;
                        default:
                            StreamTagVisitor.b streamtagvisitor_b = nbtbase.accept(streamtagvisitor);

                            switch (streamtagvisitor_b) {
                                case HALT:
                                    return StreamTagVisitor.b.HALT;
                                case BREAK:
                                    return streamtagvisitor.visitContainerEnd();
                            }
                    }
            }
        }

        return streamtagvisitor.visitContainerEnd();
    }

    public <T> void store(String s, Codec<T> codec, T t0) {
        this.store(s, codec, DynamicOpsNBT.INSTANCE, t0);
    }

    public <T> void storeNullable(String s, Codec<T> codec, @Nullable T t0) {
        if (t0 != null) {
            this.store(s, codec, t0);
        }

    }

    public <T> void store(String s, Codec<T> codec, DynamicOps<NBTBase> dynamicops, T t0) {
        this.put(s, (NBTBase) codec.encodeStart(dynamicops, t0).getOrThrow());
    }

    public <T> void storeNullable(String s, Codec<T> codec, DynamicOps<NBTBase> dynamicops, @Nullable T t0) {
        if (t0 != null) {
            this.store(s, codec, dynamicops, t0);
        }

    }

    public <T> void store(MapCodec<T> mapcodec, T t0) {
        this.store(mapcodec, DynamicOpsNBT.INSTANCE, t0);
    }

    public <T> void store(MapCodec<T> mapcodec, DynamicOps<NBTBase> dynamicops, T t0) {
        this.merge((NBTTagCompound) mapcodec.encoder().encodeStart(dynamicops, t0).getOrThrow());
    }

    public <T> Optional<T> read(String s, Codec<T> codec) {
        return this.<T>read(s, codec, DynamicOpsNBT.INSTANCE);
    }

    public <T> Optional<T> read(String s, Codec<T> codec, DynamicOps<NBTBase> dynamicops) {
        NBTBase nbtbase = this.get(s);

        return nbtbase == null ? Optional.empty() : codec.parse(dynamicops, nbtbase).resultOrPartial((s1) -> {
            NBTTagCompound.LOGGER.error("Failed to read field ({}={}): {}", new Object[]{s, nbtbase, s1});
        });
    }

    public <T> Optional<T> read(MapCodec<T> mapcodec) {
        return this.read(mapcodec, DynamicOpsNBT.INSTANCE);
    }

    public <T> Optional<T> read(MapCodec<T> mapcodec, DynamicOps<NBTBase> dynamicops) {
        return mapcodec.decode(dynamicops, (MapLike) dynamicops.getMap(this).getOrThrow()).resultOrPartial((s) -> {
            NBTTagCompound.LOGGER.error("Failed to read value ({}): {}", this, s);
        });
    }
}
