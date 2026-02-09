package net.minecraft.advancements.critereon;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public interface SingleComponentItemPredicate<T> extends DataComponentPredicate {

    @Override
    default boolean matches(DataComponentGetter datacomponentgetter) {
        T t0 = (T) datacomponentgetter.get(this.componentType());

        return t0 != null && this.matches(t0);
    }

    DataComponentType<T> componentType();

    boolean matches(T t0);
}
