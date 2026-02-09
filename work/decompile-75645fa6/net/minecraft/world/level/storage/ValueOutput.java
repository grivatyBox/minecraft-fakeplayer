package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;

public interface ValueOutput {

    <T> void store(String s, Codec<T> codec, T t0);

    <T> void storeNullable(String s, Codec<T> codec, @Nullable T t0);

    /** @deprecated */
    @Deprecated
    <T> void store(MapCodec<T> mapcodec, T t0);

    void putBoolean(String s, boolean flag);

    void putByte(String s, byte b0);

    void putShort(String s, short short0);

    void putInt(String s, int i);

    void putLong(String s, long i);

    void putFloat(String s, float f);

    void putDouble(String s, double d0);

    void putString(String s, String s1);

    void putIntArray(String s, int[] aint);

    ValueOutput child(String s);

    ValueOutput.b childrenList(String s);

    <T> ValueOutput.a<T> list(String s, Codec<T> codec);

    void discard(String s);

    boolean isEmpty();

    public interface a<T> {

        void add(T t0);

        boolean isEmpty();
    }

    public interface b {

        ValueOutput addChild();

        void discardLast();

        boolean isEmpty();
    }
}
