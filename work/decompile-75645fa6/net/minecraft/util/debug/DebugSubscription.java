package net.minecraft.util.debug;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class DebugSubscription<T> {

    public static final int DOES_NOT_EXPIRE = 0;
    @Nullable
    final StreamCodec<? super RegistryFriendlyByteBuf, T> valueStreamCodec;
    private final int expireAfterTicks;

    public DebugSubscription(@Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec, int i) {
        this.valueStreamCodec = streamcodec;
        this.expireAfterTicks = i;
    }

    public DebugSubscription(@Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamcodec) {
        this(streamcodec, 0);
    }

    public DebugSubscription.b<T> packUpdate(@Nullable T t0) {
        return new DebugSubscription.b<T>(this, Optional.ofNullable(t0));
    }

    public DebugSubscription.b<T> emptyUpdate() {
        return new DebugSubscription.b<T>(this, Optional.empty());
    }

    public DebugSubscription.a<T> packEvent(T t0) {
        return new DebugSubscription.a<T>(this, t0);
    }

    public String toString() {
        return SystemUtils.getRegisteredName(BuiltInRegistries.DEBUG_SUBSCRIPTION, this);
    }

    @Nullable
    public StreamCodec<? super RegistryFriendlyByteBuf, T> valueStreamCodec() {
        return this.valueStreamCodec;
    }

    public int expireAfterTicks() {
        return this.expireAfterTicks;
    }

    public static record b<T>(DebugSubscription<T> subscription, Optional<T> value) {

        public static final StreamCodec<RegistryFriendlyByteBuf, DebugSubscription.b<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION).dispatch(DebugSubscription.b::subscription, DebugSubscription.b::streamCodec);

        private static <T> StreamCodec<? super RegistryFriendlyByteBuf, DebugSubscription.b<T>> streamCodec(DebugSubscription<T> debugsubscription) {
            return ByteBufCodecs.optional((StreamCodec) Objects.requireNonNull(debugsubscription.valueStreamCodec)).map((optional) -> {
                return new DebugSubscription.b(debugsubscription, optional);
            }, DebugSubscription.b::value);
        }
    }

    public static record a<T>(DebugSubscription<T> subscription, T value) {

        public static final StreamCodec<RegistryFriendlyByteBuf, DebugSubscription.a<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION).dispatch(DebugSubscription.a::subscription, DebugSubscription.a::streamCodec);

        private static <T> StreamCodec<? super RegistryFriendlyByteBuf, DebugSubscription.a<T>> streamCodec(DebugSubscription<T> debugsubscription) {
            return ((StreamCodec) Objects.requireNonNull(debugsubscription.valueStreamCodec)).map((object) -> {
                return new DebugSubscription.a(debugsubscription, object);
            }, DebugSubscription.a::value);
        }
    }
}
