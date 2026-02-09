package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PowerParticleOption implements ParticleParam {

    private final Particle<PowerParticleOption> type;
    private final float power;

    public static MapCodec<PowerParticleOption> codec(Particle<PowerParticleOption> particle) {
        return Codec.FLOAT.xmap((ofloat) -> {
            return new PowerParticleOption(particle, ofloat);
        }, (powerparticleoption) -> {
            return powerparticleoption.power;
        }).optionalFieldOf("power", create(particle, 1.0F));
    }

    public static StreamCodec<? super ByteBuf, PowerParticleOption> streamCodec(Particle<PowerParticleOption> particle) {
        return ByteBufCodecs.FLOAT.map((ofloat) -> {
            return new PowerParticleOption(particle, ofloat);
        }, (powerparticleoption) -> {
            return powerparticleoption.power;
        });
    }

    private PowerParticleOption(Particle<PowerParticleOption> particle, float f) {
        this.type = particle;
        this.power = f;
    }

    @Override
    public Particle<PowerParticleOption> getType() {
        return this.type;
    }

    public float getPower() {
        return this.power;
    }

    public static PowerParticleOption create(Particle<PowerParticleOption> particle, float f) {
        return new PowerParticleOption(particle, f);
    }
}
