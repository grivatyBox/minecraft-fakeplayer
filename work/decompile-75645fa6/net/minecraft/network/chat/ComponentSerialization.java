package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.ExtraCodecs;

public class ComponentSerialization {

    public static final Codec<IChatBaseComponent> CODEC = Codec.recursive("Component", ComponentSerialization::createCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, IChatBaseComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(ComponentSerialization.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<IChatBaseComponent>> OPTIONAL_STREAM_CODEC = ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<RegistryFriendlyByteBuf, IChatBaseComponent> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(ComponentSerialization.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<IChatBaseComponent>> TRUSTED_OPTIONAL_STREAM_CODEC = ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<ByteBuf, IChatBaseComponent> TRUSTED_CONTEXT_FREE_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(ComponentSerialization.CODEC);

    public ComponentSerialization() {}

    public static Codec<IChatBaseComponent> flatRestrictedCodec(final int i) {
        return new Codec<IChatBaseComponent>() {
            public <T> DataResult<Pair<IChatBaseComponent, T>> decode(DynamicOps<T> dynamicops, T t0) {
                return ComponentSerialization.CODEC.decode(dynamicops, t0).flatMap((pair) -> {
                    return this.isTooLarge(dynamicops, (IChatBaseComponent) pair.getFirst()) ? DataResult.error(() -> {
                        return "Component was too large: greater than max size " + i;
                    }) : DataResult.success(pair);
                });
            }

            public <T> DataResult<T> encode(IChatBaseComponent ichatbasecomponent, DynamicOps<T> dynamicops, T t0) {
                return ComponentSerialization.CODEC.encodeStart(dynamicops, ichatbasecomponent);
            }

            private <T> boolean isTooLarge(DynamicOps<T> dynamicops, IChatBaseComponent ichatbasecomponent) {
                DataResult<JsonElement> dataresult = ComponentSerialization.CODEC.encodeStart(asJsonOps(dynamicops), ichatbasecomponent);

                return dataresult.isSuccess() && ChatDeserializer.encodesLongerThan((JsonElement) dataresult.getOrThrow(), i);
            }

            private static <T> DynamicOps<JsonElement> asJsonOps(DynamicOps<T> dynamicops) {
                if (dynamicops instanceof RegistryOps<T> registryops) {
                    return registryops.<JsonElement>withParent(JsonOps.INSTANCE);
                } else {
                    return JsonOps.INSTANCE;
                }
            }
        };
    }

    private static IChatMutableComponent createFromList(List<IChatBaseComponent> list) {
        IChatMutableComponent ichatmutablecomponent = ((IChatBaseComponent) list.get(0)).copy();

        for (int i = 1; i < list.size(); ++i) {
            ichatmutablecomponent.append((IChatBaseComponent) list.get(i));
        }

        return ichatmutablecomponent;
    }

    public static <T> MapCodec<T> createLegacyComponentMatcher(ExtraCodecs.b<String, MapCodec<? extends T>> extracodecs_b, Function<T, MapCodec<? extends T>> function, String s) {
        MapCodec<T> mapcodec = new ComponentSerialization.a<T>(extracodecs_b.values(), function);
        MapCodec<T> mapcodec1 = extracodecs_b.codec(Codec.STRING).dispatchMap(s, function, (mapcodec2) -> {
            return mapcodec2;
        });
        MapCodec<T> mapcodec2 = new ComponentSerialization.b<T>(s, mapcodec1, mapcodec);

        return ExtraCodecs.orCompressed(mapcodec2, mapcodec1);
    }

    private static Codec<IChatBaseComponent> createCodec(Codec<IChatBaseComponent> codec) {
        ExtraCodecs.b<String, MapCodec<? extends ComponentContents>> extracodecs_b = new ExtraCodecs.b<String, MapCodec<? extends ComponentContents>>();

        bootstrap(extracodecs_b);
        MapCodec<ComponentContents> mapcodec = createLegacyComponentMatcher(extracodecs_b, ComponentContents::codec, "type");
        Codec<IChatBaseComponent> codec1 = RecordCodecBuilder.create((instance) -> {
            return instance.group(mapcodec.forGetter(IChatBaseComponent::getContents), ExtraCodecs.nonEmptyList(codec.listOf()).optionalFieldOf("extra", List.of()).forGetter(IChatBaseComponent::getSiblings), ChatModifier.ChatModifierSerializer.MAP_CODEC.forGetter(IChatBaseComponent::getStyle)).apply(instance, IChatMutableComponent::new);
        });

        return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(codec.listOf())), codec1).xmap((either) -> {
            return (IChatBaseComponent) either.map((either1) -> {
                return (IChatBaseComponent) either1.map(IChatBaseComponent::literal, ComponentSerialization::createFromList);
            }, (ichatbasecomponent) -> {
                return ichatbasecomponent;
            });
        }, (ichatbasecomponent) -> {
            String s = ichatbasecomponent.tryCollapseToString();

            return s != null ? Either.left(Either.left(s)) : Either.right(ichatbasecomponent);
        });
    }

    private static void bootstrap(ExtraCodecs.b<String, MapCodec<? extends ComponentContents>> extracodecs_b) {
        extracodecs_b.put("text", LiteralContents.MAP_CODEC);
        extracodecs_b.put("translatable", TranslatableContents.MAP_CODEC);
        extracodecs_b.put("keybind", KeybindContents.MAP_CODEC);
        extracodecs_b.put("score", ScoreContents.MAP_CODEC);
        extracodecs_b.put("selector", SelectorContents.MAP_CODEC);
        extracodecs_b.put("nbt", NbtContents.MAP_CODEC);
        extracodecs_b.put("object", ObjectContents.MAP_CODEC);
    }

    private static class b<T> extends MapCodec<T> {

        private final String typeFieldName;
        private final MapCodec<T> typed;
        private final MapCodec<T> fuzzy;

        public b(String s, MapCodec<T> mapcodec, MapCodec<T> mapcodec1) {
            this.typeFieldName = s;
            this.typed = mapcodec;
            this.fuzzy = mapcodec1;
        }

        public <O> DataResult<T> decode(DynamicOps<O> dynamicops, MapLike<O> maplike) {
            return maplike.get(this.typeFieldName) != null ? this.typed.decode(dynamicops, maplike) : this.fuzzy.decode(dynamicops, maplike);
        }

        public <O> RecordBuilder<O> encode(T t0, DynamicOps<O> dynamicops, RecordBuilder<O> recordbuilder) {
            return this.fuzzy.encode(t0, dynamicops, recordbuilder);
        }

        public <T1> Stream<T1> keys(DynamicOps<T1> dynamicops) {
            return Stream.concat(this.typed.keys(dynamicops), this.fuzzy.keys(dynamicops)).distinct();
        }
    }

    private static class a<T> extends MapCodec<T> {

        private final Collection<MapCodec<? extends T>> codecs;
        private final Function<T, ? extends MapEncoder<? extends T>> encoderGetter;

        public a(Collection<MapCodec<? extends T>> collection, Function<T, ? extends MapEncoder<? extends T>> function) {
            this.codecs = collection;
            this.encoderGetter = function;
        }

        public <S> DataResult<T> decode(DynamicOps<S> dynamicops, MapLike<S> maplike) {
            for (MapDecoder<? extends T> mapdecoder : this.codecs) {
                DataResult<? extends T> dataresult = mapdecoder.decode(dynamicops, maplike);

                if (dataresult.result().isPresent()) {
                    return dataresult;
                }
            }

            return DataResult.error(() -> {
                return "No matching codec found";
            });
        }

        public <S> RecordBuilder<S> encode(T t0, DynamicOps<S> dynamicops, RecordBuilder<S> recordbuilder) {
            MapEncoder<T> mapencoder = (MapEncoder) this.encoderGetter.apply(t0);

            return mapencoder.encode(t0, dynamicops, recordbuilder);
        }

        public <S> Stream<S> keys(DynamicOps<S> dynamicops) {
            return this.codecs.stream().flatMap((mapcodec) -> {
                return mapcodec.keys(dynamicops);
            }).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }
}
