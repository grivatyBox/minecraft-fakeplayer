package net.minecraft.network.protocol.common.custom;

import java.util.UUID;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.MinecraftKey;

public record BreezeDebugPayload(BreezeDebugPayload.a breezeInfo) implements CustomPacketPayload {

    public static final MinecraftKey ID = new MinecraftKey("debug/breeze");

    public BreezeDebugPayload(PacketDataSerializer packetdataserializer) {
        this(new BreezeDebugPayload.a(packetdataserializer));
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        this.breezeInfo.write(packetdataserializer);
    }

    @Override
    public MinecraftKey id() {
        return BreezeDebugPayload.ID;
    }

    public static record a(UUID uuid, int id, Integer attackTarget, BlockPosition jumpTarget) {

        public a(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readUUID(), packetdataserializer.readInt(), (Integer) packetdataserializer.readNullable(PacketDataSerializer::readInt), (BlockPosition) packetdataserializer.readNullable(PacketDataSerializer::readBlockPos));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUUID(this.uuid);
            packetdataserializer.writeInt(this.id);
            packetdataserializer.writeNullable(this.attackTarget, PacketDataSerializer::writeInt);
            packetdataserializer.writeNullable(this.jumpTarget, PacketDataSerializer::writeBlockPos);
        }

        public String generateName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        public String toString() {
            return this.generateName();
        }
    }
}
