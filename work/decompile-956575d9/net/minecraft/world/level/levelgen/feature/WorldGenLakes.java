package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;

/** @deprecated */
@Deprecated
public class WorldGenLakes extends WorldGenerator<WorldGenLakes.a> {

    private static final IBlockData AIR = Blocks.CAVE_AIR.defaultBlockState();

    public WorldGenLakes(Codec<WorldGenLakes.a> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenLakes.a> featureplacecontext) {
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        RandomSource randomsource = featureplacecontext.random();
        WorldGenLakes.a worldgenlakes_a = featureplacecontext.config();

        if (blockposition.getY() <= generatoraccessseed.getMinY() + 4) {
            return false;
        } else {
            blockposition = blockposition.below(4);
            boolean[] aboolean = new boolean[2048];
            int i = randomsource.nextInt(4) + 4;

            for (int j = 0; j < i; ++j) {
                double d0 = randomsource.nextDouble() * 6.0D + 3.0D;
                double d1 = randomsource.nextDouble() * 4.0D + 2.0D;
                double d2 = randomsource.nextDouble() * 6.0D + 3.0D;
                double d3 = randomsource.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;
                double d4 = randomsource.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;
                double d5 = randomsource.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;

                for (int k = 1; k < 15; ++k) {
                    for (int l = 1; l < 15; ++l) {
                        for (int i1 = 1; i1 < 7; ++i1) {
                            double d6 = ((double) k - d3) / (d0 / 2.0D);
                            double d7 = ((double) i1 - d4) / (d1 / 2.0D);
                            double d8 = ((double) l - d5) / (d2 / 2.0D);
                            double d9 = d6 * d6 + d7 * d7 + d8 * d8;

                            if (d9 < 1.0D) {
                                aboolean[(k * 16 + l) * 8 + i1] = true;
                            }
                        }
                    }
                }
            }

            IBlockData iblockdata = worldgenlakes_a.fluid().getState(randomsource, blockposition);

            for (int j1 = 0; j1 < 16; ++j1) {
                for (int k1 = 0; k1 < 16; ++k1) {
                    for (int l1 = 0; l1 < 8; ++l1) {
                        boolean flag = !aboolean[(j1 * 16 + k1) * 8 + l1] && (j1 < 15 && aboolean[((j1 + 1) * 16 + k1) * 8 + l1] || j1 > 0 && aboolean[((j1 - 1) * 16 + k1) * 8 + l1] || k1 < 15 && aboolean[(j1 * 16 + k1 + 1) * 8 + l1] || k1 > 0 && aboolean[(j1 * 16 + (k1 - 1)) * 8 + l1] || l1 < 7 && aboolean[(j1 * 16 + k1) * 8 + l1 + 1] || l1 > 0 && aboolean[(j1 * 16 + k1) * 8 + (l1 - 1)]);

                        if (flag) {
                            IBlockData iblockdata1 = generatoraccessseed.getBlockState(blockposition.offset(j1, l1, k1));

                            if (l1 >= 4 && iblockdata1.liquid()) {
                                return false;
                            }

                            if (l1 < 4 && !iblockdata1.isSolid() && generatoraccessseed.getBlockState(blockposition.offset(j1, l1, k1)) != iblockdata) {
                                return false;
                            }
                        }
                    }
                }
            }

            for (int i2 = 0; i2 < 16; ++i2) {
                for (int j2 = 0; j2 < 16; ++j2) {
                    for (int k2 = 0; k2 < 8; ++k2) {
                        if (aboolean[(i2 * 16 + j2) * 8 + k2]) {
                            BlockPosition blockposition1 = blockposition.offset(i2, k2, j2);

                            if (this.canReplaceBlock(generatoraccessseed.getBlockState(blockposition1))) {
                                boolean flag1 = k2 >= 4;

                                generatoraccessseed.setBlock(blockposition1, flag1 ? WorldGenLakes.AIR : iblockdata, 2);
                                if (flag1) {
                                    generatoraccessseed.scheduleTick(blockposition1, WorldGenLakes.AIR.getBlock(), 0);
                                    this.markAboveForPostProcessing(generatoraccessseed, blockposition1);
                                }
                            }
                        }
                    }
                }
            }

            IBlockData iblockdata2 = worldgenlakes_a.barrier().getState(randomsource, blockposition);

            if (!iblockdata2.isAir()) {
                for (int l2 = 0; l2 < 16; ++l2) {
                    for (int i3 = 0; i3 < 16; ++i3) {
                        for (int j3 = 0; j3 < 8; ++j3) {
                            boolean flag2 = !aboolean[(l2 * 16 + i3) * 8 + j3] && (l2 < 15 && aboolean[((l2 + 1) * 16 + i3) * 8 + j3] || l2 > 0 && aboolean[((l2 - 1) * 16 + i3) * 8 + j3] || i3 < 15 && aboolean[(l2 * 16 + i3 + 1) * 8 + j3] || i3 > 0 && aboolean[(l2 * 16 + (i3 - 1)) * 8 + j3] || j3 < 7 && aboolean[(l2 * 16 + i3) * 8 + j3 + 1] || j3 > 0 && aboolean[(l2 * 16 + i3) * 8 + (j3 - 1)]);

                            if (flag2 && (j3 < 4 || randomsource.nextInt(2) != 0)) {
                                IBlockData iblockdata3 = generatoraccessseed.getBlockState(blockposition.offset(l2, j3, i3));

                                if (iblockdata3.isSolid() && !iblockdata3.is(TagsBlock.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                                    BlockPosition blockposition2 = blockposition.offset(l2, j3, i3);

                                    generatoraccessseed.setBlock(blockposition2, iblockdata2, 2);
                                    this.markAboveForPostProcessing(generatoraccessseed, blockposition2);
                                }
                            }
                        }
                    }
                }
            }

            if (iblockdata.getFluidState().is(TagsFluid.WATER)) {
                for (int k3 = 0; k3 < 16; ++k3) {
                    for (int l3 = 0; l3 < 16; ++l3) {
                        int i4 = 4;
                        BlockPosition blockposition3 = blockposition.offset(k3, 4, l3);

                        if (((BiomeBase) generatoraccessseed.getBiome(blockposition3).value()).shouldFreeze(generatoraccessseed, blockposition3, false) && this.canReplaceBlock(generatoraccessseed.getBlockState(blockposition3))) {
                            generatoraccessseed.setBlock(blockposition3, Blocks.ICE.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean canReplaceBlock(IBlockData iblockdata) {
        return !iblockdata.is(TagsBlock.FEATURES_CANNOT_REPLACE);
    }

    public static record a(WorldGenFeatureStateProvider fluid, WorldGenFeatureStateProvider barrier) implements WorldGenFeatureConfiguration {

        public static final Codec<WorldGenLakes.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(WorldGenFeatureStateProvider.CODEC.fieldOf("fluid").forGetter(WorldGenLakes.a::fluid), WorldGenFeatureStateProvider.CODEC.fieldOf("barrier").forGetter(WorldGenLakes.a::barrier)).apply(instance, WorldGenLakes.a::new);
        });
    }
}
