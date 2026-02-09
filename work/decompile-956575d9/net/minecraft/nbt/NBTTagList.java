package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public final class NBTTagList extends AbstractList<NBTBase> implements NBTList {

    private static final String WRAPPER_MARKER = "";
    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final NBTTagType<NBTTagList> TYPE = new NBTTagType.b<NBTTagList>() {
        @Override
        public NBTTagList load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.pushDepth();

            NBTTagList nbttaglist;

            try {
                nbttaglist = loadList(datainput, nbtreadlimiter);
            } finally {
                nbtreadlimiter.popDepth();
            }

            return nbttaglist;
        }

        private static NBTTagList loadList(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(36L);
            byte b0 = datainput.readByte();
            int i = datainput.readInt();

            if (b0 == 0 && i > 0) {
                throw new NbtFormatException("Missing type on ListTag");
            } else {
                nbtreadlimiter.accountBytes(4L, (long) i);
                NBTTagType<?> nbttagtype = NBTTagTypes.getType(b0);
                NBTTagList nbttaglist = new NBTTagList(new ArrayList(i));

                for (int j = 0; j < i; ++j) {
                    nbttaglist.addAndUnwrap(nbttagtype.load(datainput, nbtreadlimiter));
                }

                return nbttaglist;
            }
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.pushDepth();

            StreamTagVisitor.b streamtagvisitor_b;

            try {
                streamtagvisitor_b = parseList(datainput, streamtagvisitor, nbtreadlimiter);
            } finally {
                nbtreadlimiter.popDepth();
            }

            return streamtagvisitor_b;
        }

        private static StreamTagVisitor.b parseList(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(36L);
            NBTTagType<?> nbttagtype = NBTTagTypes.getType(datainput.readByte());
            int i = datainput.readInt();

            switch (streamtagvisitor.visitList(nbttagtype, i)) {
                case HALT:
                    return StreamTagVisitor.b.HALT;
                case BREAK:
                    nbttagtype.skip(datainput, i, nbtreadlimiter);
                    return streamtagvisitor.visitContainerEnd();
                default:
                    nbtreadlimiter.accountBytes(4L, (long) i);
                    int j = 0;

                    while (true) {
                        label45:
                        {
                            if (j < i) {
                                switch (streamtagvisitor.visitElement(nbttagtype, j)) {
                                    case HALT:
                                        return StreamTagVisitor.b.HALT;
                                    case BREAK:
                                        nbttagtype.skip(datainput, nbtreadlimiter);
                                        break;
                                    case SKIP:
                                        nbttagtype.skip(datainput, nbtreadlimiter);
                                        break label45;
                                    default:
                                        switch (nbttagtype.parse(datainput, streamtagvisitor, nbtreadlimiter)) {
                                            case HALT:
                                                return StreamTagVisitor.b.HALT;
                                            case BREAK:
                                                break;
                                            default:
                                                break label45;
                                        }
                                }
                            }

                            int k = i - 1 - j;

                            if (k > 0) {
                                nbttagtype.skip(datainput, k, nbtreadlimiter);
                            }

                            return streamtagvisitor.visitContainerEnd();
                        }

                        ++j;
                    }
            }
        }

        @Override
        public void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.pushDepth();

            try {
                NBTTagType<?> nbttagtype = NBTTagTypes.getType(datainput.readByte());
                int i = datainput.readInt();

                nbttagtype.skip(datainput, i, nbtreadlimiter);
            } finally {
                nbtreadlimiter.popDepth();
            }

        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }
    };
    private final List<NBTBase> list;

    public NBTTagList() {
        this(new ArrayList());
    }

    public NBTTagList(List<NBTBase> list) {
        this.list = list;
    }

    private static NBTBase tryUnwrap(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.size() == 1) {
            NBTBase nbtbase = nbttagcompound.get("");

            if (nbtbase != null) {
                return nbtbase;
            }
        }

        return nbttagcompound;
    }

    private static boolean isWrapper(NBTTagCompound nbttagcompound) {
        return nbttagcompound.size() == 1 && nbttagcompound.contains("");
    }

    private static NBTBase wrapIfNeeded(byte b0, NBTBase nbtbase) {
        if (b0 != 10) {
            return nbtbase;
        } else {
            if (nbtbase instanceof NBTTagCompound) {
                NBTTagCompound nbttagcompound = (NBTTagCompound) nbtbase;

                if (!isWrapper(nbttagcompound)) {
                    return nbttagcompound;
                }
            }

            return wrapElement(nbtbase);
        }
    }

    private static NBTTagCompound wrapElement(NBTBase nbtbase) {
        return new NBTTagCompound(Map.of("", nbtbase));
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        byte b0 = this.identifyRawElementType();

        dataoutput.writeByte(b0);
        dataoutput.writeInt(this.list.size());

        for (NBTBase nbtbase : this.list) {
            wrapIfNeeded(b0, nbtbase).write(dataoutput);
        }

    }

    @VisibleForTesting
    public byte identifyRawElementType() {
        byte b0 = 0;

        for (NBTBase nbtbase : this.list) {
            byte b1 = nbtbase.getId();

            if (b0 == 0) {
                b0 = b1;
            } else if (b0 != b1) {
                return 10;
            }
        }

        return b0;
    }

    public void addAndUnwrap(NBTBase nbtbase) {
        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            this.add(tryUnwrap(nbttagcompound));
        } else {
            this.add(nbtbase);
        }

    }

    @Override
    public int sizeInBytes() {
        int i = 36;

        i += 4 * this.list.size();

        for (NBTBase nbtbase : this.list) {
            i += nbtbase.sizeInBytes();
        }

        return i;
    }

    @Override
    public byte getId() {
        return 9;
    }

    @Override
    public NBTTagType<NBTTagList> getType() {
        return NBTTagList.TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringtagvisitor = new StringTagVisitor();

        stringtagvisitor.visitList(this);
        return stringtagvisitor.build();
    }

    @Override
    public NBTBase remove(int i) {
        return (NBTBase) this.list.remove(i);
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public Optional<NBTTagCompound> getCompound(int i) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTTagCompound nbttagcompound) {
            return Optional.of(nbttagcompound);
        } else {
            return Optional.empty();
        }
    }

    public NBTTagCompound getCompoundOrEmpty(int i) {
        return (NBTTagCompound) this.getCompound(i).orElseGet(NBTTagCompound::new);
    }

    public Optional<NBTTagList> getList(int i) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTTagList nbttaglist) {
            return Optional.of(nbttaglist);
        } else {
            return Optional.empty();
        }
    }

    public NBTTagList getListOrEmpty(int i) {
        return (NBTTagList) this.getList(i).orElseGet(NBTTagList::new);
    }

    public Optional<Short> getShort(int i) {
        return this.getOptional(i).flatMap(NBTBase::asShort);
    }

    public short getShortOr(int i, short short0) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTNumber nbtnumber) {
            return nbtnumber.shortValue();
        } else {
            return short0;
        }
    }

    public Optional<Integer> getInt(int i) {
        return this.getOptional(i).flatMap(NBTBase::asInt);
    }

    public int getIntOr(int i, int j) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTNumber nbtnumber) {
            return nbtnumber.intValue();
        } else {
            return j;
        }
    }

    public Optional<int[]> getIntArray(int i) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTTagIntArray nbttagintarray) {
            return Optional.of(nbttagintarray.getAsIntArray());
        } else {
            return Optional.empty();
        }
    }

    public Optional<long[]> getLongArray(int i) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTTagLongArray nbttaglongarray) {
            return Optional.of(nbttaglongarray.getAsLongArray());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Double> getDouble(int i) {
        return this.getOptional(i).flatMap(NBTBase::asDouble);
    }

    public double getDoubleOr(int i, double d0) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTNumber nbtnumber) {
            return nbtnumber.doubleValue();
        } else {
            return d0;
        }
    }

    public Optional<Float> getFloat(int i) {
        return this.getOptional(i).flatMap(NBTBase::asFloat);
    }

    public float getFloatOr(int i, float f) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTNumber nbtnumber) {
            return nbtnumber.floatValue();
        } else {
            return f;
        }
    }

    public Optional<String> getString(int i) {
        return this.getOptional(i).flatMap(NBTBase::asString);
    }

    public String getStringOr(int i, String s) {
        NBTBase nbtbase = this.getNullable(i);

        if (nbtbase instanceof NBTTagString nbttagstring) {
            NBTTagString nbttagstring1 = nbttagstring;

            try {
                s1 = nbttagstring1.value();
            } catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }

            String s2 = s1;

            return s2;
        } else {
            return s;
        }
    }

    @Nullable
    private NBTBase getNullable(int i) {
        return i >= 0 && i < this.list.size() ? (NBTBase) this.list.get(i) : null;
    }

    private Optional<NBTBase> getOptional(int i) {
        return Optional.ofNullable(this.getNullable(i));
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public NBTBase get(int i) {
        return (NBTBase) this.list.get(i);
    }

    public NBTBase set(int i, NBTBase nbtbase) {
        return (NBTBase) this.list.set(i, nbtbase);
    }

    public void add(int i, NBTBase nbtbase) {
        this.list.add(i, nbtbase);
    }

    @Override
    public boolean setTag(int i, NBTBase nbtbase) {
        this.list.set(i, nbtbase);
        return true;
    }

    @Override
    public boolean addTag(int i, NBTBase nbtbase) {
        this.list.add(i, nbtbase);
        return true;
    }

    @Override
    public NBTTagList copy() {
        List<NBTBase> list = new ArrayList(this.list.size());

        for (NBTBase nbtbase : this.list) {
            list.add(nbtbase.copy());
        }

        return new NBTTagList(list);
    }

    @Override
    public Optional<NBTTagList> asList() {
        return Optional.of(this);
    }

    public boolean equals(Object object) {
        return this == object ? true : object instanceof NBTTagList && Objects.equals(this.list, ((NBTTagList) object).list);
    }

    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public Stream<NBTBase> stream() {
        return super.stream();
    }

    public Stream<NBTTagCompound> compoundStream() {
        return this.stream().mapMulti((nbtbase, consumer) -> {
            if (nbtbase instanceof NBTTagCompound nbttagcompound) {
                consumer.accept(nbttagcompound);
            }

        });
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitList(this);
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public StreamTagVisitor.b accept(StreamTagVisitor streamtagvisitor) {
        byte b0 = this.identifyRawElementType();

        switch (streamtagvisitor.visitList(NBTTagTypes.getType(b0), this.list.size())) {
            case HALT:
                return StreamTagVisitor.b.HALT;
            case BREAK:
                return streamtagvisitor.visitContainerEnd();
            default:
                int i = 0;

                while (i < this.list.size()) {
                    NBTBase nbtbase = wrapIfNeeded(b0, (NBTBase) this.list.get(i));

                    switch (streamtagvisitor.visitElement(nbtbase.getType(), i)) {
                        case HALT:
                            return StreamTagVisitor.b.HALT;
                        case BREAK:
                            return streamtagvisitor.visitContainerEnd();
                        default:
                            switch (nbtbase.accept(streamtagvisitor)) {
                                case HALT:
                                    return StreamTagVisitor.b.HALT;
                                case BREAK:
                                    return streamtagvisitor.visitContainerEnd();
                            }
                        case SKIP:
                            ++i;
                    }
                }

                return streamtagvisitor.visitContainerEnd();
        }
    }
}
