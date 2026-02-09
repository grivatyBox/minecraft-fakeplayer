package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public final class WeightedList<E> {

    private static final int FLAT_THRESHOLD = 64;
    private final int totalWeight;
    private final List<Weighted<E>> items;
    @Nullable
    private final WeightedList.d<E> selector;

    WeightedList(List<? extends Weighted<E>> list) {
        this.items = List.copyOf(list);
        this.totalWeight = WeightedRandom2.getTotalWeight(list, Weighted::weight);
        if (this.totalWeight == 0) {
            this.selector = null;
        } else if (this.totalWeight < 64) {
            this.selector = new WeightedList.c<E>(this.items, this.totalWeight);
        } else {
            this.selector = new WeightedList.b<E>(this.items);
        }

    }

    public static <E> WeightedList<E> of() {
        return new WeightedList<E>(List.of());
    }

    public static <E> WeightedList<E> of(E e0) {
        return new WeightedList<E>(List.of(new Weighted(e0, 1)));
    }

    @SafeVarargs
    public static <E> WeightedList<E> of(Weighted<E>... aweighted) {
        return new WeightedList<E>(List.of(aweighted));
    }

    public static <E> WeightedList<E> of(List<Weighted<E>> list) {
        return new WeightedList<E>(list);
    }

    public static <E> WeightedList.a<E> builder() {
        return new WeightedList.a<E>();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public <T> WeightedList<T> map(Function<E, T> function) {
        return new WeightedList<T>(Lists.transform(this.items, (weighted) -> {
            return weighted.map(function);
        }));
    }

    public Optional<E> getRandom(RandomSource randomsource) {
        if (this.selector == null) {
            return Optional.empty();
        } else {
            int i = randomsource.nextInt(this.totalWeight);

            return Optional.of(this.selector.get(i));
        }
    }

    public E getRandomOrThrow(RandomSource randomsource) {
        if (this.selector == null) {
            throw new IllegalStateException("Weighted list has no elements");
        } else {
            int i = randomsource.nextInt(this.totalWeight);

            return this.selector.get(i);
        }
    }

    public List<Weighted<E>> unwrap() {
        return this.items;
    }

    public static <E> Codec<WeightedList<E>> codec(Codec<E> codec) {
        return Weighted.codec(codec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> codec(MapCodec<E> mapcodec) {
        return Weighted.codec(mapcodec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(Codec<E> codec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(codec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(MapCodec<E> mapcodec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(mapcodec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public boolean contains(E e0) {
        for (Weighted<E> weighted : this.items) {
            if (weighted.value().equals(e0)) {
                return true;
            }
        }

        return false;
    }

    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof WeightedList)) {
            return false;
        } else {
            WeightedList<?> weightedlist = (WeightedList) object;

            return this.totalWeight == weightedlist.totalWeight && Objects.equals(this.items, weightedlist.items);
        }
    }

    public int hashCode() {
        int i = this.totalWeight;

        i = 31 * i + this.items.hashCode();
        return i;
    }

    public static class a<E> {

        private final ImmutableList.Builder<Weighted<E>> result = ImmutableList.builder();

        public a() {}

        public WeightedList.a<E> add(E e0) {
            return this.add(e0, 1);
        }

        public WeightedList.a<E> add(E e0, int i) {
            this.result.add(new Weighted(e0, i));
            return this;
        }

        public WeightedList<E> build() {
            return new WeightedList<E>(this.result.build());
        }
    }

    private static class c<E> implements WeightedList.d<E> {

        private final Object[] entries;

        c(List<Weighted<E>> list, int i) {
            this.entries = new Object[i];
            int j = 0;

            for (Weighted<E> weighted : list) {
                int k = weighted.weight();

                Arrays.fill(this.entries, j, j + k, weighted.value());
                j += k;
            }

        }

        @Override
        public E get(int i) {
            return (E) this.entries[i];
        }
    }

    private static class b<E> implements WeightedList.d<E> {

        private final Weighted<?>[] entries;

        b(List<Weighted<E>> list) {
            this.entries = (Weighted[]) list.toArray((i) -> {
                return new Weighted[i];
            });
        }

        @Override
        public E get(int i) {
            for (Weighted<?> weighted : this.entries) {
                i -= weighted.weight();
                if (i < 0) {
                    return (E) weighted.value();
                }
            }

            throw new IllegalStateException(i + " exceeded total weight");
        }
    }

    private interface d<E> {

        E get(int i);
    }
}
