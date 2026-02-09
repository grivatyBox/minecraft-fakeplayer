package net.minecraft.world.level.timers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.MinecraftServer;

public record CustomFunctionCallback(MinecraftKey functionId) implements CustomFunctionCallbackTimer<MinecraftServer> {

    public static final MapCodec<CustomFunctionCallback> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("Name").forGetter(CustomFunctionCallback::functionId)).apply(instance, CustomFunctionCallback::new);
    });

    public void handle(MinecraftServer minecraftserver, CustomFunctionCallbackTimerQueue<MinecraftServer> customfunctioncallbacktimerqueue, long i) {
        CustomFunctionData customfunctiondata = minecraftserver.getFunctions();

        customfunctiondata.get(this.functionId).ifPresent((commandfunction) -> {
            customfunctiondata.execute(commandfunction, customfunctiondata.getGameLoopSender());
        });
    }

    @Override
    public MapCodec<CustomFunctionCallback> codec() {
        return CustomFunctionCallback.CODEC;
    }
}
