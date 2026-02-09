package net.minecraft.world.level.timers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.CustomFunctionData;
import net.minecraft.server.MinecraftServer;

public record CustomFunctionCallbackTag(MinecraftKey tagId) implements CustomFunctionCallbackTimer<MinecraftServer> {

    public static final MapCodec<CustomFunctionCallbackTag> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("Name").forGetter(CustomFunctionCallbackTag::tagId)).apply(instance, CustomFunctionCallbackTag::new);
    });

    public void handle(MinecraftServer minecraftserver, CustomFunctionCallbackTimerQueue<MinecraftServer> customfunctioncallbacktimerqueue, long i) {
        CustomFunctionData customfunctiondata = minecraftserver.getFunctions();

        for (CommandFunction<CommandListenerWrapper> commandfunction : customfunctiondata.getTag(this.tagId)) {
            customfunctiondata.execute(commandfunction, customfunctiondata.getGameLoopSender());
        }

    }

    @Override
    public MapCodec<CustomFunctionCallbackTag> codec() {
        return CustomFunctionCallbackTag.CODEC;
    }
}
