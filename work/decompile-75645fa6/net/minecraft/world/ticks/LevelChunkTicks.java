package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;

public class LevelChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {

    private final Queue<NextTickListEntry<T>> tickQueue;
    @Nullable
    private List<TickListChunk<T>> pendingTicks;
    private final Set<NextTickListEntry<?>> ticksPerPosition;
    @Nullable
    private BiConsumer<LevelChunkTicks<T>, NextTickListEntry<T>> onTickAdded;

    public LevelChunkTicks() {
        this.tickQueue = new PriorityQueue(NextTickListEntry.DRAIN_ORDER);
        this.ticksPerPosition = new ObjectOpenCustomHashSet(NextTickListEntry.UNIQUE_TICK_HASH);
    }

    public LevelChunkTicks(List<TickListChunk<T>> list) {
        this.tickQueue = new PriorityQueue(NextTickListEntry.DRAIN_ORDER);
        this.ticksPerPosition = new ObjectOpenCustomHashSet(NextTickListEntry.UNIQUE_TICK_HASH);
        this.pendingTicks = list;

        for (TickListChunk<T> ticklistchunk : list) {
            this.ticksPerPosition.add(NextTickListEntry.probe(ticklistchunk.type(), ticklistchunk.pos()));
        }

    }

    public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, NextTickListEntry<T>> biconsumer) {
        this.onTickAdded = biconsumer;
    }

    @Nullable
    public NextTickListEntry<T> peek() {
        return (NextTickListEntry) this.tickQueue.peek();
    }

    @Nullable
    public NextTickListEntry<T> poll() {
        NextTickListEntry<T> nextticklistentry = (NextTickListEntry) this.tickQueue.poll();

        if (nextticklistentry != null) {
            this.ticksPerPosition.remove(nextticklistentry);
        }

        return nextticklistentry;
    }

    @Override
    public void schedule(NextTickListEntry<T> nextticklistentry) {
        if (this.ticksPerPosition.add(nextticklistentry)) {
            this.scheduleUnchecked(nextticklistentry);
        }

    }

    private void scheduleUnchecked(NextTickListEntry<T> nextticklistentry) {
        this.tickQueue.add(nextticklistentry);
        if (this.onTickAdded != null) {
            this.onTickAdded.accept(this, nextticklistentry);
        }

    }

    @Override
    public boolean hasScheduledTick(BlockPosition blockposition, T t0) {
        return this.ticksPerPosition.contains(NextTickListEntry.probe(t0, blockposition));
    }

    public void removeIf(Predicate<NextTickListEntry<T>> predicate) {
        Iterator<NextTickListEntry<T>> iterator = this.tickQueue.iterator();

        while (iterator.hasNext()) {
            NextTickListEntry<T> nextticklistentry = (NextTickListEntry) iterator.next();

            if (predicate.test(nextticklistentry)) {
                iterator.remove();
                this.ticksPerPosition.remove(nextticklistentry);
            }
        }

    }

    public Stream<NextTickListEntry<T>> getAll() {
        return this.tickQueue.stream();
    }

    @Override
    public int count() {
        return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
    }

    @Override
    public List<TickListChunk<T>> pack(long i) {
        List<TickListChunk<T>> list = new ArrayList(this.tickQueue.size());

        if (this.pendingTicks != null) {
            list.addAll(this.pendingTicks);
        }

        for (NextTickListEntry<T> nextticklistentry : this.tickQueue) {
            list.add(nextticklistentry.toSavedTick(i));
        }

        return list;
    }

    public void unpack(long i) {
        if (this.pendingTicks != null) {
            int j = -this.pendingTicks.size();

            for (TickListChunk<T> ticklistchunk : this.pendingTicks) {
                this.scheduleUnchecked(ticklistchunk.unpack(i, (long) (j++)));
            }
        }

        this.pendingTicks = null;
    }
}
