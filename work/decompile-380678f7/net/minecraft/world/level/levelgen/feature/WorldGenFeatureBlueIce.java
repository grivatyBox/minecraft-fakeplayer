package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;

public class WorldGenFeatureBlueIce extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    public WorldGenFeatureBlueIce(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        RandomSource randomsource = featureplacecontext.random();

        if (blockposition.getY() > generatoraccessseed.getSeaLevel() - 1) {
            return false;
        } else if (!generatoraccessseed.getBlockState(blockposition).is(Blocks.WATER) && !generatoraccessseed.getBlockState(blockposition.below()).is(Blocks.WATER)) {
            return false;
        } else {
            boolean flag = false;

            for (EnumDirection enumdirection : EnumDirection.values()) {
                if (enumdirection != EnumDirection.DOWN && generatoraccessseed.getBlockState(blockposition.relative(enumdirection)).is(Blocks.PACKED_ICE)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                return false;
            } else {
                generatoraccessseed.setBlock(blockposition, Blocks.BLUE_ICE.defaultBlockState(), 2);

                for (int i = 0; i < 200; ++i) {
                    int j = randomsource.nextInt(5) - randomsource.nextInt(6);
                    int k = 3;

                    if (j < 2) {
                        k += j / 2;
                    }

                    if (k >= 1) {
                        BlockPosition blockposition1 = blockposition.offset(randomsource.nextInt(k) - randomsource.nextInt(k), j, randomsource.nextInt(k) - randomsource.nextInt(k));
                        IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition1);

                        if (iblockdata.isAir() || iblockdata.is(Blocks.WATER) || iblockdata.is(Blocks.PACKED_ICE) || iblockdata.is(Blocks.ICE)) {
                            for (EnumDirection enumdirection1 : EnumDirection.values()) {
                                IBlockData iblockdata1 = generatoraccessseed.getBlockState(blockposition1.relative(enumdirection1));

                                if (iblockdata1.is(Blocks.BLUE_ICE)) {
                                    generatoraccessseed.setBlock(blockposition1, Blocks.BLUE_ICE.defaultBlockState(), 2);
                                    break;
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
