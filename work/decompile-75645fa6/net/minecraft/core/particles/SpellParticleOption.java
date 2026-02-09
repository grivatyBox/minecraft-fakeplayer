package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class SpellParticleOption implements ParticleParam {

    private final Particle<SpellParticleOption> type;
    private final int color;
    private final float power;

    public static MapCodec<SpellParticleOption> codec(Particle<SpellParticleOption> particle) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color", -1).forGetter((spellparticleoption) -> {
                return spellparticleoption.color;
            }), Codec.FLOAT.optionalFieldOf("power", 1.0F).forGetter((spellparticleoption) -> {
                return spellparticleoption.power;
            })).apply(instance, (integer, ofloat) -> {
                return new SpellParticleOption(particle, integer, ofloat);
            });
        });
    }

    public static StreamCodec<? super ByteBuf, SpellParticleOption> streamCodec(Particle<SpellParticleOption> particle) {
        return StreamCodec.composite(ByteBufCodecs.INT, (spellparticleoption) -> {
            return spellparticleoption.color;
        }, ByteBufCodecs.FLOAT, (spellparticleoption) -> {
            return spellparticleoption.power;
        }, (integer, ofloat) -> {
            return new SpellParticleOption(particle, integer, ofloat);
        });
    }

    private SpellParticleOption(Particle<SpellParticleOption> particle, int i, float f) {
        this.type = particle;
        this.color = i;
        this.power = f;
    }

    @Override
    public Particle<SpellParticleOption> getType() {
        return this.type;
    }

    public float getRed() {
        return (float) ARGB.red(this.color) / 255.0F;
    }

    public float getGreen() {
        return (float) ARGB.green(this.color) / 255.0F;
    }

    public float getBlue() {
        return (float) ARGB.blue(this.color) / 255.0F;
    }

    public float getPower() {
        return this.power;
    }

    public static SpellParticleOption create(Particle<SpellParticleOption> particle, int i, float f) {
        return new SpellParticleOption(particle, i, f);
    }

    public static SpellParticleOption create(Particle<SpellParticleOption> particle, float f, float f1, float f2, float f3) {
        return create(particle, ARGB.colorFromFloat(1.0F, f, f1, f2), f3);
    }
}
