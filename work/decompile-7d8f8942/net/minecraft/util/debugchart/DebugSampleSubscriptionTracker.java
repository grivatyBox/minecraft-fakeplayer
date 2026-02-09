package net.minecraft.util.debugchart;

import com.google.common.collect.Maps;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import net.minecraft.SystemUtils;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.players.PlayerList;

public class DebugSampleSubscriptionTracker {

    public static final int STOP_SENDING_AFTER_TICKS = 200;
    public static final int STOP_SENDING_AFTER_MS = 10000;
    private final PlayerList playerList;
    private final Map<RemoteDebugSampleType, Map<EntityPlayer, DebugSampleSubscriptionTracker.b>> subscriptions;
    private final Queue<DebugSampleSubscriptionTracker.a> subscriptionRequestQueue = new LinkedList();

    public DebugSampleSubscriptionTracker(PlayerList playerlist) {
        this.playerList = playerlist;
        this.subscriptions = SystemUtils.<RemoteDebugSampleType, Map<EntityPlayer, DebugSampleSubscriptionTracker.b>>makeEnumMap(RemoteDebugSampleType.class, (remotedebugsampletype) -> {
            return Maps.newHashMap();
        });
    }

    public boolean shouldLogSamples(RemoteDebugSampleType remotedebugsampletype) {
        return !((Map) this.subscriptions.get(remotedebugsampletype)).isEmpty();
    }

    public void broadcast(ClientboundDebugSamplePacket clientbounddebugsamplepacket) {
        for (EntityPlayer entityplayer : ((Map) this.subscriptions.get(clientbounddebugsamplepacket.debugSampleType())).keySet()) {
            entityplayer.connection.send(clientbounddebugsamplepacket);
        }

    }

    public void subscribe(EntityPlayer entityplayer, RemoteDebugSampleType remotedebugsampletype) {
        if (this.playerList.isOp(entityplayer.getGameProfile())) {
            this.subscriptionRequestQueue.add(new DebugSampleSubscriptionTracker.a(entityplayer, remotedebugsampletype));
        }

    }

    public void tick(int i) {
        long j = SystemUtils.getMillis();

        this.handleSubscriptions(j, i);
        this.handleUnsubscriptions(j, i);
    }

    private void handleSubscriptions(long i, int j) {
        for (DebugSampleSubscriptionTracker.a debugsamplesubscriptiontracker_a : this.subscriptionRequestQueue) {
            ((Map) this.subscriptions.get(debugsamplesubscriptiontracker_a.sampleType())).put(debugsamplesubscriptiontracker_a.player(), new DebugSampleSubscriptionTracker.b(i, j));
        }

    }

    private void handleUnsubscriptions(long i, int j) {
        for (Map<EntityPlayer, DebugSampleSubscriptionTracker.b> map : this.subscriptions.values()) {
            map.entrySet().removeIf((entry) -> {
                boolean flag = !this.playerList.isOp(((EntityPlayer) entry.getKey()).getGameProfile());
                DebugSampleSubscriptionTracker.b debugsamplesubscriptiontracker_b = (DebugSampleSubscriptionTracker.b) entry.getValue();

                return flag || j > debugsamplesubscriptiontracker_b.tick() + 200 && i > debugsamplesubscriptiontracker_b.millis() + 10000L;
            });
        }

    }

    private static record a(EntityPlayer player, RemoteDebugSampleType sampleType) {

    }

    private static record b(long millis, int tick) {

    }
}
