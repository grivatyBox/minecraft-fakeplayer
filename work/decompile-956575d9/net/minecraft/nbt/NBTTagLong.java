package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record NBTTagLong(long value) implements NBTNumber {

    private static final int SELF_SIZE_IN_BYTES = 16;
    public static final NBTTagType<NBTTagLong> TYPE = new NBTTagType.a<NBTTagLong>() {
        @Override
        public NBTTagLong load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagLong.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static long readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(16L);
            return datainput.readLong();
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public String getName() {
            return "LONG";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long";
        }
    };

    public static NBTTagLong valueOf(long i) {
        return i >= -128L && i <= 1024L ? NBTTagLong.a.cache[(int) i - Byte.MIN_VALUE] : new NBTTagLong(i);
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeLong(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 16;
    }

    @Override
    public byte getId() {
        return 4;
    }

    @Override
    public NBTTagType<NBTTagLong> getType() {
        return NBTTagLong.TYPE;
    }

    @Override
    public NBTTagLong copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitLong(this);
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return (int) (this.value & -1L);
    }

    @Override
    public short shortValue() {
        return (short) ((int) (this.value & 65535L));
    }

    @Override
    public byte byteValue() {
        return (byte) ((int) (this.value & 255L));
    }

    @Override
    public double doubleValue() {
        return (double) this.value;
    }

    @Override
    public float floatValue() {
        return (float) this.value;
    }

    @Override
    public Number box() {
        return this.value;
    }

    @Override
    public StreamTagVisitor.b accept(StreamTagVisitor streamtagvisitor) {
        return streamtagvisitor.visit(this.value);
    }

    @Override
    public String toString() {
        StringTagVisitor stringtagvisitor = new StringTagVisitor();

        stringtagvisitor.visitLong(this);
        return stringtagvisitor.build();
    }

    private static class a {

        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final NBTTagLong[] cache = new NBTTagLong[1153];

        private a() {}

        static {
            for (int i = 0; i < NBTTagLong.a.cache.length; ++i) {
                NBTTagLong.a.cache[i] = new NBTTagLong((long) (Byte.MIN_VALUE + i));
            }

        }
    }
}
