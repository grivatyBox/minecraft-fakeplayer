package net.minecraft.world.level.timers;

import com.mojang.serialization.MapCodec;

public interface CustomFunctionCallbackTimer<T> {

    void handle(T t0, CustomFunctionCallbackTimerQueue<T> customfunctioncallbacktimerqueue, long i);

    MapCodec<? extends CustomFunctionCallbackTimer<T>> codec();
}
