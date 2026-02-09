package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureOreConfiguration;

public class ScatteredOreFeature extends WorldGenerator<WorldGenFeatureOreConfiguration> {

    private static final int MAX_DIST_FROM_ORIGIN = 7;

    ScatteredOreFeature(Codec<WorldGenFeatureOreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureOreConfiguration> featureplacecontext) {
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        RandomSource randomsource = featureplacecontext.random();
        WorldGenFeatureOreConfiguration worldgenfeatureoreconfiguration = featureplacecontext.config();
        BlockPosition blockposition = featureplacecontext.origin();
        int i = randomsource.nextInt(worldgenfeatureoreconfiguration.size + 1);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int j = 0; j < i; ++j) {
            this.offsetTargetPos(blockposition_mutableblockposition, randomsource, blockposition, Math.min(j, 7));
            IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition_mutableblockposition);

            for (WorldGenFeatureOreConfiguration.a worldgenfeatureoreconfiguration_a : worldgenfeatureoreconfiguration.targetStates) {
                Objects.requireNonNull(generatoraccessseed);
                if (WorldGenMinable.canPlaceOre(iblockdata, generatoraccessseed::getBlockState, randomsource, worldgenfeatureoreconfiguration, worldgenfeatureoreconfiguration_a, blockposition_mutableblockposition)) {
                    generatoraccessseed.setBlock(blockposition_mutableblockposition, worldgenfeatureoreconfiguration_a.state, 2);
                    break;
                }
            }
        }

        return true;
    }

    private void offsetTargetPos(BlockPosition.MutableBlockPosition blockposition_mutableblockposition, RandomSource randomsource, BlockPosition blockposition, int i) {
        int j = this.getRandomPlacementInOneAxisRelativeToOrigin(randomsource, i);
        int k = this.getRandomPlacementInOneAxisRelativeToOrigin(randomsource, i);
        int l = this.getRandomPlacementInOneAxisRelativeToOrigin(randomsource, i);

        blockposition_mutableblockposition.setWithOffset(blockposition, j, k, l);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource randomsource, int i) {
        return Math.round((randomsource.nextFloat() - randomsource.nextFloat()) * (float) i);
    }
}
