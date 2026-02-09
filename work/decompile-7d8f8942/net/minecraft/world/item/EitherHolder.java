package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Either<Holder<T>, ResourceKey<T>> contents) {

    public EitherHolder(Holder<T> holder) {
        this(Either.left(holder));
    }

    public EitherHolder(ResourceKey<T> resourcekey) {
        this(Either.right(resourcekey));
    }

    public static <T> Codec<EitherHolder<T>> codec(ResourceKey<IRegistry<T>> resourcekey, Codec<Holder<T>> codec) {
        return Codec.either(codec, ResourceKey.codec(resourcekey).comapFlatMap((resourcekey1) -> {
            return DataResult.error(() -> {
                return "Cannot parse as key without registry";
            });
        }, Function.identity())).xmap(EitherHolder::new, EitherHolder::contents);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(ResourceKey<IRegistry<T>> resourcekey, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> streamcodec) {
        return StreamCodec.composite(ByteBufCodecs.either(streamcodec, ResourceKey.streamCodec(resourcekey)), EitherHolder::contents, EitherHolder::new);
    }

    public Optional<T> unwrap(IRegistry<T> iregistry) {
        Either either = this.contents;
        Function function = (holder) -> {
            return Optional.of(holder.value());
        };

        Objects.requireNonNull(iregistry);
        return (Optional) either.map(function, iregistry::getOptional);
    }

    public Optional<Holder<T>> unwrap(HolderLookup.a holderlookup_a) {
        return (Optional) this.contents.map(Optional::of, (resourcekey) -> {
            return holderlookup_a.get(resourcekey).map((holder_c) -> {
                return holder_c;
            });
        });
    }

    public Optional<ResourceKey<T>> key() {
        return (Optional) this.contents.map(Holder::unwrapKey, Optional::of);
    }
}
