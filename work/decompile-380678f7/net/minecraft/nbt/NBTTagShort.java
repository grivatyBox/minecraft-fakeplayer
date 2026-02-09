package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record NBTTagShort(short value) implements NBTNumber {

    private static final int SELF_SIZE_IN_BYTES = 10;
    public static final NBTTagType<NBTTagShort> TYPE = new NBTTagType.a<NBTTagShort>() {
        @Override
        public NBTTagShort load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagShort.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static short readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(10L);
            return datainput.readShort();
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public String getName() {
            return "SHORT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Short";
        }
    };

    public static NBTTagShort valueOf(short short0) {
        return short0 >= Byte.MIN_VALUE && short0 <= 1024 ? NBTTagShort.a.cache[short0 - Byte.MIN_VALUE] : new NBTTagShort(short0);
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeShort(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 10;
    }

    @Override
    public byte getId() {
        return 2;
    }

    @Override
    public NBTTagType<NBTTagShort> getType() {
        return NBTTagShort.TYPE;
    }

    @Override
    public NBTTagShort copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitShort(this);
    }

    @Override
    public long longValue() {
        return (long) this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return this.value;
    }

    @Override
    public byte byteValue() {
        return (byte) (this.value & 255);
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

        stringtagvisitor.visitShort(this);
        return stringtagvisitor.build();
    }

    private static class a {

        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final NBTTagShort[] cache = new NBTTagShort[1153];

        private a() {}

        static {
            for (int i = 0; i < NBTTagShort.a.cache.length; ++i) {
                NBTTagShort.a.cache[i] = new NBTTagShort((short) (Byte.MIN_VALUE + i));
            }

        }
    }
}
