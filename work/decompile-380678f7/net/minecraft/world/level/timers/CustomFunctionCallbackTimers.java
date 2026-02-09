package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;

public class CustomFunctionCallbackTimers<C> {

    public static final CustomFunctionCallbackTimers<MinecraftServer> SERVER_CALLBACKS = (new CustomFunctionCallbackTimers<MinecraftServer>()).register(MinecraftKey.withDefaultNamespace("function"), CustomFunctionCallback.CODEC).register(MinecraftKey.withDefaultNamespace("function_tag"), CustomFunctionCallbackTag.CODEC);
    private final ExtraCodecs.b<MinecraftKey, MapCodec<? extends CustomFunctionCallbackTimer<C>>> idMapper = new ExtraCodecs.b<MinecraftKey, MapCodec<? extends CustomFunctionCallbackTimer<C>>>();
    private final Codec<CustomFunctionCallbackTimer<C>> codec;

    @VisibleForTesting
    public CustomFunctionCallbackTimers() {
        this.codec = this.idMapper.codec(MinecraftKey.CODEC).dispatch("Type", CustomFunctionCallbackTimer::codec, Function.identity());
    }

    public CustomFunctionCallbackTimers<C> register(MinecraftKey minecraftkey, MapCodec<? extends CustomFunctionCallbackTimer<C>> mapcodec) {
        this.idMapper.put(minecraftkey, mapcodec);
        return this;
    }

    public Codec<CustomFunctionCallbackTimer<C>> codec() {
        return this.codec;
    }
}
