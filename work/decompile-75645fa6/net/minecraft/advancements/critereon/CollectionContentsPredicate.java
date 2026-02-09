package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionContentsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {

    List<P> unpack();

    static <T, P extends Predicate<T>> Codec<CollectionContentsPredicate<T, P>> codec(Codec<P> codec) {
        return codec.listOf().xmap(CollectionContentsPredicate::of, CollectionContentsPredicate::unpack);
    }

    @SafeVarargs
    static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(P... ap) {
        return of(List.of(ap));
    }

    static <T, P extends Predicate<T>> CollectionContentsPredicate<T, P> of(List<P> list) {
        Object object;

        switch (list.size()) {
            case 0:
                object = new CollectionContentsPredicate.c();
                break;
            case 1:
                object = new CollectionContentsPredicate.b((Predicate) list.getFirst());
                break;
            default:
                object = new CollectionContentsPredicate.a(list);
        }

        return (CollectionContentsPredicate<T, P>) object;
    }

    public static class c<T, P extends Predicate<T>> implements CollectionContentsPredicate<T, P> {

        public c() {}

        public boolean test(Iterable<T> iterable) {
            return true;
        }

        @Override
        public List<P> unpack() {
            return List.of();
        }
    }

    public static record b<T, P extends Predicate<T>>(P test) implements CollectionContentsPredicate<T, P> {

        public boolean test(Iterable<T> iterable) {
            for (T t0 : iterable) {
                if (this.test.test(t0)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public List<P> unpack() {
            return List.of(this.test);
        }
    }

    public static record a<T, P extends Predicate<T>>(List<P> tests) implements CollectionContentsPredicate<T, P> {

        public boolean test(Iterable<T> iterable) {
            List<Predicate<T>> list = new ArrayList(this.tests);

            for (T t0 : iterable) {
                list.removeIf((predicate) -> {
                    return predicate.test(t0);
                });
                if (list.isEmpty()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public List<P> unpack() {
            return this.tests;
        }
    }
}
