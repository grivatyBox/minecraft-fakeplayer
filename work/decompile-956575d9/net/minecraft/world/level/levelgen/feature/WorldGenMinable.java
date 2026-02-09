package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureOreConfiguration;

public class WorldGenMinable extends WorldGenerator<WorldGenFeatureOreConfiguration> {

    public WorldGenMinable(Codec<WorldGenFeatureOreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureOreConfiguration> featureplacecontext) {
        RandomSource randomsource = featureplacecontext.random();
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        WorldGenFeatureOreConfiguration worldgenfeatureoreconfiguration = featureplacecontext.config();
        float f = randomsource.nextFloat() * (float) Math.PI;
        float f1 = (float) worldgenfeatureoreconfiguration.size / 8.0F;
        int i = MathHelper.ceil(((float) worldgenfeatureoreconfiguration.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double d0 = (double) blockposition.getX() + Math.sin((double) f) * (double) f1;
        double d1 = (double) blockposition.getX() - Math.sin((double) f) * (double) f1;
        double d2 = (double) blockposition.getZ() + Math.cos((double) f) * (double) f1;
        double d3 = (double) blockposition.getZ() - Math.cos((double) f) * (double) f1;
        int j = 2;
        double d4 = (double) (blockposition.getY() + randomsource.nextInt(3) - 2);
        double d5 = (double) (blockposition.getY() + randomsource.nextInt(3) - 2);
        int k = blockposition.getX() - MathHelper.ceil(f1) - i;
        int l = blockposition.getY() - 2 - i;
        int i1 = blockposition.getZ() - MathHelper.ceil(f1) - i;
        int j1 = 2 * (MathHelper.ceil(f1) + i);
        int k1 = 2 * (2 + i);

        for (int l1 = k; l1 <= k + j1; ++l1) {
            for (int i2 = i1; i2 <= i1 + j1; ++i2) {
                if (l <= generatoraccessseed.getHeight(HeightMap.Type.OCEAN_FLOOR_WG, l1, i2)) {
                    return this.doPlace(generatoraccessseed, randomsource, worldgenfeatureoreconfiguration, d0, d1, d2, d3, d4, d5, k, l, i1, j1, k1);
                }
            }
        }

        return false;
    }

    protected boolean doPlace(GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, WorldGenFeatureOreConfiguration worldgenfeatureoreconfiguration, double d0, double d1, double d2, double d3, double d4, double d5, int i, int j, int k, int l, int i1) {
        int j1 = 0;
        BitSet bitset = new BitSet(l * i1 * l);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        int k1 = worldgenfeatureoreconfiguration.size;
        double[] adouble = new double[k1 * 4];

        for (int l1 = 0; l1 < k1; ++l1) {
            float f = (float) l1 / (float) k1;
            double d6 = MathHelper.lerp((double) f, d0, d1);
            double d7 = MathHelper.lerp((double) f, d4, d5);
            double d8 = MathHelper.lerp((double) f, d2, d3);
            double d9 = randomsource.nextDouble() * (double) k1 / 16.0D;
            double d10 = ((double) (MathHelper.sin((float) Math.PI * f) + 1.0F) * d9 + 1.0D) / 2.0D;

            adouble[l1 * 4 + 0] = d6;
            adouble[l1 * 4 + 1] = d7;
            adouble[l1 * 4 + 2] = d8;
            adouble[l1 * 4 + 3] = d10;
        }

        for (int i2 = 0; i2 < k1 - 1; ++i2) {
            if (adouble[i2 * 4 + 3] > 0.0D) {
                for (int j2 = i2 + 1; j2 < k1; ++j2) {
                    if (adouble[j2 * 4 + 3] > 0.0D) {
                        double d11 = adouble[i2 * 4 + 0] - adouble[j2 * 4 + 0];
                        double d12 = adouble[i2 * 4 + 1] - adouble[j2 * 4 + 1];
                        double d13 = adouble[i2 * 4 + 2] - adouble[j2 * 4 + 2];
                        double d14 = adouble[i2 * 4 + 3] - adouble[j2 * 4 + 3];

                        if (d14 * d14 > d11 * d11 + d12 * d12 + d13 * d13) {
                            if (d14 > 0.0D) {
                                adouble[j2 * 4 + 3] = -1.0D;
                            } else {
                                adouble[i2 * 4 + 3] = -1.0D;
                            }
                        }
                    }
                }
            }
        }

        try (BulkSectionAccess bulksectionaccess = new BulkSectionAccess(generatoraccessseed)) {
            for (int k2 = 0; k2 < k1; ++k2) {
                double d15 = adouble[k2 * 4 + 3];

                if (d15 >= 0.0D) {
                    double d16 = adouble[k2 * 4 + 0];
                    double d17 = adouble[k2 * 4 + 1];
                    double d18 = adouble[k2 * 4 + 2];
                    int l2 = Math.max(MathHelper.floor(d16 - d15), i);
                    int i3 = Math.max(MathHelper.floor(d17 - d15), j);
                    int j3 = Math.max(MathHelper.floor(d18 - d15), k);
                    int k3 = Math.max(MathHelper.floor(d16 + d15), l2);
                    int l3 = Math.max(MathHelper.floor(d17 + d15), i3);
                    int i4 = Math.max(MathHelper.floor(d18 + d15), j3);

                    for (int j4 = l2; j4 <= k3; ++j4) {
                        double d19 = ((double) j4 + 0.5D - d16) / d15;

                        if (d19 * d19 < 1.0D) {
                            for (int k4 = i3; k4 <= l3; ++k4) {
                                double d20 = ((double) k4 + 0.5D - d17) / d15;

                                if (d19 * d19 + d20 * d20 < 1.0D) {
                                    for (int l4 = j3; l4 <= i4; ++l4) {
                                        double d21 = ((double) l4 + 0.5D - d18) / d15;

                                        if (d19 * d19 + d20 * d20 + d21 * d21 < 1.0D && !generatoraccessseed.isOutsideBuildHeight(k4)) {
                                            int i5 = j4 - i + (k4 - j) * l + (l4 - k) * l * i1;

                                            if (!bitset.get(i5)) {
                                                bitset.set(i5);
                                                blockposition_mutableblockposition.set(j4, k4, l4);
                                                if (generatoraccessseed.ensureCanWrite(blockposition_mutableblockposition)) {
                                                    ChunkSection chunksection = bulksectionaccess.getSection(blockposition_mutableblockposition);

                                                    if (chunksection != null) {
                                                        int j5 = SectionPosition.sectionRelative(j4);
                                                        int k5 = SectionPosition.sectionRelative(k4);
                                                        int l5 = SectionPosition.sectionRelative(l4);
                                                        IBlockData iblockdata = chunksection.getBlockState(j5, k5, l5);

                                                        for (WorldGenFeatureOreConfiguration.a worldgenfeatureoreconfiguration_a : worldgenfeatureoreconfiguration.targetStates) {
                                                            Objects.requireNonNull(bulksectionaccess);
                                                            if (canPlaceOre(iblockdata, bulksectionaccess::getBlockState, randomsource, worldgenfeatureoreconfiguration, worldgenfeatureoreconfiguration_a, blockposition_mutableblockposition)) {
                                                                chunksection.setBlockState(j5, k5, l5, worldgenfeatureoreconfiguration_a.state, false);
                                                                ++j1;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return j1 > 0;
    }

    public static boolean canPlaceOre(IBlockData iblockdata, Function<BlockPosition, IBlockData> function, RandomSource randomsource, WorldGenFeatureOreConfiguration worldgenfeatureoreconfiguration, WorldGenFeatureOreConfiguration.a worldgenfeatureoreconfiguration_a, BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        return !worldgenfeatureoreconfiguration_a.target.test(iblockdata, randomsource) ? false : (shouldSkipAirCheck(randomsource, worldgenfeatureoreconfiguration.discardChanceOnAirExposure) ? true : !isAdjacentToAir(function, blockposition_mutableblockposition));
    }

    protected static boolean shouldSkipAirCheck(RandomSource randomsource, float f) {
        return f <= 0.0F ? true : (f >= 1.0F ? false : randomsource.nextFloat() >= f);
    }
}
