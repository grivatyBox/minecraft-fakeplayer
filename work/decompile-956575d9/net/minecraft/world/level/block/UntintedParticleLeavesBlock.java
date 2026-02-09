package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;

public class UntintedParticleLeavesBlock extends BlockLeaves {

    public static final MapCodec<UntintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter((untintedparticleleavesblock) -> {
            return untintedparticleleavesblock.leafParticleChance;
        }), Particles.CODEC.fieldOf("leaf_particle").forGetter((untintedparticleleavesblock) -> {
            return untintedparticleleavesblock.leafParticle;
        }), propertiesCodec()).apply(instance, UntintedParticleLeavesBlock::new);
    });
    protected final ParticleParam leafParticle;

    public UntintedParticleLeavesBlock(float f, ParticleParam particleparam, BlockBase.Info blockbase_info) {
        super(f, blockbase_info);
        this.leafParticle = particleparam;
    }

    @Override
    protected void spawnFallingLeavesParticle(World world, BlockPosition blockposition, RandomSource randomsource) {
        ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, this.leafParticle);
    }

    @Override
    public MapCodec<UntintedParticleLeavesBlock> codec() {
        return UntintedParticleLeavesBlock.CODEC;
    }
}
