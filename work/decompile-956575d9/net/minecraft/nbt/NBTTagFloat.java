package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.MathHelper;

public record NBTTagFloat(float value) implements NBTNumber {

    private static final int SELF_SIZE_IN_BYTES = 12;
    public static final NBTTagFloat ZERO = new NBTTagFloat(0.0F);
    public static final NBTTagType<NBTTagFloat> TYPE = new NBTTagType.a<NBTTagFloat>() {
        @Override
        public NBTTagFloat load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagFloat.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static float readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(12L);
            return datainput.readFloat();
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public String getName() {
            return "FLOAT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Float";
        }
    };

    public static NBTTagFloat valueOf(float f) {
        return f == 0.0F ? NBTTagFloat.ZERO : new NBTTagFloat(f);
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeFloat(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 12;
    }

    @Override
    public byte getId() {
        return 5;
    }

    @Override
    public NBTTagType<NBTTagFloat> getType() {
        return NBTTagFloat.TYPE;
    }

    @Override
    public NBTTagFloat copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitFloat(this);
    }

    @Override
    public long longValue() {
        return (long) this.value;
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
        return (double) this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
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

        stringtagvisitor.visitFloat(this);
        return stringtagvisitor.build();
    }
}
