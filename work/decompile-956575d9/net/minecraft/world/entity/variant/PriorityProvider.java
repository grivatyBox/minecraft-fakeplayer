package net.minecraft.world.entity.variant;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.util.RandomSource;

public interface PriorityProvider<Context, Condition extends PriorityProvider.b<Context>> {

    List<PriorityProvider.a<Context, Condition>> selectors();

    static <C, T> Stream<T> select(Stream<T> stream, Function<T, PriorityProvider<C, ?>> function, C c0) {
        List<PriorityProvider.c<C, T>> list = new ArrayList();

        stream.forEach((object) -> {
            PriorityProvider<C, ?> priorityprovider = (PriorityProvider) function.apply(object);

            for (PriorityProvider.a<C, ?> priorityprovider_a : priorityprovider.selectors()) {
                list.add(new PriorityProvider.c(object, priorityprovider_a.priority(), (PriorityProvider.b) DataFixUtils.orElseGet(priorityprovider_a.condition(), PriorityProvider.b::alwaysTrue)));
            }

        });
        list.sort(PriorityProvider.c.HIGHEST_PRIORITY_FIRST);
        Iterator<PriorityProvider.c<C, T>> iterator = list.iterator();
        int i = Integer.MIN_VALUE;

        while (iterator.hasNext()) {
            PriorityProvider.c<C, T> priorityprovider_c = (PriorityProvider.c) iterator.next();

            if (priorityprovider_c.priority < i) {
                iterator.remove();
            } else if (priorityprovider_c.condition.test(c0)) {
                i = priorityprovider_c.priority;
            } else {
                iterator.remove();
            }
        }

        return list.stream().map(PriorityProvider.c::entry);
    }

    static <C, T> Optional<T> pick(Stream<T> stream, Function<T, PriorityProvider<C, ?>> function, RandomSource randomsource, C c0) {
        List<T> list = select(stream, function, c0).toList();

        return SystemUtils.<T>getRandomSafe(list, randomsource);
    }

    static <Context, Condition extends PriorityProvider.b<Context>> List<PriorityProvider.a<Context, Condition>> single(Condition condition, int i) {
        return List.of(new PriorityProvider.a(condition, i));
    }

    static <Context, Condition extends PriorityProvider.b<Context>> List<PriorityProvider.a<Context, Condition>> alwaysTrue(int i) {
        return List.of(new PriorityProvider.a(Optional.empty(), i));
    }

    public static record a<Context, Condition extends PriorityProvider.b<Context>>(Optional<Condition> condition, int priority) {

        public a(Condition condition, int i) {
            this(Optional.of(condition), i);
        }

        public a(int i) {
            this(Optional.empty(), i);
        }

        public static <Context, Condition extends PriorityProvider.b<Context>> Codec<PriorityProvider.a<Context, Condition>> codec(Codec<Condition> codec) {
            return RecordCodecBuilder.create((instance) -> {
                return instance.group(codec.optionalFieldOf("condition").forGetter(PriorityProvider.a::condition), Codec.INT.fieldOf("priority").forGetter(PriorityProvider.a::priority)).apply(instance, PriorityProvider.a::new);
            });
        }
    }

    @FunctionalInterface
    public interface b<C> extends Predicate<C> {

        static <C> PriorityProvider.b<C> alwaysTrue() {
            return (object) -> {
                return true;
            };
        }
    }

    public static record c<C, T>(T entry, int priority, PriorityProvider.b<C> condition) {

        public static final Comparator<PriorityProvider.c<?, ?>> HIGHEST_PRIORITY_FIRST = Comparator.comparingInt(PriorityProvider.c::priority).reversed();
    }
}
