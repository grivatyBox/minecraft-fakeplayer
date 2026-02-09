package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.Nullable;

public final class SequencedPriorityIterator<T> extends AbstractIterator<T> {

    private final Int2ObjectMap<Deque<T>> valuesByPriority = new Int2ObjectOpenHashMap();

    public SequencedPriorityIterator() {}

    public void add(T t0, int i) {
        ((Deque) this.valuesByPriority.computeIfAbsent(i, (j) -> {
            return Queues.newArrayDeque();
        })).addLast(t0);
    }

    @Nullable
    protected T computeNext() {
        Optional<Deque<T>> optional = this.valuesByPriority.int2ObjectEntrySet().stream().filter((entry) -> {
            return !((Deque) entry.getValue()).isEmpty();
        }).max(Comparator.comparingInt(Entry::getKey)).map(Entry::getValue);

        return optional.map(Deque::removeFirst).orElseGet(() -> {
            return this.endOfData();
        });
    }
}
