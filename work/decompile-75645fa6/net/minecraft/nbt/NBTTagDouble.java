package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.MathHelper;

public record NBTTagDouble(double value) implements NBTNumber {

    private static final int SELF_SIZE_IN_BYTES = 16;
    public static final NBTTagDouble ZERO = new NBTTagDouble(0.0D);
    public static final NBTTagType<NBTTagDouble> TYPE = new NBTTagType.a<NBTTagDouble>() {
        @Override
        public NBTTagDouble load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagDouble.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static double readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(16L);
            return datainput.readDouble();
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Double";
        }
    };

    public static NBTTagDouble valueOf(double d0) {
        return d0 == 0.0D ? NBTTagDouble.ZERO : new NBTTagDouble(d0);
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeDouble(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 16;
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public NBTTagType<NBTTagDouble> getType() {
        return NBTTagDouble.TYPE;
    }

    @Override
    public NBTTagDouble copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitDouble(this);
    }

    @Override
    public long longValue() {
        return (long) Math.floor(this.value);
    }

    @Override
    public int intValue() {
        return MathHelper.floor(this.value);
    }

    @Override
    public short shortValue() {
        return (short) (MathHelper.floor(this.value) & '\uffff');
    }

    @Override
    public byte byteValue() {
        return (byte) (MathHelper.floor(this.value) & 255);
    }

    @Override
    public double doubleValue() {
        return this.value;
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

        stringtagvisitor.visitDouble(this);
        return stringtagvisitor.build();
    }
}
