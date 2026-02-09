package net.minecraft.world.level.saveddata;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.datafix.DataFixTypes;

public record SavedDataType<T extends PersistentBase>(String id, Function<PersistentBase.a, T> constructor, Function<PersistentBase.a, Codec<T>> codec, DataFixTypes dataFixType) {

    public SavedDataType(String s, Supplier<T> supplier, Codec<T> codec, DataFixTypes datafixtypes) {
        this(s, (persistentbase_a) -> {
            return (PersistentBase) supplier.get();
        }, (persistentbase_a) -> {
            return codec;
        }, datafixtypes);
    }

    public boolean equals(Object object) {
        boolean flag;

        if (object instanceof SavedDataType<?> saveddatatype) {
            if (this.id.equals(saveddatatype.id)) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return "SavedDataType[" + this.id + "]";
    }
}
