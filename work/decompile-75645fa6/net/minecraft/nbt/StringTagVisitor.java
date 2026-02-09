package net.minecraft.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class StringTagVisitor implements TagVisitor {

    private static final Pattern UNQUOTED_KEY_MATCH = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");
    private final StringBuilder builder = new StringBuilder();

    public StringTagVisitor() {}

    public String build() {
        return this.builder.toString();
    }

    @Override
    public void visitString(NBTTagString nbttagstring) {
        this.builder.append(NBTTagString.quoteAndEscape(nbttagstring.value()));
    }

    @Override
    public void visitByte(NBTTagByte nbttagbyte) {
        this.builder.append(nbttagbyte.value()).append('b');
    }

    @Override
    public void visitShort(NBTTagShort nbttagshort) {
        this.builder.append(nbttagshort.value()).append('s');
    }

    @Override
    public void visitInt(NBTTagInt nbttagint) {
        this.builder.append(nbttagint.value());
    }

    @Override
    public void visitLong(NBTTagLong nbttaglong) {
        this.builder.append(nbttaglong.value()).append('L');
    }

    @Override
    public void visitFloat(NBTTagFloat nbttagfloat) {
        this.builder.append(nbttagfloat.value()).append('f');
    }

    @Override
    public void visitDouble(NBTTagDouble nbttagdouble) {
        this.builder.append(nbttagdouble.value()).append('d');
    }

    @Override
    public void visitByteArray(NBTTagByteArray nbttagbytearray) {
        this.builder.append("[B;");
        byte[] abyte = nbttagbytearray.getAsByteArray();

        for (int i = 0; i < abyte.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }

            this.builder.append(abyte[i]).append('B');
        }

        this.builder.append(']');
    }

    @Override
    public void visitIntArray(NBTTagIntArray nbttagintarray) {
        this.builder.append("[I;");
        int[] aint = nbttagintarray.getAsIntArray();

        for (int i = 0; i < aint.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }

            this.builder.append(aint[i]);
        }

        this.builder.append(']');
    }

    @Override
    public void visitLongArray(NBTTagLongArray nbttaglongarray) {
        this.builder.append("[L;");
        long[] along = nbttaglongarray.getAsLongArray();

        for (int i = 0; i < along.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }

            this.builder.append(along[i]).append('L');
        }

        this.builder.append(']');
    }

    @Override
    public void visitList(NBTTagList nbttaglist) {
        this.builder.append('[');

        for (int i = 0; i < nbttaglist.size(); ++i) {
            if (i != 0) {
                this.builder.append(',');
            }

            nbttaglist.get(i).accept((TagVisitor) this);
        }

        this.builder.append(']');
    }

    @Override
    public void visitCompound(NBTTagCompound nbttagcompound) {
        this.builder.append('{');
        List<Map.Entry<String, NBTBase>> list = new ArrayList(nbttagcompound.entrySet());

        list.sort(Entry.comparingByKey());

        for (int i = 0; i < ((List) list).size(); ++i) {
            Map.Entry<String, NBTBase> map_entry = (Entry) list.get(i);

            if (i != 0) {
                this.builder.append(',');
            }

            this.handleKeyEscape((String) map_entry.getKey());
            this.builder.append(':');
            ((NBTBase) map_entry.getValue()).accept((TagVisitor) this);
        }

        this.builder.append('}');
    }

    private void handleKeyEscape(String s) {
        if (!s.equalsIgnoreCase("true") && !s.equalsIgnoreCase("false") && StringTagVisitor.UNQUOTED_KEY_MATCH.matcher(s).matches()) {
            this.builder.append(s);
        } else {
            NBTTagString.quoteAndEscape(s, this.builder);
        }

    }

    @Override
    public void visitEnd(NBTTagEnd nbttagend) {
        this.builder.append("END");
    }
}
