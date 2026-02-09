package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;

public class PacketPlayOutEntityVelocity implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutEntityVelocity> STREAM_CODEC = Packet.<PacketDataSerializer, PacketPlayOutEntityVelocity>codec(PacketPlayOutEntityVelocity::write, PacketPlayOutEntityVelocity::new);
    private final int id;
    private final Vec3D movement;

    public PacketPlayOutEntityVelocity(Entity entity) {
        this(entity.getId(), entity.getDeltaMovement());
    }

    public PacketPlayOutEntityVelocity(int i, Vec3D vec3d) {
        this.id = i;
        this.movement = vec3d;
    }

    private PacketPlayOutEntityVelocity(PacketDataSerializer packetdataserializer) {
        this.id = packetdataserializer.readVarInt();
        this.movement = packetdataserializer.readLpVec3();
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.id);
        packetdataserializer.writeLpVec3(this.movement);
    }

    @Override
    public PacketType<PacketPlayOutEntityVelocity> type() {
        return GamePacketTypes.CLIENTBOUND_SET_ENTITY_MOTION;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleSetEntityMotion(this);
    }

    public int getId() {
        return this.id;
    }

    public Vec3D getMovement() {
        return this.movement;
    }
}
