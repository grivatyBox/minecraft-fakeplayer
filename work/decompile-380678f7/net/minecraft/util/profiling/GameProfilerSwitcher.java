package net.minecraft.util.profiling;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public class GameProfilerSwitcher {

    private final LongSupplier realTime;
    private final IntSupplier tickCount;
    private final BooleanSupplier suppressWarnings;
    private GameProfilerFillerActive profiler;

    public GameProfilerSwitcher(LongSupplier longsupplier, IntSupplier intsupplier, BooleanSupplier booleansupplier) {
        this.profiler = GameProfilerDisabled.INSTANCE;
        this.realTime = longsupplier;
        this.tickCount = intsupplier;
        this.suppressWarnings = booleansupplier;
    }

    public boolean isEnabled() {
        return this.profiler != GameProfilerDisabled.INSTANCE;
    }

    public void disable() {
        this.profiler = GameProfilerDisabled.INSTANCE;
    }

    public void enable() {
        this.profiler = new MethodProfiler(this.realTime, this.tickCount, this.suppressWarnings);
    }

    public GameProfilerFiller getFiller() {
        return this.profiler;
    }

    public MethodProfilerResults getResults() {
        return this.profiler.getResults();
    }
}
