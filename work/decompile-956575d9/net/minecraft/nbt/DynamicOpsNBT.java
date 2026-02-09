package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;

public class DynamicOpsNBT implements DynamicOps<NBTBase> {

    public static final DynamicOpsNBT INSTANCE = new DynamicOpsNBT();

    private DynamicOpsNBT() {}

    public NBTBase empty() {
        return NBTTagEnd.INSTANCE;
    }

    public <U> U convertTo(DynamicOps<U> dynamicops, NBTBase nbtbase) {
        Objects.requireNonNull(nbtbase);
        byte b0 = 0;
        Object object;

        //$FF: b0->value
        //0->net/minecraft/nbt/NBTTagEnd
        //1->net/minecraft/nbt/NBTTagByte
        //2->net/minecraft/nbt/NBTTagShort
        //3->net/minecraft/nbt/NBTTagInt
        //4->net/minecraft/nbt/NBTTagLong
        //5->net/minecraft/nbt/NBTTagFloat
        //6->net/minecraft/nbt/NBTTagDouble
        //7->net/minecraft/nbt/NBTTagByteArray
        //8->net/minecraft/nbt/NBTTagString
        //9->net/minecraft/nbt/NBTTagList
        //10->net/minecraft/nbt/NBTTagCompound
        //11->net/minecraft/nbt/NBTTagIntArray
        //12->net/minecraft/nbt/NBTTagLongArray
        switch (nbtbase.typeSwitch<invokedynamic>(nbtbase, b0)) {
            case 0:
                NBTTagEnd nbttagend = (NBTTagEnd)nbtbase;

                object = (NBTTagString)(dynamicops.empty());
                break;
            case 1:
                NBTTagByte nbttagbyte = (NBTTagByte)nbtbase;
                NBTTagByte nbttagbyte1 = nbttagbyte;

                try {
                    b1 = nbttagbyte1.value();
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }

                byte b2 = b1;

                object = (NBTTagString)(dynamicops.createByte(b2));
                break;
            case 2:
                NBTTagShort nbttagshort = (NBTTagShort)nbtbase;
                NBTTagShort nbttagshort1 = nbttagshort;

                try {
                    short0 = nbttagshort1.value();
                } catch (Throwable throwable1) {
                    throw new MatchException(throwable1.toString(), throwable1);
                }

                short short1 = short0;

                object = (NBTTagString)(dynamicops.createShort(short1));
                break;
            case 3:
                NBTTagInt nbttagint = (NBTTagInt)nbtbase;
                NBTTagInt nbttagint1 = nbttagint;

                try {
                    i = nbttagint1.value();
                } catch (Throwable throwable2) {
                    throw new MatchException(throwable2.toString(), throwable2);
                }

                int j = i;

                object = (NBTTagString)(dynamicops.createInt(j));
                break;
            case 4:
                NBTTagLong nbttaglong = (NBTTagLong)nbtbase;
                NBTTagLong nbttaglong1 = nbttaglong;

                try {
                    k = nbttaglong1.value();
                } catch (Throwable throwable3) {
                    throw new MatchException(throwable3.toString(), throwable3);
                }

                long l = k;

                object = (NBTTagString)(dynamicops.createLong(l));
                break;
            case 5:
                NBTTagFloat nbttagfloat = (NBTTagFloat)nbtbase;
                NBTTagFloat nbttagfloat1 = nbttagfloat;

                try {
                    f = nbttagfloat1.value();
                } catch (Throwable throwable4) {
                    throw new MatchException(throwable4.toString(), throwable4);
                }

                float f1 = f;

                object = (NBTTagString)(dynamicops.createFloat(f1));
                break;
            case 6:
                NBTTagDouble nbttagdouble = (NBTTagDouble)nbtbase;
                NBTTagDouble nbttagdouble1 = nbttagdouble;

                try {
                    d0 = nbttagdouble1.value();
                } catch (Throwable throwable5) {
                    throw new MatchException(throwable5.toString(), throwable5);
                }

                double d1 = d0;

                object = (NBTTagString)(dynamicops.createDouble(d1));
                break;
            case 7:
                NBTTagByteArray nbttagbytearray = (NBTTagByteArray)nbtbase;

                object = (NBTTagString)(dynamicops.createByteList(ByteBuffer.wrap(nbttagbytearray.getAsByteArray())));
                break;
            case 8:
                NBTTagString nbttagstring = (NBTTagString)nbtbase;

                object = nbttagstring;

                try {
                    s = object.value();
                } catch (Throwable throwable6) {
                    throw new MatchException(throwable6.toString(), throwable6);
                }

                String s1 = s;

                object = (NBTTagString)(dynamicops.createString(s1));
                break;
            case 9:
                NBTTagList nbttaglist = (NBTTagList)nbtbase;

                object = (NBTTagString)(this.convertList(dynamicops, nbttaglist));
                break;
            case 10:
                NBTTagCompound nbttagcompound = (NBTTagCompound)nbtbase;

                object = (NBTTagString)(this.convertMap(dynamicops, nbttagcompound));
                break;
            case 11:
                NBTTagIntArray nbttagintarray = (NBTTagIntArray)nbtbase;

                object = (NBTTagString)(dynamicops.createIntList(Arrays.stream(nbttagintarray.getAsIntArray())));
                break;
            case 12:
                NBTTagLongArray nbttaglongarray = (NBTTagLongArray)nbtbase;

                object = (NBTTagString)(dynamicops.createLongList(Arrays.stream(nbttaglongarray.getAsLongArray())));
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        return (U)object;
    }

    public DataResult<Number> getNumberValue(NBTBase nbtbase) {
        return (DataResult) nbtbase.asNumber().map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
                return "Not a number";
            });
        });
    }

    public NBTBase createNumeric(Number number) {
        return NBTTagDouble.valueOf(number.doubleValue());
    }

    public NBTBase createByte(byte b0) {
        return NBTTagByte.valueOf(b0);
    }

    public NBTBase createShort(short short0) {
        return NBTTagShort.valueOf(short0);
    }

    public NBTBase createInt(int i) {
        return NBTTagInt.valueOf(i);
    }

    public NBTBase createLong(long i) {
        return NBTTagLong.valueOf(i);
    }

    public NBTBase createFloat(float f) {
        return NBTTagFloat.valueOf(f);
    }

    public NBTBase createDouble(double d0) {
        return NBTTagDouble.valueOf(d0);
    }

    public NBTBase createBoolean(boolean flag) {
        return NBTTagByte.valueOf(flag);
    }

    public DataResult<String> getStringValue(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagString nbttagstring) {
            NBTTagString nbttagstring1 = nbttagstring;

            try {
                s = nbttagstring1.value();
            } catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }

            String s1 = s;

            return DataResult.success(s1);
        } else {
            return DataResult.error(() -> {
                return "Not a string";
            });
        }
    }

    public NBTBase createString(String s) {
        return NBTTagString.valueOf(s);
    }

    public DataResult<NBTBase> mergeToList(NBTBase nbtbase, NBTBase nbtbase1) {
        return (DataResult) createCollector(nbtbase).map((dynamicopsnbt_d) -> {
            return DataResult.success(dynamicopsnbt_d.accept(nbtbase1).result());
        }).orElseGet(() -> {
            return DataResult.error(() -> {
                return "mergeToList called with not a list: " + String.valueOf(nbtbase);
            }, nbtbase);
        });
    }

    public DataResult<NBTBase> mergeToList(NBTBase nbtbase, List<NBTBase> list) {
        return (DataResult) createCollector(nbtbase).map((dynamicopsnbt_d) -> {
            return DataResult.success(dynamicopsnbt_d.acceptAll(list).result());
        }).orElseGet(() -> {
            return DataResult.error(() -> {
                return "mergeToList called with not a list: " + String.valueOf(nbtbase);
            }, nbtbase);
        });
    }

    public DataResult<NBTBase> mergeToMap(NBTBase nbtbase, NBTBase nbtbase1, NBTBase nbtbase2) {
        if (!(nbtbase instanceof NBTTagCompound) && !(nbtbase instanceof NBTTagEnd)) {
            return DataResult.error(() -> {
                return "mergeToMap called with not a map: " + String.valueOf(nbtbase);
            }, nbtbase);
        } else if (nbtbase1 instanceof NBTTagString) {
            NBTTagString nbttagstring = (NBTTagString) nbtbase1;
            NBTTagString nbttagstring1 = nbttagstring;

            try {
                s = nbttagstring1.value();
            } catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }

            String s1 = s;
            String s2 = s1;
            NBTTagCompound nbttagcompound;

            if (nbtbase instanceof NBTTagCompound) {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

                nbttagcompound = nbttagcompound1.shallowCopy();
            } else {
                nbttagcompound = new NBTTagCompound();
            }

            NBTTagCompound nbttagcompound2 = nbttagcompound;

            nbttagcompound2.put(s2, nbtbase2);
            return DataResult.success(nbttagcompound2);
        } else {
            return DataResult.error(() -> {
                return "key is not a string: " + String.valueOf(nbtbase1);
            }, nbtbase);
        }
    }

    public DataResult<NBTBase> mergeToMap(NBTBase nbtbase, MapLike<NBTBase> maplike) {
        if (!(nbtbase instanceof NBTTagCompound) && !(nbtbase instanceof NBTTagEnd)) {
            return DataResult.error(() -> {
                return "mergeToMap called with not a map: " + String.valueOf(nbtbase);
            }, nbtbase);
        } else {
            NBTTagCompound nbttagcompound;

            if (nbtbase instanceof NBTTagCompound) {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

                nbttagcompound = nbttagcompound1.shallowCopy();
            } else {
                nbttagcompound = new NBTTagCompound();
            }

            NBTTagCompound nbttagcompound2 = nbttagcompound;
            List<NBTBase> list = new ArrayList();

            maplike.entries().forEach((pair) -> {
                NBTBase nbtbase1 = (NBTBase) pair.getFirst();

                if (nbtbase1 instanceof NBTTagString nbttagstring) {
                    NBTTagString nbttagstring1 = nbttagstring;

                    try {
                        s = nbttagstring1.value();
                    } catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }

                    String s1 = s;

                    nbttagcompound2.put(s1, (NBTBase) pair.getSecond());
                } else {
                    list.add(nbtbase1);
                }
            });
            return !list.isEmpty() ? DataResult.error(() -> {
                return "some keys are not strings: " + String.valueOf(list);
            }, nbttagcompound2) : DataResult.success(nbttagcompound2);
        }
    }

    public DataResult<NBTBase> mergeToMap(NBTBase nbtbase, Map<NBTBase, NBTBase> map) {
        if (!(nbtbase instanceof NBTTagCompound) && !(nbtbase instanceof NBTTagEnd)) {
            return DataResult.error(() -> {
                return "mergeToMap called with not a map: " + String.valueOf(nbtbase);
            }, nbtbase);
        } else {
            NBTTagCompound nbttagcompound;

            if (nbtbase instanceof NBTTagCompound) {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

                nbttagcompound = nbttagcompound1.shallowCopy();
            } else {
                nbttagcompound = new NBTTagCompound();
            }

            NBTTagCompound nbttagcompound2 = nbttagcompound;
            List<NBTBase> list = new ArrayList();

            for (Map.Entry<NBTBase, NBTBase> map_entry : map.entrySet()) {
                NBTBase nbtbase1 = (NBTBase) map_entry.getKey();

                if (nbtbase1 instanceof NBTTagString) {
                    NBTTagString nbttagstring = (NBTTagString) nbtbase1;
                    NBTTagString nbttagstring1 = nbttagstring;

                    try {
                        s = nbttagstring1.value();
                    } catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }

                    String s1 = s;

                    nbttagcompound2.put(s1, (NBTBase) map_entry.getValue());
                } else {
                    list.add(nbtbase1);
                }
            }

            if (!list.isEmpty()) {
                return DataResult.error(() -> {
                    return "some keys are not strings: " + String.valueOf(list);
                }, nbttagcompound2);
            } else {
                return DataResult.success(nbttagcompound2);
            }
        }
    }

    public DataResult<Stream<Pair<NBTBase, NBTBase>>> getMapValues(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            return DataResult.success(nbttagcompound.entrySet().stream().map((entry) -> {
                return Pair.of(this.createString((String) entry.getKey()), (NBTBase) entry.getValue());
            }));
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + String.valueOf(nbtbase);
            });
        }
    }

    public DataResult<Consumer<BiConsumer<NBTBase, NBTBase>>> getMapEntries(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            return DataResult.success((Consumer) (biconsumer) -> {
                for (Map.Entry<String, NBTBase> map_entry : nbttagcompound.entrySet()) {
                    biconsumer.accept(this.createString((String) map_entry.getKey()), (NBTBase) map_entry.getValue());
                }

            });
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + String.valueOf(nbtbase);
            });
        }
    }

    public DataResult<MapLike<NBTBase>> getMap(NBTBase nbtbase) {
        if (nbtbase instanceof final NBTTagCompound nbttagcompound) {
            return DataResult.success(new MapLike<NBTBase>() {
                @Nullable
                public NBTBase get(NBTBase nbtbase1) {
                    if (nbtbase1 instanceof NBTTagString nbttagstring) {
                        NBTTagString nbttagstring1 = nbttagstring;

                        try {
                            s = nbttagstring1.value();
                        } catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }

                        String s1 = s;

                        return nbttagcompound.get(s1);
                    } else {
                        throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + String.valueOf(nbtbase1));
                    }
                }

                @Nullable
                public NBTBase get(String s) {
                    return nbttagcompound.get(s);
                }

                public Stream<Pair<NBTBase, NBTBase>> entries() {
                    return nbttagcompound.entrySet().stream().map((entry) -> {
                        return Pair.of(DynamicOpsNBT.this.createString((String) entry.getKey()), (NBTBase) entry.getValue());
                    });
                }

                public String toString() {
                    return "MapLike[" + String.valueOf(nbttagcompound) + "]";
                }
            });
        } else {
            return DataResult.error(() -> {
                return "Not a map: " + String.valueOf(nbtbase);
            });
        }
    }

    public NBTBase createMap(Stream<Pair<NBTBase, NBTBase>> stream) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        stream.forEach((pair) -> {
            NBTBase nbtbase = (NBTBase) pair.getFirst();
            NBTBase nbtbase1 = (NBTBase) pair.getSecond();

            if (nbtbase instanceof NBTTagString nbttagstring) {
                NBTTagString nbttagstring1 = nbttagstring;

                try {
                    s = nbttagstring1.value();
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }

                String s1 = s;

                nbttagcompound.put(s1, nbtbase1);
            } else {
                throw new UnsupportedOperationException("Cannot create map with non-string key: " + String.valueOf(nbtbase));
            }
        });
        return nbttagcompound;
    }

    public DataResult<Stream<NBTBase>> getStream(NBTBase nbtbase) {
        if (nbtbase instanceof NBTList nbtlist) {
            return DataResult.success(nbtlist.stream());
        } else {
            return DataResult.error(() -> {
                return "Not a list";
            });
        }
    }

    public DataResult<Consumer<Consumer<NBTBase>>> getList(NBTBase nbtbase) {
        if (nbtbase instanceof NBTList nbtlist) {
            Objects.requireNonNull(nbtlist);
            return DataResult.success(nbtlist::forEach);
        } else {
            return DataResult.error(() -> {
                return "Not a list: " + String.valueOf(nbtbase);
            });
        }
    }

    public DataResult<ByteBuffer> getByteBuffer(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagByteArray nbttagbytearray) {
            return DataResult.success(ByteBuffer.wrap(nbttagbytearray.getAsByteArray()));
        } else {
            return super.getByteBuffer(nbtbase);
        }
    }

    public NBTBase createByteList(ByteBuffer bytebuffer) {
        ByteBuffer bytebuffer1 = bytebuffer.duplicate().clear();
        byte[] abyte = new byte[bytebuffer.capacity()];

        bytebuffer1.get(0, abyte, 0, abyte.length);
        return new NBTTagByteArray(abyte);
    }

    public DataResult<IntStream> getIntStream(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagIntArray nbttagintarray) {
            return DataResult.success(Arrays.stream(nbttagintarray.getAsIntArray()));
        } else {
            return super.getIntStream(nbtbase);
        }
    }

    public NBTBase createIntList(IntStream intstream) {
        return new NBTTagIntArray(intstream.toArray());
    }

    public DataResult<LongStream> getLongStream(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagLongArray nbttaglongarray) {
            return DataResult.success(Arrays.stream(nbttaglongarray.getAsLongArray()));
        } else {
            return super.getLongStream(nbtbase);
        }
    }

    public NBTBase createLongList(LongStream longstream) {
        return new NBTTagLongArray(longstream.toArray());
    }

    public NBTBase createList(Stream<NBTBase> stream) {
        return new NBTTagList((List) stream.collect(SystemUtils.toMutableList()));
    }

    public NBTBase remove(NBTBase nbtbase, String s) {
        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.shallowCopy();

            nbttagcompound1.remove(s);
            return nbttagcompound1;
        } else {
            return nbtbase;
        }
    }

    public String toString() {
        return "NBT";
    }

    public RecordBuilder<NBTBase> mapBuilder() {
        return new DynamicOpsNBT.f();
    }

    private static Optional<DynamicOpsNBT.d> createCollector(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagEnd) {
            return Optional.of(new DynamicOpsNBT.b());
        } else if (nbtbase instanceof NBTList) {
            NBTList nbtlist = (NBTList)nbtbase;

            if (nbtlist.isEmpty()) {
                return Optional.of(new DynamicOpsNBT.b());
            } else {
                Objects.requireNonNull(nbtlist);
                byte b0 = 0;
                Optional optional;

                //$FF: b0->value
                //0->net/minecraft/nbt/NBTTagList
                //1->net/minecraft/nbt/NBTTagByteArray
                //2->net/minecraft/nbt/NBTTagIntArray
                //3->net/minecraft/nbt/NBTTagLongArray
                switch (nbtlist.typeSwitch<invokedynamic>(nbtlist, b0)) {
                    case 0:
                        NBTTagList nbttaglist = (NBTTagList)nbtlist;

                        optional = Optional.of(new DynamicOpsNBT.b(nbttaglist));
                        break;
                    case 1:
                        NBTTagByteArray nbttagbytearray = (NBTTagByteArray)nbtlist;

                        optional = Optional.of(new DynamicOpsNBT.a(nbttagbytearray.getAsByteArray()));
                        break;
                    case 2:
                        NBTTagIntArray nbttagintarray = (NBTTagIntArray)nbtlist;

                        optional = Optional.of(new DynamicOpsNBT.c(nbttagintarray.getAsIntArray()));
                        break;
                    case 3:
                        NBTTagLongArray nbttaglongarray = (NBTTagLongArray)nbtlist;

                        optional = Optional.of(new DynamicOpsNBT.e(nbttaglongarray.getAsLongArray()));
                        break;
                    default:
                        throw new MatchException((String)null, (Throwable)null);
                }

                return optional;
            }
        } else {
            return Optional.empty();
        }
    }

    private class f extends RecordBuilder.AbstractStringBuilder<NBTBase, NBTTagCompound> {

        protected f() {
            super(DynamicOpsNBT.this);
        }

        protected NBTTagCompound initBuilder() {
            return new NBTTagCompound();
        }

        protected NBTTagCompound append(String s, NBTBase nbtbase, NBTTagCompound nbttagcompound) {
            nbttagcompound.put(s, nbtbase);
            return nbttagcompound;
        }

        protected DataResult<NBTBase> build(NBTTagCompound nbttagcompound, NBTBase nbtbase) {
            if (nbtbase != null && nbtbase != NBTTagEnd.INSTANCE) {
                if (!(nbtbase instanceof NBTTagCompound)) {
                    return DataResult.error(() -> {
                        return "mergeToMap called with not a map: " + String.valueOf(nbtbase);
                    }, nbtbase);
                } else {
                    NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;
                    NBTTagCompound nbttagcompound2 = nbttagcompound1.shallowCopy();

                    for (Map.Entry<String, NBTBase> map_entry : nbttagcompound.entrySet()) {
                        nbttagcompound2.put((String) map_entry.getKey(), (NBTBase) map_entry.getValue());
                    }

                    return DataResult.success(nbttagcompound2);
                }
            } else {
                return DataResult.success(nbttagcompound);
            }
        }
    }

    private interface d {

        DynamicOpsNBT.d accept(NBTBase nbtbase);

        default DynamicOpsNBT.d acceptAll(Iterable<NBTBase> iterable) {
            DynamicOpsNBT.d dynamicopsnbt_d = this;

            for (NBTBase nbtbase : iterable) {
                dynamicopsnbt_d = dynamicopsnbt_d.accept(nbtbase);
            }

            return dynamicopsnbt_d;
        }

        default DynamicOpsNBT.d acceptAll(Stream<NBTBase> stream) {
            Objects.requireNonNull(stream);
            return this.acceptAll(stream::iterator);
        }

        NBTBase result();
    }

    private static class b implements DynamicOpsNBT.d {

        private final NBTTagList result = new NBTTagList();

        b() {}

        b(NBTTagList nbttaglist) {
            this.result.addAll(nbttaglist);
        }

        public b(IntArrayList intarraylist) {
            intarraylist.forEach((i) -> {
                this.result.add(NBTTagInt.valueOf(i));
            });
        }

        public b(ByteArrayList bytearraylist) {
            bytearraylist.forEach((b0) -> {
                this.result.add(NBTTagByte.valueOf(b0));
            });
        }

        public b(LongArrayList longarraylist) {
            longarraylist.forEach((i) -> {
                this.result.add(NBTTagLong.valueOf(i));
            });
        }

        @Override
        public DynamicOpsNBT.d accept(NBTBase nbtbase) {
            this.result.add(nbtbase);
            return this;
        }

        @Override
        public NBTBase result() {
            return this.result;
        }
    }

    private static class c implements DynamicOpsNBT.d {

        private final IntArrayList values = new IntArrayList();

        public c(int[] aint) {
            this.values.addElements(0, aint);
        }

        @Override
        public DynamicOpsNBT.d accept(NBTBase nbtbase) {
            if (nbtbase instanceof NBTTagInt nbttagint) {
                this.values.add(nbttagint.intValue());
                return this;
            } else {
                return (new DynamicOpsNBT.b(this.values)).accept(nbtbase);
            }
        }

        @Override
        public NBTBase result() {
            return new NBTTagIntArray(this.values.toIntArray());
        }
    }

    private static class a implements DynamicOpsNBT.d {

        private final ByteArrayList values = new ByteArrayList();

        public a(byte[] abyte) {
            this.values.addElements(0, abyte);
        }

        @Override
        public DynamicOpsNBT.d accept(NBTBase nbtbase) {
            if (nbtbase instanceof NBTTagByte nbttagbyte) {
                this.values.add(nbttagbyte.byteValue());
                return this;
            } else {
                return (new DynamicOpsNBT.b(this.values)).accept(nbtbase);
            }
        }

        @Override
        public NBTBase result() {
            return new NBTTagByteArray(this.values.toByteArray());
        }
    }

    private static class e implements DynamicOpsNBT.d {

        private final LongArrayList values = new LongArrayList();

        public e(long[] along) {
            this.values.addElements(0, along);
        }

        @Override
        public DynamicOpsNBT.d accept(NBTBase nbtbase) {
            if (nbtbase instanceof NBTTagLong nbttaglong) {
                this.values.add(nbttaglong.longValue());
                return this;
            } else {
                return (new DynamicOpsNBT.b(this.values)).accept(nbtbase);
            }
        }

        @Override
        public NBTBase result() {
            return new NBTTagLongArray(this.values.toLongArray());
        }
    }
}
