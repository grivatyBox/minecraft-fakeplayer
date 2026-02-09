package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.TrackedWaypointManager;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointManager;

public record ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a operation, TrackedWaypoint waypoint) implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTrackedWaypointPacket> STREAM_CODEC = StreamCodec.composite(ClientboundTrackedWaypointPacket.a.STREAM_CODEC, ClientboundTrackedWaypointPacket::operation, TrackedWaypoint.STREAM_CODEC, ClientboundTrackedWaypointPacket::waypoint, ClientboundTrackedWaypointPacket::new);

    public static ClientboundTrackedWaypointPacket removeWaypoint(UUID uuid) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.UNTRACK, TrackedWaypoint.empty(uuid));
    }

    public static ClientboundTrackedWaypointPacket addWaypointPosition(UUID uuid, Waypoint.a waypoint_a, BaseBlockPosition baseblockposition) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.TRACK, TrackedWaypoint.setPosition(uuid, waypoint_a, baseblockposition));
    }

    public static ClientboundTrackedWaypointPacket updateWaypointPosition(UUID uuid, Waypoint.a waypoint_a, BaseBlockPosition baseblockposition) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.UPDATE, TrackedWaypoint.setPosition(uuid, waypoint_a, baseblockposition));
    }

    public static ClientboundTrackedWaypointPacket addWaypointChunk(UUID uuid, Waypoint.a waypoint_a, ChunkCoordIntPair chunkcoordintpair) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.TRACK, TrackedWaypoint.setChunk(uuid, waypoint_a, chunkcoordintpair));
    }

    public static ClientboundTrackedWaypointPacket updateWaypointChunk(UUID uuid, Waypoint.a waypoint_a, ChunkCoordIntPair chunkcoordintpair) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.UPDATE, TrackedWaypoint.setChunk(uuid, waypoint_a, chunkcoordintpair));
    }

    public static ClientboundTrackedWaypointPacket addWaypointAzimuth(UUID uuid, Waypoint.a waypoint_a, float f) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.TRACK, TrackedWaypoint.setAzimuth(uuid, waypoint_a, f));
    }

    public static ClientboundTrackedWaypointPacket updateWaypointAzimuth(UUID uuid, Waypoint.a waypoint_a, float f) {
        return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.a.UPDATE, TrackedWaypoint.setAzimuth(uuid, waypoint_a, f));
    }

    @Override
    public PacketType<ClientboundTrackedWaypointPacket> type() {
        return GamePacketTypes.CLIENTBOUND_WAYPOINT;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleWaypoint(this);
    }

    public void apply(TrackedWaypointManager trackedwaypointmanager) {
        this.operation.action.accept(trackedwaypointmanager, this.waypoint);
    }

    private static enum a {

        TRACK(WaypointManager::trackWaypoint), UNTRACK(WaypointManager::untrackWaypoint), UPDATE(WaypointManager::updateWaypoint);

        final BiConsumer<TrackedWaypointManager, TrackedWaypoint> action;
        public static final IntFunction<ClientboundTrackedWaypointPacket.a> BY_ID = ByIdMap.<ClientboundTrackedWaypointPacket.a>continuous(Enum::ordinal, values(), ByIdMap.a.WRAP);
        public static final StreamCodec<ByteBuf, ClientboundTrackedWaypointPacket.a> STREAM_CODEC = ByteBufCodecs.idMapper(ClientboundTrackedWaypointPacket.a.BY_ID, Enum::ordinal);

        private a(final BiConsumer biconsumer) {
            this.action = biconsumer;
        }
    }
}
