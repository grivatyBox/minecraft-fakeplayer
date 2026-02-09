package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;

public interface ValueInput {

    <T> Optional<T> read(String s, Codec<T> codec);

    /** @deprecated */
    @Deprecated
    <T> Optional<T> read(MapCodec<T> mapcodec);

    Optional<ValueInput> child(String s);

    ValueInput childOrEmpty(String s);

    Optional<ValueInput.b> childrenList(String s);

    ValueInput.b childrenListOrEmpty(String s);

    <T> Optional<ValueInput.a<T>> list(String s, Codec<T> codec);

    <T> ValueInput.a<T> listOrEmpty(String s, Codec<T> codec);

    boolean getBooleanOr(String s, boolean flag);

    byte getByteOr(String s, byte b0);

    int getShortOr(String s, short short0);

    Optional<Integer> getInt(String s);

    int getIntOr(String s, int i);

    long getLongOr(String s, long i);

    Optional<Long> getLong(String s);

    float getFloatOr(String s, float f);

    double getDoubleOr(String s, double d0);

    Optional<String> getString(String s);

    String getStringOr(String s, String s1);

    Optional<int[]> getIntArray(String s);

    /** @deprecated */
    @Deprecated
    HolderLookup.a lookup();

    public interface a<T> extends Iterable<T> {

        boolean isEmpty();

        Stream<T> stream();
    }

    public interface b extends Iterable<ValueInput> {

        boolean isEmpty();

        Stream<ValueInput> stream();
    }
}
