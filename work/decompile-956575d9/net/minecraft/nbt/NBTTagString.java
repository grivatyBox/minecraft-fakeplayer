package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public record NBTTagString(String value) implements PrimitiveTag {

    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final NBTTagType<NBTTagString> TYPE = new NBTTagType.b<NBTTagString>() {
        @Override
        public NBTTagString load(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            return NBTTagString.valueOf(readAccounted(datainput, nbtreadlimiter));
        }

        @Override
        public StreamTagVisitor.b parse(DataInput datainput, StreamTagVisitor streamtagvisitor, NBTReadLimiter nbtreadlimiter) throws IOException {
            return streamtagvisitor.visit(readAccounted(datainput, nbtreadlimiter));
        }

        private static String readAccounted(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            nbtreadlimiter.accountBytes(36L);
            String s = datainput.readUTF();

            nbtreadlimiter.accountBytes(2L, (long) s.length());
            return s;
        }

        @Override
        public void skip(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
            NBTTagString.skipString(datainput);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
        }
    };
    private static final NBTTagString EMPTY = new NBTTagString("");
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';

    public static void skipString(DataInput datainput) throws IOException {
        datainput.skipBytes(datainput.readUnsignedShort());
    }

    public static NBTTagString valueOf(String s) {
        return s.isEmpty() ? NBTTagString.EMPTY : new NBTTagString(s);
    }

    @Override
    public void write(DataOutput dataoutput) throws IOException {
        dataoutput.writeUTF(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 36 + 2 * this.value.length();
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public NBTTagType<NBTTagString> getType() {
        return NBTTagString.TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringtagvisitor = new StringTagVisitor();

        stringtagvisitor.visitString(this);
        return stringtagvisitor.build();
    }

    @Override
    public NBTTagString copy() {
        return this;
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(this.value);
    }

    @Override
    public void accept(TagVisitor tagvisitor) {
        tagvisitor.visitString(this);
    }

    public static String quoteAndEscape(String s) {
        StringBuilder stringbuilder = new StringBuilder();

        quoteAndEscape(s, stringbuilder);
        return stringbuilder.toString();
    }

    public static void quoteAndEscape(String s, StringBuilder stringbuilder) {
        int i = stringbuilder.length();

        stringbuilder.append(' ');
        char c0 = 0;

        for (int j = 0; j < s.length(); ++j) {
            char c1 = s.charAt(j);

            if (c1 == '\\') {
                stringbuilder.append("\\\\");
            } else if (c1 != '"' && c1 != '\'') {
                String s1 = SnbtGrammar.escapeControlCharacters(c1);

                if (s1 != null) {
                    stringbuilder.append('\\');
                    stringbuilder.append(s1);
                } else {
                    stringbuilder.append(c1);
                }
            } else {
                if (c0 == 0) {
                    c0 = (char) (c1 == '"' ? 39 : 34);
                }

                if (c0 == c1) {
                    stringbuilder.append('\\');
                }

                stringbuilder.append(c1);
            }
        }

        if (c0 == 0) {
            c0 = '"';
        }

        stringbuilder.setCharAt(i, c0);
        stringbuilder.append(c0);
    }

    @Override
    public StreamTagVisitor.b accept(StreamTagVisitor streamtagvisitor) {
        return streamtagvisitor.visit(this.value);
    }
}
