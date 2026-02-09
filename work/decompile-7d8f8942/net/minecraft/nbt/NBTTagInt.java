package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record NBTTagInt(int value) implements NBTNumber {

    private static final int SELF_SIZE_IN_BYTES = 12;
    public static final NBTTagType<NBTTagInt> TYPE = new NBTTagType.a<NBTTagInt>() {
        @Override
        public NBTTagInt load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagInt.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static int readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(12L);
            return datainput.readInt();
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public String getName() {
            return "INT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int";
        }
    };

    public static NBTTagInt valueOf(int i) {
        return i >= -128 && i <= 1024 ? NBTTagInt.a.cache[i - Byte.MIN_VALUE] : new NBTTagInt(i);
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeInt(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 12;
    }

    @Override
    public byte getId() {
        return 3;
    }

    @Override
    public NBTTagType<NBTTagInt> getType() {
        return NBTTagInt.TYPE;
    }

    @Override
    public NBTTagInt copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitInt(this);
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
        return (short) (this.value & '\uffff');
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

        stringtagvisitor.visitInt(this);
        return stringtagvisitor.build();
    }

    private static class a {

        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final NBTTagInt[] cache = new NBTTagInt[1153];

        private a() {}

        static {
            for (int i = 0; i < NBTTagInt.a.cache.length; ++i) {
                NBTTagInt.a.cache[i] = new NBTTagInt(Byte.MIN_VALUE + i);
            }

        }
    }
}
