package net.minecraft.core.component;

import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface DataComponentHolder extends DataComponentGetter {

    DataComponentMap getComponents();

    @Nullable
    @Override
    default <T> T get(DataComponentType<? extends T> datacomponenttype) {
        return (T) this.getComponents().get(datacomponenttype);
    }

    default <T> Stream<T> getAllOfType(Class<? extends T> oclass) {
        return this.getComponents().stream().map(TypedDataComponent::value).filter((object) -> {
            return oclass.isAssignableFrom(object.getClass());
        }).map((object) -> {
            return object;
        });
    }

    @Override
    default <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0) {
        return (T) this.getComponents().getOrDefault(datacomponenttype, t0);
    }

    default boolean has(DataComponentType<?> datacomponenttype) {
        return this.getComponents().has(datacomponenttype);
    }
}
