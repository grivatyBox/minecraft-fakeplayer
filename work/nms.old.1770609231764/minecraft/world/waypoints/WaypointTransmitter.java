package net.minecraft.world.waypoints;

import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.phys.Vec3D;

public interface WaypointTransmitter extends Waypoint {

    int REALLY_FAR_DISTANCE = 332;

    boolean isTransmittingWaypoint();

    Optional<WaypointTransmitter.c> makeWaypointConnectionWith(EntityPlayer entityplayer);

    Waypoint.a waypointIcon();

    static boolean doesSourceIgnoreReceiver(EntityLiving entityliving, EntityPlayer entityplayer) {
        // CraftBukkit start
        if (!entityplayer.getBukkitEntity().canSee(entityliving.getBukkitEntity())) {
            return true;
        }
        // CraftBukkit end
        if (entityplayer.isSpectator()) {
            return false;
        } else if (!entityliving.isSpectator() && !entityliving.hasIndirectPassenger(entityplayer)) {
            double d0 = Math.min(entityliving.getAttributeValue(GenericAttributes.WAYPOINT_TRANSMIT_RANGE), entityplayer.getAttributeValue(GenericAttributes.WAYPOINT_RECEIVE_RANGE));

            return (double) entityliving.distanceTo(entityplayer) >= d0;
        } else {
            return true;
        }
    }

    static boolean isChunkVisible(ChunkCoordIntPair chunkcoordintpair, EntityPlayer entityplayer) {
        return entityplayer.getChunkTrackingView().isInViewDistance(chunkcoordintpair.x, chunkcoordintpair.z);
    }

    static boolean isReallyFar(EntityLiving entityliving, EntityPlayer entityplayer) {
        return entityliving.distanceTo(entityplayer) > 332.0F;
    }

    public interface a extends WaypointTransmitter.c {

        int distanceManhattan();

        @Override
        default boolean isBroken() {
            return this.distanceManhattan() > 1;
        }
    }

    public static class e implements WaypointTransmitter.a {

        private final EntityLiving source;
        private final Waypoint.a icon;
        private final EntityPlayer receiver;
        private BlockPosition lastPosition;

        public e(EntityLiving entityliving, Waypoint.a waypoint_a, EntityPlayer entityplayer) {
            this.source = entityliving;
            this.receiver = entityplayer;
            this.icon = waypoint_a;
            this.lastPosition = entityliving.blockPosition();
        }

        @Override
        public void connect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(this.source.getUUID(), this.icon, this.lastPosition));
        }

        @Override
        public void disconnect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
        }

        @Override
        public void update() {
            BlockPosition blockposition = this.source.blockPosition();

            if (blockposition.distManhattan(this.lastPosition) > 0) {
                this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointPosition(this.source.getUUID(), this.icon, blockposition));
                this.lastPosition = blockposition;
            }

        }

        @Override
        public int distanceManhattan() {
            return this.lastPosition.distManhattan(this.source.blockPosition());
        }

        @Override
        public boolean isBroken() {
            return WaypointTransmitter.a.super.isBroken() || WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver);
        }
    }

    public interface b extends WaypointTransmitter.c {

        int distanceChessboard();

        @Override
        default boolean isBroken() {
            return this.distanceChessboard() > 1;
        }
    }

    public static class f implements WaypointTransmitter.b {

        private final EntityLiving source;
        private final Waypoint.a icon;
        private final EntityPlayer receiver;
        private ChunkCoordIntPair lastPosition;

        public f(EntityLiving entityliving, Waypoint.a waypoint_a, EntityPlayer entityplayer) {
            this.source = entityliving;
            this.icon = waypoint_a;
            this.receiver = entityplayer;
            this.lastPosition = entityliving.chunkPosition();
        }

        @Override
        public int distanceChessboard() {
            return this.lastPosition.getChessboardDistance(this.source.chunkPosition());
        }

        @Override
        public void connect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointChunk(this.source.getUUID(), this.icon, this.lastPosition));
        }

        @Override
        public void disconnect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
        }

        @Override
        public void update() {
            ChunkCoordIntPair chunkcoordintpair = this.source.chunkPosition();

            if (chunkcoordintpair.getChessboardDistance(this.lastPosition) > 0) {
                this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointChunk(this.source.getUUID(), this.icon, chunkcoordintpair));
                this.lastPosition = chunkcoordintpair;
            }

        }

        @Override
        public boolean isBroken() {
            return !WaypointTransmitter.b.super.isBroken() && !WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver) ? WaypointTransmitter.isChunkVisible(this.lastPosition, this.receiver) : true;
        }
    }

    public static class d implements WaypointTransmitter.c {

        private final EntityLiving source;
        private final Waypoint.a icon;
        private final EntityPlayer receiver;
        private float lastAngle;

        public d(EntityLiving entityliving, Waypoint.a waypoint_a, EntityPlayer entityplayer) {
            this.source = entityliving;
            this.icon = waypoint_a;
            this.receiver = entityplayer;
            Vec3D vec3d = entityplayer.position().subtract(entityliving.position()).rotateClockwise90();

            this.lastAngle = (float) MathHelper.atan2(vec3d.z(), vec3d.x());
        }

        @Override
        public boolean isBroken() {
            return WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver) || WaypointTransmitter.isChunkVisible(this.source.chunkPosition(), this.receiver) || !WaypointTransmitter.isReallyFar(this.source, this.receiver);
        }

        @Override
        public void connect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointAzimuth(this.source.getUUID(), this.icon, this.lastAngle));
        }

        @Override
        public void disconnect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
        }

        @Override
        public void update() {
            Vec3D vec3d = this.receiver.position().subtract(this.source.position()).rotateClockwise90();
            float f = (float) MathHelper.atan2(vec3d.z(), vec3d.x());

            if (MathHelper.abs(f - this.lastAngle) > 0.008726646F) {
                this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointAzimuth(this.source.getUUID(), this.icon, f));
                this.lastAngle = f;
            }

        }
    }

    public interface c {

        void connect();

        void disconnect();

        void update();

        boolean isBroken();
    }
}
