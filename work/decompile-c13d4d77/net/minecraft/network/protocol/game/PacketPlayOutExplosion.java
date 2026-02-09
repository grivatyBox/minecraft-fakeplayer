package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.Particle;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3D;

public class PacketPlayOutExplosion implements Packet<PacketListenerPlayOut> {

    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final List<BlockPosition> toBlow;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;
    private final ParticleParam smallExplosionParticles;
    private final ParticleParam largeExplosionParticles;
    private final Explosion.Effect blockInteraction;
    private final SoundEffect explosionSound;

    public PacketPlayOutExplosion(double d0, double d1, double d2, float f, List<BlockPosition> list, @Nullable Vec3D vec3d, Explosion.Effect explosion_effect, ParticleParam particleparam, ParticleParam particleparam1, SoundEffect soundeffect) {
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.power = f;
        this.toBlow = Lists.newArrayList(list);
        this.explosionSound = soundeffect;
        if (vec3d != null) {
            this.knockbackX = (float) vec3d.x;
            this.knockbackY = (float) vec3d.y;
            this.knockbackZ = (float) vec3d.z;
        } else {
            this.knockbackX = 0.0F;
            this.knockbackY = 0.0F;
            this.knockbackZ = 0.0F;
        }

        this.blockInteraction = explosion_effect;
        this.smallExplosionParticles = particleparam;
        this.largeExplosionParticles = particleparam1;
    }

    public PacketPlayOutExplosion(PacketDataSerializer packetdataserializer) {
        this.x = packetdataserializer.readDouble();
        this.y = packetdataserializer.readDouble();
        this.z = packetdataserializer.readDouble();
        this.power = packetdataserializer.readFloat();
        int i = MathHelper.floor(this.x);
        int j = MathHelper.floor(this.y);
        int k = MathHelper.floor(this.z);

        this.toBlow = packetdataserializer.readList((packetdataserializer1) -> {
            int l = packetdataserializer1.readByte() + i;
            int i1 = packetdataserializer1.readByte() + j;
            int j1 = packetdataserializer1.readByte() + k;

            return new BlockPosition(l, i1, j1);
        });
        this.knockbackX = packetdataserializer.readFloat();
        this.knockbackY = packetdataserializer.readFloat();
        this.knockbackZ = packetdataserializer.readFloat();
        this.blockInteraction = (Explosion.Effect) packetdataserializer.readEnum(Explosion.Effect.class);
        this.smallExplosionParticles = this.readParticle(packetdataserializer, (Particle) packetdataserializer.readById((Registry) BuiltInRegistries.PARTICLE_TYPE));
        this.largeExplosionParticles = this.readParticle(packetdataserializer, (Particle) packetdataserializer.readById((Registry) BuiltInRegistries.PARTICLE_TYPE));
        this.explosionSound = SoundEffect.readFromNetwork(packetdataserializer);
    }

    public void writeParticle(PacketDataSerializer packetdataserializer, ParticleParam particleparam) {
        packetdataserializer.writeId(BuiltInRegistries.PARTICLE_TYPE, particleparam.getType());
        particleparam.writeToNetwork(packetdataserializer);
    }

    private <T extends ParticleParam> T readParticle(PacketDataSerializer packetdataserializer, Particle<T> particle) {
        return particle.getDeserializer().fromNetwork(particle, packetdataserializer);
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeDouble(this.x);
        packetdataserializer.writeDouble(this.y);
        packetdataserializer.writeDouble(this.z);
        packetdataserializer.writeFloat(this.power);
        int i = MathHelper.floor(this.x);
        int j = MathHelper.floor(this.y);
        int k = MathHelper.floor(this.z);

        packetdataserializer.writeCollection(this.toBlow, (packetdataserializer1, blockposition) -> {
            int l = blockposition.getX() - i;
            int i1 = blockposition.getY() - j;
            int j1 = blockposition.getZ() - k;

            packetdataserializer1.writeByte(l);
            packetdataserializer1.writeByte(i1);
            packetdataserializer1.writeByte(j1);
        });
        packetdataserializer.writeFloat(this.knockbackX);
        packetdataserializer.writeFloat(this.knockbackY);
        packetdataserializer.writeFloat(this.knockbackZ);
        packetdataserializer.writeEnum(this.blockInteraction);
        this.writeParticle(packetdataserializer, this.smallExplosionParticles);
        this.writeParticle(packetdataserializer, this.largeExplosionParticles);
        this.explosionSound.writeToNetwork(packetdataserializer);
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleExplosion(this);
    }

    public float getKnockbackX() {
        return this.knockbackX;
    }

    public float getKnockbackY() {
        return this.knockbackY;
    }

    public float getKnockbackZ() {
        return this.knockbackZ;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getPower() {
        return this.power;
    }

    public List<BlockPosition> getToBlow() {
        return this.toBlow;
    }

    public Explosion.Effect getBlockInteraction() {
        return this.blockInteraction;
    }

    public ParticleParam getSmallExplosionParticles() {
        return this.smallExplosionParticles;
    }

    public ParticleParam getLargeExplosionParticles() {
        return this.largeExplosionParticles;
    }

    public SoundEffect getExplosionSound() {
        return this.explosionSound;
    }
}
