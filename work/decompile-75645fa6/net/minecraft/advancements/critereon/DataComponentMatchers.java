package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DataComponentMatchers(DataComponentExactPredicate exact, Map<DataComponentPredicate.b<?>, DataComponentPredicate> partial) implements Predicate<DataComponentGetter> {

    public static final DataComponentMatchers ANY = new DataComponentMatchers(DataComponentExactPredicate.EMPTY, Map.of());
    public static final MapCodec<DataComponentMatchers> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(DataComponentMatchers::exact), DataComponentPredicate.CODEC.optionalFieldOf("predicates", Map.of()).forGetter(DataComponentMatchers::partial)).apply(instance, DataComponentMatchers::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentMatchers> STREAM_CODEC = StreamCodec.composite(DataComponentExactPredicate.STREAM_CODEC, DataComponentMatchers::exact, DataComponentPredicate.STREAM_CODEC, DataComponentMatchers::partial, DataComponentMatchers::new);

    public boolean test(DataComponentGetter datacomponentgetter) {
        if (!this.exact.test(datacomponentgetter)) {
            return false;
        } else {
            for (DataComponentPredicate datacomponentpredicate : this.partial.values()) {
                if (!datacomponentpredicate.matches(datacomponentgetter)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isEmpty() {
        return this.exact.isEmpty() && this.partial.isEmpty();
    }

    public static class a {

        private DataComponentExactPredicate exact;
        private final ImmutableMap.Builder<DataComponentPredicate.b<?>, DataComponentPredicate> partial;

        private a() {
            this.exact = DataComponentExactPredicate.EMPTY;
            this.partial = ImmutableMap.builder();
        }

        public static DataComponentMatchers.a components() {
            return new DataComponentMatchers.a();
        }

        public <T extends DataComponentPredicate> DataComponentMatchers.a partial(DataComponentPredicate.b<T> datacomponentpredicate_b, T t0) {
            this.partial.put(datacomponentpredicate_b, t0);
            return this;
        }

        public DataComponentMatchers.a exact(DataComponentExactPredicate datacomponentexactpredicate) {
            this.exact = datacomponentexactpredicate;
            return this;
        }

        public DataComponentMatchers build() {
            return new DataComponentMatchers(this.exact, this.partial.buildOrThrow());
        }
    }
}
