package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class MangroveLeavesBlock extends TintedParticleLeavesBlock implements IBlockFragilePlantElement {

    public static final MapCodec<MangroveLeavesBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter((mangroveleavesblock) -> {
            return mangroveleavesblock.leafParticleChance;
        }), propertiesCodec()).apply(instance, MangroveLeavesBlock::new);
    });

    @Override
    public MapCodec<MangroveLeavesBlock> codec() {
        return MangroveLeavesBlock.CODEC;
    }

    public MangroveLeavesBlock(float f, BlockBase.Info blockbase_info) {
        super(f, blockbase_info);
    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return iworldreader.getBlockState(blockposition.below()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        worldserver.setBlock(blockposition.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
    }

    @Override
    public BlockPosition getParticlePos(BlockPosition blockposition) {
        return blockposition.below();
    }
}
