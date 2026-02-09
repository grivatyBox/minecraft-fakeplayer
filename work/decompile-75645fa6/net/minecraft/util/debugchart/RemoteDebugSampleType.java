package net.minecraft.util.debugchart;

import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;

public enum RemoteDebugSampleType {

    TICK_TIME(DebugSubscriptions.DEDICATED_SERVER_TICK_TIME);

    private final DebugSubscription<?> subscription;

    private RemoteDebugSampleType(final DebugSubscription debugsubscription) {
        this.subscription = debugsubscription;
    }

    public DebugSubscription<?> subscription() {
        return this.subscription;
    }
}
