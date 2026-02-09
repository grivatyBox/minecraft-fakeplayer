package net.minecraft.core.component;

import javax.annotation.Nullable;

public interface DataComponentGetter {

    @Nullable
    <T> T get(DataComponentType<? extends T> datacomponenttype);

    default <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0) {
        T t1 = (T) this.get(datacomponenttype);

        return t1 != null ? t1 : t0;
    }

    @Nullable
    default <T> TypedDataComponent<T> getTyped(DataComponentType<T> datacomponenttype) {
        T t0 = (T) this.get(datacomponenttype);

        return t0 != null ? new TypedDataComponent(datacomponenttype, t0) : null;
    }
}
