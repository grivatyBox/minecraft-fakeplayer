package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3D;

public record TargetColorParticleOption(Vec3D target, int color) implements ParticleParam {

    public static final MapCodec<TargetColorParticleOption> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Vec3D.CODEC.fieldOf("target").forGetter(TargetColorParticleOption::target), ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(TargetColorParticleOption::color)).apply(instance, TargetColorParticleOption::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetColorParticleOption> STREAM_CODEC = StreamCodec.composite(Vec3D.STREAM_CODEC, TargetColorParticleOption::target, ByteBufCodecs.INT, TargetColorParticleOption::color, TargetColorParticleOption::new);

    @Override
    public Particle<TargetColorParticleOption> getType() {
        return Particles.TRAIL;
    }
}
