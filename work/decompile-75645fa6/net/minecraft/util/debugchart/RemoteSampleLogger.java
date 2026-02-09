package net.minecraft.util.debugchart;

import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.util.debug.ServerDebugSubscribers;

public class RemoteSampleLogger extends AbstractSampleLogger {

    private final ServerDebugSubscribers subscribers;
    private final RemoteDebugSampleType sampleType;

    public RemoteSampleLogger(int i, ServerDebugSubscribers serverdebugsubscribers, RemoteDebugSampleType remotedebugsampletype) {
        this(i, serverdebugsubscribers, remotedebugsampletype, new long[i]);
    }

    public RemoteSampleLogger(int i, ServerDebugSubscribers serverdebugsubscribers, RemoteDebugSampleType remotedebugsampletype, long[] along) {
        super(i, along);
        this.subscribers = serverdebugsubscribers;
        this.sampleType = remotedebugsampletype;
    }

    @Override
    protected void useSample() {
        if (this.subscribers.hasAnySubscriberFor(this.sampleType.subscription())) {
            this.subscribers.broadcastToAll(this.sampleType.subscription(), new ClientboundDebugSamplePacket((long[]) this.sample.clone(), this.sampleType));
        }

    }
}
