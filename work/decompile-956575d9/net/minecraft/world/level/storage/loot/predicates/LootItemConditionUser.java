package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

public interface LootItemConditionUser<T extends LootItemConditionUser<T>> {

    T when(LootItemCondition.a lootitemcondition_a);

    default <E> T when(Iterable<E> iterable, Function<E, LootItemCondition.a> function) {
        T t0 = this.unwrap();

        for (E e0 : iterable) {
            t0 = t0.when((LootItemCondition.a) function.apply(e0));
        }

        return t0;
    }

    T unwrap();
}
