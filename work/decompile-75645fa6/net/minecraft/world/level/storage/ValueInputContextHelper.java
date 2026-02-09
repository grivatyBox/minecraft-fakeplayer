package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTBase;

public class ValueInputContextHelper {

    final HolderLookup.a lookup;
    private final DynamicOps<NBTBase> ops;
    final ValueInput.b emptyChildList = new ValueInput.b() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Stream<ValueInput> stream() {
            return Stream.empty();
        }

        public Iterator<ValueInput> iterator() {
            return Collections.emptyIterator();
        }
    };
    private final ValueInput.a<Object> emptyTypedList = new ValueInput.a<Object>() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Stream<Object> stream() {
            return Stream.empty();
        }

        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }
    };
    private final ValueInput empty = new ValueInput() {
        @Override
        public <T> Optional<T> read(String s, Codec<T> codec) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> read(MapCodec<T> mapcodec) {
            return Optional.empty();
        }

        @Override
        public Optional<ValueInput> child(String s) {
            return Optional.empty();
        }

        @Override
        public ValueInput childOrEmpty(String s) {
            return this;
        }

        @Override
        public Optional<ValueInput.b> childrenList(String s) {
            return Optional.empty();
        }

        @Override
        public ValueInput.b childrenListOrEmpty(String s) {
            return ValueInputContextHelper.this.emptyChildList;
        }

        @Override
        public <T> Optional<ValueInput.a<T>> list(String s, Codec<T> codec) {
            return Optional.empty();
        }

        @Override
        public <T> ValueInput.a<T> listOrEmpty(String s, Codec<T> codec) {
            return ValueInputContextHelper.this.<T>emptyTypedList();
        }

        @Override
        public boolean getBooleanOr(String s, boolean flag) {
            return flag;
        }

        @Override
        public byte getByteOr(String s, byte b0) {
            return b0;
        }

        @Override
        public int getShortOr(String s, short short0) {
            return short0;
        }

        @Override
        public Optional<Integer> getInt(String s) {
            return Optional.empty();
        }

        @Override
        public int getIntOr(String s, int i) {
            return i;
        }

        @Override
        public long getLongOr(String s, long i) {
            return i;
        }

        @Override
        public Optional<Long> getLong(String s) {
            return Optional.empty();
        }

        @Override
        public float getFloatOr(String s, float f) {
            return f;
        }

        @Override
        public double getDoubleOr(String s, double d0) {
            return d0;
        }

        @Override
        public Optional<String> getString(String s) {
            return Optional.empty();
        }

        @Override
        public String getStringOr(String s, String s1) {
            return s1;
        }

        @Override
        public HolderLookup.a lookup() {
            return ValueInputContextHelper.this.lookup;
        }

        @Override
        public Optional<int[]> getIntArray(String s) {
            return Optional.empty();
        }
    };

    public ValueInputContextHelper(HolderLookup.a holderlookup_a, DynamicOps<NBTBase> dynamicops) {
        this.lookup = holderlookup_a;
        this.ops = holderlookup_a.<NBTBase>createSerializationContext(dynamicops);
    }

    public DynamicOps<NBTBase> ops() {
        return this.ops;
    }

    public HolderLookup.a lookup() {
        return this.lookup;
    }

    public ValueInput empty() {
        return this.empty;
    }

    public ValueInput.b emptyList() {
        return this.emptyChildList;
    }

    public <T> ValueInput.a<T> emptyTypedList() {
        return this.emptyTypedList;
    }
}
