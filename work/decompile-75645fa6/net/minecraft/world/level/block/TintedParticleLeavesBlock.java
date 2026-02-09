package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.Particles;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;

public class TintedParticleLeavesBlock extends BlockLeaves {

    public static final MapCodec<TintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter((tintedparticleleavesblock) -> {
            return tintedparticleleavesblock.leafParticleChance;
        }), propertiesCodec()).apply(instance, TintedParticleLeavesBlock::new);
    });

    public TintedParticleLeavesBlock(float f, BlockBase.Info blockbase_info) {
        super(f, blockbase_info);
    }

    @Override
    protected void spawnFallingLeavesParticle(World world, BlockPosition blockposition, RandomSource randomsource) {
        ColorParticleOption colorparticleoption = ColorParticleOption.create(Particles.TINTED_LEAVES, world.getClientLeafTintColor(blockposition));

        ParticleUtils.spawnParticleBelow(world, blockposition, randomsource, colorparticleoption);
    }

    @Override
    public MapCodec<? extends TintedParticleLeavesBlock> codec() {
        return TintedParticleLeavesBlock.CODEC;
    }
}
