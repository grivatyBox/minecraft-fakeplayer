package net.minecraft.util.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.players.NameAndId;

public class ServerDebugSubscribers {

    private final MinecraftServer server;
    private final Map<DebugSubscription<?>, List<EntityPlayer>> enabledSubscriptions = new HashMap();

    public ServerDebugSubscribers(MinecraftServer minecraftserver) {
        this.server = minecraftserver;
    }

    private List<EntityPlayer> getSubscribersFor(DebugSubscription<?> debugsubscription) {
        return (List) this.enabledSubscriptions.getOrDefault(debugsubscription, List.of());
    }

    public void tick() {
        this.enabledSubscriptions.values().forEach(List::clear);

        for (EntityPlayer entityplayer : this.server.getPlayerList().getPlayers()) {
            for (DebugSubscription<?> debugsubscription : entityplayer.debugSubscriptions()) {
                ((List) this.enabledSubscriptions.computeIfAbsent(debugsubscription, (debugsubscription1) -> {
                    return new ArrayList();
                })).add(entityplayer);
            }
        }

        this.enabledSubscriptions.values().removeIf(List::isEmpty);
    }

    public void broadcastToAll(DebugSubscription<?> debugsubscription, Packet<?> packet) {
        for (EntityPlayer entityplayer : this.getSubscribersFor(debugsubscription)) {
            entityplayer.connection.send(packet);
        }

    }

    public Set<DebugSubscription<?>> enabledSubscriptions() {
        return Set.copyOf(this.enabledSubscriptions.keySet());
    }

    public boolean hasAnySubscriberFor(DebugSubscription<?> debugsubscription) {
        return !this.getSubscribersFor(debugsubscription).isEmpty();
    }

    public boolean hasRequiredPermissions(EntityPlayer entityplayer) {
        NameAndId nameandid = entityplayer.nameAndId();

        return SharedConstants.IS_RUNNING_IN_IDE && this.server.isSingleplayerOwner(nameandid) ? true : this.server.getPlayerList().isOp(nameandid);
    }
}
