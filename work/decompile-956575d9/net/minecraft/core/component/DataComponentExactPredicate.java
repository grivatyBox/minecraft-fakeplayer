package net.minecraft.core.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class DataComponentExactPredicate implements Predicate<DataComponentGetter> {

    public static final Codec<DataComponentExactPredicate> CODEC = DataComponentType.VALUE_MAP_CODEC.xmap((map) -> {
        return new DataComponentExactPredicate((List) map.entrySet().stream().map(TypedDataComponent::fromEntryUnchecked).collect(Collectors.toList()));
    }, (datacomponentexactpredicate) -> {
        return (Map) datacomponentexactpredicate.expectedComponents.stream().filter((typeddatacomponent) -> {
            return !typeddatacomponent.type().isTransient();
        }).collect(Collectors.toMap(TypedDataComponent::type, TypedDataComponent::value));
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentExactPredicate> STREAM_CODEC = TypedDataComponent.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DataComponentExactPredicate::new, (datacomponentexactpredicate) -> {
        return datacomponentexactpredicate.expectedComponents;
    });
    public static final DataComponentExactPredicate EMPTY = new DataComponentExactPredicate(List.of());
    private final List<TypedDataComponent<?>> expectedComponents;

    DataComponentExactPredicate(List<TypedDataComponent<?>> list) {
        this.expectedComponents = list;
    }

    public static DataComponentExactPredicate.a builder() {
        return new DataComponentExactPredicate.a();
    }

    public static <T> DataComponentExactPredicate expect(DataComponentType<T> datacomponenttype, T t0) {
        return new DataComponentExactPredicate(List.of(new TypedDataComponent(datacomponenttype, t0)));
    }

    public static DataComponentExactPredicate allOf(DataComponentMap datacomponentmap) {
        return new DataComponentExactPredicate(ImmutableList.copyOf(datacomponentmap));
    }

    public static DataComponentExactPredicate someOf(DataComponentMap datacomponentmap, DataComponentType<?>... adatacomponenttype) {
        DataComponentExactPredicate.a datacomponentexactpredicate_a = new DataComponentExactPredicate.a();

        for (DataComponentType<?> datacomponenttype : adatacomponenttype) {
            TypedDataComponent<?> typeddatacomponent = datacomponentmap.getTyped(datacomponenttype);

            if (typeddatacomponent != null) {
                datacomponentexactpredicate_a.expect(typeddatacomponent);
            }
        }

        return datacomponentexactpredicate_a.build();
    }

    public boolean isEmpty() {
        return this.expectedComponents.isEmpty();
    }

    public boolean equals(Object object) {
        boolean flag;

        if (object instanceof DataComponentExactPredicate datacomponentexactpredicate) {
            if (this.expectedComponents.equals(datacomponentexactpredicate.expectedComponents)) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    public int hashCode() {
        return this.expectedComponents.hashCode();
    }

    public String toString() {
        return this.expectedComponents.toString();
    }

    public boolean test(DataComponentGetter datacomponentgetter) {
        for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
            Object object = datacomponentgetter.get(typeddatacomponent.type());

            if (!Objects.equals(typeddatacomponent.value(), object)) {
                return false;
            }
        }

        return true;
    }

    public boolean alwaysMatches() {
        return this.expectedComponents.isEmpty();
    }

    public DataComponentPatch asPatch() {
        DataComponentPatch.a datacomponentpatch_a = DataComponentPatch.builder();

        for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
            datacomponentpatch_a.set(typeddatacomponent);
        }

        return datacomponentpatch_a.build();
    }

    public static class a {

        private final List<TypedDataComponent<?>> expectedComponents = new ArrayList();

        a() {}

        public <T> DataComponentExactPredicate.a expect(TypedDataComponent<T> typeddatacomponent) {
            return this.expect(typeddatacomponent.type(), typeddatacomponent.value());
        }

        public <T> DataComponentExactPredicate.a expect(DataComponentType<? super T> datacomponenttype, T t0) {
            for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
                if (typeddatacomponent.type() == datacomponenttype) {
                    throw new IllegalArgumentException("Predicate already has component of type: '" + String.valueOf(datacomponenttype) + "'");
                }
            }

            this.expectedComponents.add(new TypedDataComponent(datacomponenttype, t0));
            return this;
        }

        public DataComponentExactPredicate build() {
            return new DataComponentExactPredicate(List.copyOf(this.expectedComponents));
        }
    }
}
