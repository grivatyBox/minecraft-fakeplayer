package net.minecraft.world.waypoints;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

public abstract class TrackedWaypoint implements Waypoint {

    static final Logger LOGGER = LogUtils.getLogger();
    public static final StreamCodec<ByteBuf, TrackedWaypoint> STREAM_CODEC = StreamCodec.<ByteBuf, TrackedWaypoint>ofMember(TrackedWaypoint::write, TrackedWaypoint::read);
    protected final Either<UUID, String> identifier;
    private final Waypoint.a icon;
    private final TrackedWaypoint.g type;

    TrackedWaypoint(Either<UUID, String> either, Waypoint.a waypoint_a, TrackedWaypoint.g trackedwaypoint_g) {
        this.identifier = either;
        this.icon = waypoint_a;
        this.type = trackedwaypoint_g;
    }

    public Either<UUID, String> id() {
        return this.identifier;
    }

    public abstract void update(TrackedWaypoint trackedwaypoint);

    public void write(ByteBuf bytebuf) {
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf);

        packetdataserializer.writeEither(this.identifier, UUIDUtil.STREAM_CODEC, PacketDataSerializer::writeUtf);
        Waypoint.a.STREAM_CODEC.encode(packetdataserializer, this.icon);
        packetdataserializer.writeEnum(this.type);
        this.writeContents(bytebuf);
    }

    public abstract void writeContents(ByteBuf bytebuf);

    private static TrackedWaypoint read(ByteBuf bytebuf) {
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(bytebuf);
        Either<UUID, String> either = packetdataserializer.<UUID, String>readEither(UUIDUtil.STREAM_CODEC, PacketDataSerializer::readUtf);
        Waypoint.a waypoint_a = (Waypoint.a) Waypoint.a.STREAM_CODEC.decode(packetdataserializer);
        TrackedWaypoint.g trackedwaypoint_g = (TrackedWaypoint.g) packetdataserializer.readEnum(TrackedWaypoint.g.class);

        return (TrackedWaypoint) trackedwaypoint_g.constructor.apply(either, waypoint_a, packetdataserializer);
    }

    public static TrackedWaypoint setPosition(UUID uuid, Waypoint.a waypoint_a, BaseBlockPosition baseblockposition) {
        return new TrackedWaypoint.h(uuid, waypoint_a, baseblockposition);
    }

    public static TrackedWaypoint setChunk(UUID uuid, Waypoint.a waypoint_a, ChunkCoordIntPair chunkcoordintpair) {
        return new TrackedWaypoint.c(uuid, waypoint_a, chunkcoordintpair);
    }

    public static TrackedWaypoint setAzimuth(UUID uuid, Waypoint.a waypoint_a, float f) {
        return new TrackedWaypoint.a(uuid, waypoint_a, f);
    }

    public static TrackedWaypoint empty(UUID uuid) {
        return new TrackedWaypoint.d(uuid);
    }

    public abstract double yawAngleToCamera(World world, TrackedWaypoint.b trackedwaypoint_b, PartialTickSupplier partialticksupplier);

    public abstract TrackedWaypoint.e pitchDirectionToCamera(World world, TrackedWaypoint.f trackedwaypoint_f, PartialTickSupplier partialticksupplier);

    public abstract double distanceSquared(Entity entity);

    public Waypoint.a icon() {
        return this.icon;
    }

    public static enum e {

        NONE, UP, DOWN;

        private e() {}
    }

    private static enum g {

        EMPTY(TrackedWaypoint.d::new), VEC3I(TrackedWaypoint.h::new), CHUNK(TrackedWaypoint.c::new), AZIMUTH(TrackedWaypoint.a::new);

        final TriFunction<Either<UUID, String>, Waypoint.a, PacketDataSerializer, TrackedWaypoint> constructor;

        private g(final TriFunction trifunction) {
            this.constructor = trifunction;
        }
    }

    private static class d extends TrackedWaypoint {

        private d(Either<UUID, String> either, Waypoint.a waypoint_a, PacketDataSerializer packetdataserializer) {
            super(either, waypoint_a, TrackedWaypoint.g.EMPTY);
        }

        d(UUID uuid) {
            super(Either.left(uuid), Waypoint.a.NULL, TrackedWaypoint.g.EMPTY);
        }

        @Override
        public void update(TrackedWaypoint trackedwaypoint) {}

        @Override
        public void writeContents(ByteBuf bytebuf) {}

        @Override
        public double yawAngleToCamera(World world, TrackedWaypoint.b trackedwaypoint_b, PartialTickSupplier partialticksupplier) {
            return Double.NaN;
        }

        @Override
        public TrackedWaypoint.e pitchDirectionToCamera(World world, TrackedWaypoint.f trackedwaypoint_f, PartialTickSupplier partialticksupplier) {
            return TrackedWaypoint.e.NONE;
        }

        @Override
        public double distanceSquared(Entity entity) {
            return Double.POSITIVE_INFINITY;
        }
    }

    private static class h extends TrackedWaypoint {

        private BaseBlockPosition vector;

        public h(UUID uuid, Waypoint.a waypoint_a, BaseBlockPosition baseblockposition) {
            super(Either.left(uuid), waypoint_a, TrackedWaypoint.g.VEC3I);
            this.vector = baseblockposition;
        }

        public h(Either<UUID, String> either, Waypoint.a waypoint_a, PacketDataSerializer packetdataserializer) {
            super(either, waypoint_a, TrackedWaypoint.g.VEC3I);
            this.vector = new BaseBlockPosition(packetdataserializer.readVarInt(), packetdataserializer.readVarInt(), packetdataserializer.readVarInt());
        }

        @Override
        public void update(TrackedWaypoint trackedwaypoint) {
            if (trackedwaypoint instanceof TrackedWaypoint.h trackedwaypoint_h) {
                this.vector = trackedwaypoint_h.vector;
            } else {
                TrackedWaypoint.LOGGER.warn("Unsupported Waypoint update operation: {}", trackedwaypoint.getClass());
            }

        }

        @Override
        public void writeContents(ByteBuf bytebuf) {
            VarInt.write(bytebuf, this.vector.getX());
            VarInt.write(bytebuf, this.vector.getY());
            VarInt.write(bytebuf, this.vector.getZ());
        }

        private Vec3D position(World world, PartialTickSupplier partialticksupplier) {
            Optional optional = this.identifier.left();

            Objects.requireNonNull(world);
            return (Vec3D) optional.map(world::getEntity).map((entity) -> {
                return entity.blockPosition().distManhattan(this.vector) > 3 ? null : entity.getEyePosition(partialticksupplier.apply(entity));
            }).orElseGet(() -> {
                return Vec3D.atCenterOf(this.vector);
            });
        }

        @Override
        public double yawAngleToCamera(World world, TrackedWaypoint.b trackedwaypoint_b, PartialTickSupplier partialticksupplier) {
            Vec3D vec3d = trackedwaypoint_b.position().subtract(this.position(world, partialticksupplier)).rotateClockwise90();
            float f = (float) MathHelper.atan2(vec3d.z(), vec3d.x()) * (180F / (float) Math.PI);

            return (double) MathHelper.degreesDifference(trackedwaypoint_b.yaw(), f);
        }

        @Override
        public TrackedWaypoint.e pitchDirectionToCamera(World world, TrackedWaypoint.f trackedwaypoint_f, PartialTickSupplier partialticksupplier) {
            Vec3D vec3d = trackedwaypoint_f.projectPointToScreen(this.position(world, partialticksupplier));
            boolean flag = vec3d.z > 1.0D;
            double d0 = flag ? -vec3d.y : vec3d.y;

            if (d0 < -1.0D) {
                return TrackedWaypoint.e.DOWN;
            } else if (d0 > 1.0D) {
                return TrackedWaypoint.e.UP;
            } else {
                if (flag) {
                    if (vec3d.y > 0.0D) {
                        return TrackedWaypoint.e.UP;
                    }

                    if (vec3d.y < 0.0D) {
                        return TrackedWaypoint.e.DOWN;
                    }
                }

                return TrackedWaypoint.e.NONE;
            }
        }

        @Override
        public double distanceSquared(Entity entity) {
            return entity.distanceToSqr(Vec3D.atCenterOf(this.vector));
        }
    }

    private static class c extends TrackedWaypoint {

        private ChunkCoordIntPair chunkPos;

        public c(UUID uuid, Waypoint.a waypoint_a, ChunkCoordIntPair chunkcoordintpair) {
            super(Either.left(uuid), waypoint_a, TrackedWaypoint.g.CHUNK);
            this.chunkPos = chunkcoordintpair;
        }

        public c(Either<UUID, String> either, Waypoint.a waypoint_a, PacketDataSerializer packetdataserializer) {
            super(either, waypoint_a, TrackedWaypoint.g.CHUNK);
            this.chunkPos = new ChunkCoordIntPair(packetdataserializer.readVarInt(), packetdataserializer.readVarInt());
        }

        @Override
        public void update(TrackedWaypoint trackedwaypoint) {
            if (trackedwaypoint instanceof TrackedWaypoint.c trackedwaypoint_c) {
                this.chunkPos = trackedwaypoint_c.chunkPos;
            } else {
                TrackedWaypoint.LOGGER.warn("Unsupported Waypoint update operation: {}", trackedwaypoint.getClass());
            }

        }

        @Override
        public void writeContents(ByteBuf bytebuf) {
            VarInt.write(bytebuf, this.chunkPos.x);
            VarInt.write(bytebuf, this.chunkPos.z);
        }

        private Vec3D position(double d0) {
            return Vec3D.atCenterOf(this.chunkPos.getMiddleBlockPosition((int) d0));
        }

        @Override
        public double yawAngleToCamera(World world, TrackedWaypoint.b trackedwaypoint_b, PartialTickSupplier partialticksupplier) {
            Vec3D vec3d = trackedwaypoint_b.position();
            Vec3D vec3d1 = vec3d.subtract(this.position(vec3d.y())).rotateClockwise90();
            float f = (float) MathHelper.atan2(vec3d1.z(), vec3d1.x()) * (180F / (float) Math.PI);

            return (double) MathHelper.degreesDifference(trackedwaypoint_b.yaw(), f);
        }

        @Override
        public TrackedWaypoint.e pitchDirectionToCamera(World world, TrackedWaypoint.f trackedwaypoint_f, PartialTickSupplier partialticksupplier) {
            double d0 = trackedwaypoint_f.projectHorizonToScreen();

            return d0 < -1.0D ? TrackedWaypoint.e.DOWN : (d0 > 1.0D ? TrackedWaypoint.e.UP : TrackedWaypoint.e.NONE);
        }

        @Override
        public double distanceSquared(Entity entity) {
            return entity.distanceToSqr(Vec3D.atCenterOf(this.chunkPos.getMiddleBlockPosition(entity.getBlockY())));
        }
    }

    private static class a extends TrackedWaypoint {

        private float angle;

        public a(UUID uuid, Waypoint.a waypoint_a, float f) {
            super(Either.left(uuid), waypoint_a, TrackedWaypoint.g.AZIMUTH);
            this.angle = f;
        }

        public a(Either<UUID, String> either, Waypoint.a waypoint_a, PacketDataSerializer packetdataserializer) {
            super(either, waypoint_a, TrackedWaypoint.g.AZIMUTH);
            this.angle = packetdataserializer.readFloat();
        }

        @Override
        public void update(TrackedWaypoint trackedwaypoint) {
            if (trackedwaypoint instanceof TrackedWaypoint.a trackedwaypoint_a) {
                this.angle = trackedwaypoint_a.angle;
            } else {
                TrackedWaypoint.LOGGER.warn("Unsupported Waypoint update operation: {}", trackedwaypoint.getClass());
            }

        }

        @Override
        public void writeContents(ByteBuf bytebuf) {
            bytebuf.writeFloat(this.angle);
        }

        @Override
        public double yawAngleToCamera(World world, TrackedWaypoint.b trackedwaypoint_b, PartialTickSupplier partialticksupplier) {
            return (double) MathHelper.degreesDifference(trackedwaypoint_b.yaw(), this.angle * (180F / (float) Math.PI));
        }

        @Override
        public TrackedWaypoint.e pitchDirectionToCamera(World world, TrackedWaypoint.f trackedwaypoint_f, PartialTickSupplier partialticksupplier) {
            double d0 = trackedwaypoint_f.projectHorizonToScreen();

            return d0 < -1.0D ? TrackedWaypoint.e.DOWN : (d0 > 1.0D ? TrackedWaypoint.e.UP : TrackedWaypoint.e.NONE);
        }

        @Override
        public double distanceSquared(Entity entity) {
            return Double.POSITIVE_INFINITY;
        }
    }

    public interface b {

        float yaw();

        Vec3D position();
    }

    public interface f {

        Vec3D projectPointToScreen(Vec3D vec3d);

        double projectHorizonToScreen();
    }
}
