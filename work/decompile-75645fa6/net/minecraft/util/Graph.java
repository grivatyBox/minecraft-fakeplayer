package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {

    private Graph() {}

    public static <T> boolean depthFirstSearch(Map<T, Set<T>> map, Set<T> set, Set<T> set1, Consumer<T> consumer, T t0) {
        if (set.contains(t0)) {
            return false;
        } else if (set1.contains(t0)) {
            return true;
        } else {
            set1.add(t0);

            for (T t1 : (Set) map.getOrDefault(t0, ImmutableSet.of())) {
                if (depthFirstSearch(map, set, set1, consumer, t1)) {
                    return true;
                }
            }

            set1.remove(t0);
            set.add(t0);
            consumer.accept(t0);
            return false;
        }
    }
}
