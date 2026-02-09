package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface DataComponentPredicate {

    Codec<Map<DataComponentPredicate.b<?>, DataComponentPredicate>> CODEC = Codec.dispatchedMap(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.byNameCodec(), DataComponentPredicate.b::codec);
    StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.a<?>> SINGLE_STREAM_CODEC = ByteBufCodecs.registry(Registries.DATA_COMPONENT_PREDICATE_TYPE).dispatch(DataComponentPredicate.a::type, DataComponentPredicate.b::singleStreamCodec);
    StreamCodec<RegistryFriendlyByteBuf, Map<DataComponentPredicate.b<?>, DataComponentPredicate>> STREAM_CODEC = DataComponentPredicate.SINGLE_STREAM_CODEC.apply(ByteBufCodecs.list(64)).map((list) -> {
        return (Map) list.stream().collect(Collectors.toMap(DataComponentPredicate.a::type, DataComponentPredicate.a::predicate));
    }, (map) -> {
        return map.entrySet().stream().map(DataComponentPredicate.a::fromEntry).toList();
    });

    static MapCodec<DataComponentPredicate.a<?>> singleCodec(String s) {
        return BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE.byNameCodec().dispatchMap(s, DataComponentPredicate.a::type, DataComponentPredicate.b::wrappedCodec);
    }

    boolean matches(DataComponentGetter datacomponentgetter);

    public static final class b<T extends DataComponentPredicate> {

        private final Codec<T> codec;
        private final MapCodec<DataComponentPredicate.a<T>> wrappedCodec;
        private final StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.a<T>> singleStreamCodec;

        public b(Codec<T> codec) {
            this.codec = codec;
            this.wrappedCodec = RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(codec.fieldOf("value").forGetter(DataComponentPredicate.a::predicate)).apply(instance, (datacomponentpredicate) -> {
                    return new DataComponentPredicate.a(this, datacomponentpredicate);
                });
            });
            this.singleStreamCodec = ByteBufCodecs.fromCodecWithRegistries(codec).map((datacomponentpredicate) -> {
                return new DataComponentPredicate.a(this, datacomponentpredicate);
            }, DataComponentPredicate.a::predicate);
        }

        public Codec<T> codec() {
            return this.codec;
        }

        public MapCodec<DataComponentPredicate.a<T>> wrappedCodec() {
            return this.wrappedCodec;
        }

        public StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate.a<T>> singleStreamCodec() {
            return this.singleStreamCodec;
        }
    }

    public static record a<T extends DataComponentPredicate>(DataComponentPredicate.b<T> type, T predicate) {

        private static <T extends DataComponentPredicate> DataComponentPredicate.a<T> fromEntry(Map.Entry<DataComponentPredicate.b<?>, T> map_entry) {
            return new DataComponentPredicate.a<T>((DataComponentPredicate.b) map_entry.getKey(), (DataComponentPredicate) map_entry.getValue());
        }
    }
}
