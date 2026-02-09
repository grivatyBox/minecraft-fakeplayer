package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureLakeConfiguration;

public class WorldGenTaigaStructure extends WorldGenerator<WorldGenFeatureLakeConfiguration> {

    public WorldGenTaigaStructure(Codec<WorldGenFeatureLakeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureLakeConfiguration> featureplacecontext) {
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        RandomSource randomsource = featureplacecontext.random();

        WorldGenFeatureLakeConfiguration worldgenfeaturelakeconfiguration;

        for (worldgenfeaturelakeconfiguration = featureplacecontext.config(); blockposition.getY() > generatoraccessseed.getMinY() + 3; blockposition = blockposition.below()) {
            if (!generatoraccessseed.isEmptyBlock(blockposition.below())) {
                IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition.below());

                if (isDirt(iblockdata) || isStone(iblockdata)) {
                    break;
                }
            }
        }

        if (blockposition.getY() <= generatoraccessseed.getMinY() + 3) {
            return false;
        } else {
            for (int i = 0; i < 3; ++i) {
                int j = randomsource.nextInt(2);
                int k = randomsource.nextInt(2);
                int l = randomsource.nextInt(2);
                float f = (float) (j + k + l) * 0.333F + 0.5F;

                for (BlockPosition blockposition1 : BlockPosition.betweenClosed(blockposition.offset(-j, -k, -l), blockposition.offset(j, k, l))) {
                    if (blockposition1.distSqr(blockposition) <= (double) (f * f)) {
                        generatoraccessseed.setBlock(blockposition1, worldgenfeaturelakeconfiguration.state, 3);
                    }
                }

                blockposition = blockposition.offset(-1 + randomsource.nextInt(2), -randomsource.nextInt(2), -1 + randomsource.nextInt(2));
            }

            return true;
        }
    }
}
