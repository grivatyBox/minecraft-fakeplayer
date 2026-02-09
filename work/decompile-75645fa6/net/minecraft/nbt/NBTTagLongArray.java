package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;

public final class NBTTagLongArray implements NBTList {

    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final NBTTagType<NBTTagLongArray> TYPE = new NBTTagType.b<NBTTagLongArray>() {
        @Override
        public NBTTagLongArray load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return new NBTTagLongArray(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static long[] readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(24L);
            int i = datainput.readInt();

            nbtreadlimiter.accountBytes(8L, (long) i);
            long[] along = new long[i];

            for (int j = 0; j < i; ++j) {
                along[j] = datainput.readLong();
            }

            return along;
        }

        @Override
        public void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            datainput.skipBytes(datainput.readInt() * 8);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }
    };
    private long[] data;

    public NBTTagLongArray(long[] along) {
        this.data = along;
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeInt(this.data.length);

        for (long i : this.data) {
            dataoutput.writeLong(i);
        }

    }

    @Override
    public int sizeInBytes() {
        return 24 + 8 * this.data.length;
    }

    @Override
    public byte getId() {
        return 12;
    }

    @Override
    public NBTTagType<NBTTagLongArray> getType() {
        return NBTTagLongArray.TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringtagvisitor = new StringTagVisitor();

        stringtagvisitor.visitLongArray(this);
        return stringtagvisitor.build();
    }

    @Override
    public NBTTagLongArray copy() {
        long[] along = new long[this.data.length];

        System.arraycopy(this.data, 0, along, 0, this.data.length);
        return new NBTTagLongArray(along);
    }

    public boolean equals(Object object) {
        return this == object ? true : object instanceof NBTTagLongArray && Arrays.equals(this.data, ((NBTTagLongArray) object).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitLongArray(this);
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public NBTTagLong get(int i) {
        return NBTTagLong.valueOf(this.data[i]);
    }

    @Override
    public boolean setTag(int i, NBTBase nbtbase) {
        if (nbtbase instanceof NBTNumber nbtnumber) {
            this.data[i] = nbtnumber.longValue();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTag(int i, NBTBase nbtbase) {
        if (nbtbase instanceof NBTNumber nbtnumber) {
            this.data = ArrayUtils.add(this.data, i, nbtnumber.longValue());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public NBTTagLong remove(int i) {
        long j = this.data[i];

        this.data = ArrayUtils.remove(this.data, i);
        return NBTTagLong.valueOf(j);
    }

    @Override
    public void clear() {
        this.data = new long[0];
    }

    @Override
    public Optional<long[]> asLongArray() {
        return Optional.of(this.data);
    }

    @Override
    public StreamTagVisitor.b accept(StreamTagVisitor streamtagvisitor) {
        return streamtagvisitor.visit(this.data);
    }
}
