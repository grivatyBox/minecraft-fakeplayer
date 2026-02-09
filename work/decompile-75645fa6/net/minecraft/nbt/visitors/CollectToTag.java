package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTTagType;
import net.minecraft.nbt.StreamTagVisitor;

public class CollectToTag implements StreamTagVisitor {

    private final Deque<CollectToTag.b> containerStack = new ArrayDeque();

    public CollectToTag() {
        this.containerStack.addLast(new CollectToTag.d());
    }

    @Nullable
    public NBTBase getResult() {
        return ((CollectToTag.b) this.containerStack.getFirst()).build();
    }

    protected int depth() {
        return this.containerStack.size() - 1;
    }

    private void appendEntry(NBTBase nbtbase) {
        ((CollectToTag.b) this.containerStack.getLast()).acceptValue(nbtbase);
    }

    @Override
    public StreamTagVisitor.b visitEnd() {
        this.appendEntry(NBTTagEnd.INSTANCE);
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(String s) {
        this.appendEntry(NBTTagString.valueOf(s));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(byte b0) {
        this.appendEntry(NBTTagByte.valueOf(b0));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(short short0) {
        this.appendEntry(NBTTagShort.valueOf(short0));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(int i) {
        this.appendEntry(NBTTagInt.valueOf(i));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(long i) {
        this.appendEntry(NBTTagLong.valueOf(i));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(float f) {
        this.appendEntry(NBTTagFloat.valueOf(f));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(double d0) {
        this.appendEntry(NBTTagDouble.valueOf(d0));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(byte[] abyte) {
        this.appendEntry(new NBTTagByteArray(abyte));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(int[] aint) {
        this.appendEntry(new NBTTagIntArray(aint));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visit(long[] along) {
        this.appendEntry(new NBTTagLongArray(along));
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visitList(NBTTagType<?> nbttagtype, int i) {
        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.a visitElement(NBTTagType<?> nbttagtype, int i) {
        this.enterContainerIfNeeded(nbttagtype);
        return StreamTagVisitor.a.ENTER;
    }

    @Override
    public StreamTagVisitor.a visitEntry(NBTTagType<?> nbttagtype) {
        return StreamTagVisitor.a.ENTER;
    }

    @Override
    public StreamTagVisitor.a visitEntry(NBTTagType<?> nbttagtype, String s) {
        ((CollectToTag.b) this.containerStack.getLast()).acceptKey(s);
        this.enterContainerIfNeeded(nbttagtype);
        return StreamTagVisitor.a.ENTER;
    }

    private void enterContainerIfNeeded(NBTTagType<?> nbttagtype) {
        if (nbttagtype == NBTTagList.TYPE) {
            this.containerStack.addLast(new CollectToTag.c());
        } else if (nbttagtype == NBTTagCompound.TYPE) {
            this.containerStack.addLast(new CollectToTag.a());
        }

    }

    @Override
    public StreamTagVisitor.b visitContainerEnd() {
        CollectToTag.b collecttotag_b = (CollectToTag.b) this.containerStack.removeLast();
        NBTBase nbtbase = collecttotag_b.build();

        if (nbtbase != null) {
            ((CollectToTag.b) this.containerStack.getLast()).acceptValue(nbtbase);
        }

        return StreamTagVisitor.b.CONTINUE;
    }

    @Override
    public StreamTagVisitor.b visitRootEntry(NBTTagType<?> nbttagtype) {
        this.enterContainerIfNeeded(nbttagtype);
        return StreamTagVisitor.b.CONTINUE;
    }

    private interface b {

        default void acceptKey(String s) {}

        void acceptValue(NBTBase nbtbase);

        @Nullable
        NBTBase build();
    }

    private static class d implements CollectToTag.b {

        @Nullable
        private NBTBase result;

        d() {}

        @Override
        public void acceptValue(NBTBase nbtbase) {
            this.result = nbtbase;
        }

        @Nullable
        @Override
        public NBTBase build() {
            return this.result;
        }
    }

    private static class a implements CollectToTag.b {

        private final NBTTagCompound compound = new NBTTagCompound();
        private String lastId = "";

        a() {}

        @Override
        public void acceptKey(String s) {
            this.lastId = s;
        }

        @Override
        public void acceptValue(NBTBase nbtbase) {
            this.compound.put(this.lastId, nbtbase);
        }

        @Override
        public NBTBase build() {
            return this.compound;
        }
    }

    private static class c implements CollectToTag.b {

        private final NBTTagList list = new NBTTagList();

        c() {}

        @Override
        public void acceptValue(NBTBase nbtbase) {
            this.list.addAndUnwrap(nbtbase);
        }

        @Override
        public NBTBase build() {
            return this.list;
        }
    }
}
