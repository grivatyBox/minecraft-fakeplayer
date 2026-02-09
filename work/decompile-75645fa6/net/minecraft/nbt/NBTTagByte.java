package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public record NBTTagByte(byte value) implements NBTNumber {

    private static final int SELF_SIZE_IN_BYTES = 9;
    public static final NBTTagType<NBTTagByte> TYPE = new NBTTagType.a<NBTTagByte>() {
        @Override
        public NBTTagByte load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagByte.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static byte readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(9L);
            return datainput.readByte();
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public String getName() {
            return "BYTE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte";
        }
    };
    public static final NBTTagByte ZERO = valueOf((byte) 0);
    public static final NBTTagByte ONE = valueOf((byte) 1);

    public static NBTTagByte valueOf(byte b0) {
        return NBTTagByte.a.cache[128 + b0];
    }

    public static NBTTagByte valueOf(boolean flag) {
        return flag ? NBTTagByte.ONE : NBTTagByte.ZERO;
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 9;
    }

    @Override
    public byte getId() {
        return 1;
    }

    @Override
    public NBTTagType<NBTTagByte> getType() {
        return NBTTagByte.TYPE;
    }

    @Override
    public NBTTagByte copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitByte(this);
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
        return (short) this.value;
    }

    @Override
    public byte byteValue() {
        return this.value;
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

        stringtagvisitor.visitByte(this);
        return stringtagvisitor.build();
    }

    private static class a {

        static final NBTTagByte[] cache = new NBTTagByte[256];

        private a() {}

        static {
            for (int i = 0; i < NBTTagByte.a.cache.length; ++i) {
                NBTTagByte.a.cache[i] = new NBTTagByte((byte) (i - 128));
            }

        }
    }
}
