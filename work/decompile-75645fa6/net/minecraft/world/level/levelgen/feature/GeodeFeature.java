package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.SeededRandom;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorNormal;
import net.minecraft.world.level.material.Fluid;

public class GeodeFeature extends WorldGenerator<GeodeConfiguration> {

    private static final EnumDirection[] DIRECTIONS = EnumDirection.values();

    public GeodeFeature(Codec<GeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> featureplacecontext) {
        GeodeConfiguration geodeconfiguration = featureplacecontext.config();
        RandomSource randomsource = featureplacecontext.random();
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        int i = geodeconfiguration.minGenOffset;
        int j = geodeconfiguration.maxGenOffset;
        List<Pair<BlockPosition, Integer>> list = Lists.newLinkedList();
        int k = geodeconfiguration.distributionPoints.sample(randomsource);
        SeededRandom seededrandom = new SeededRandom(new LegacyRandomSource(generatoraccessseed.getSeed()));
        NoiseGeneratorNormal noisegeneratornormal = NoiseGeneratorNormal.create(seededrandom, -4, 1.0D);
        List<BlockPosition> list1 = Lists.newLinkedList();
        double d0 = (double) k / (double) geodeconfiguration.outerWallDistance.getMaxValue();
        GeodeLayerSettings geodelayersettings = geodeconfiguration.geodeLayerSettings;
        GeodeBlockSettings geodeblocksettings = geodeconfiguration.geodeBlockSettings;
        GeodeCrackSettings geodecracksettings = geodeconfiguration.geodeCrackSettings;
        double d1 = 1.0D / Math.sqrt(geodelayersettings.filling);
        double d2 = 1.0D / Math.sqrt(geodelayersettings.innerLayer + d0);
        double d3 = 1.0D / Math.sqrt(geodelayersettings.middleLayer + d0);
        double d4 = 1.0D / Math.sqrt(geodelayersettings.outerLayer + d0);
        double d5 = 1.0D / Math.sqrt(geodecracksettings.baseCrackSize + randomsource.nextDouble() / 2.0D + (k > 3 ? d0 : 0.0D));
        boolean flag = (double) randomsource.nextFloat() < geodecracksettings.generateCrackChance;
        int l = 0;

        for (int i1 = 0; i1 < k; ++i1) {
            int j1 = geodeconfiguration.outerWallDistance.sample(randomsource);
            int k1 = geodeconfiguration.outerWallDistance.sample(randomsource);
            int l1 = geodeconfiguration.outerWallDistance.sample(randomsource);
            BlockPosition blockposition1 = blockposition.offset(j1, k1, l1);
            IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition1);

            if (iblockdata.isAir() || iblockdata.is(geodeblocksettings.invalidBlocks)) {
                ++l;
                if (l > geodeconfiguration.invalidBlocksThreshold) {
                    return false;
                }
            }

            list.add(Pair.of(blockposition1, geodeconfiguration.pointOffset.sample(randomsource)));
        }

        if (flag) {
            int i2 = randomsource.nextInt(4);
            int j2 = k * 2 + 1;

            if (i2 == 0) {
                list1.add(blockposition.offset(j2, 7, 0));
                list1.add(blockposition.offset(j2, 5, 0));
                list1.add(blockposition.offset(j2, 1, 0));
            } else if (i2 == 1) {
                list1.add(blockposition.offset(0, 7, j2));
                list1.add(blockposition.offset(0, 5, j2));
                list1.add(blockposition.offset(0, 1, j2));
            } else if (i2 == 2) {
                list1.add(blockposition.offset(j2, 7, j2));
                list1.add(blockposition.offset(j2, 5, j2));
                list1.add(blockposition.offset(j2, 1, j2));
            } else {
                list1.add(blockposition.offset(0, 7, 0));
                list1.add(blockposition.offset(0, 5, 0));
                list1.add(blockposition.offset(0, 1, 0));
            }
        }

        List<BlockPosition> list2 = Lists.newArrayList();
        Predicate<IBlockData> predicate = isReplaceable(geodeconfiguration.geodeBlockSettings.cannotReplace);

        for (BlockPosition blockposition2 : BlockPosition.betweenClosed(blockposition.offset(i, i, i), blockposition.offset(j, j, j))) {
            double d6 = noisegeneratornormal.getValue((double) blockposition2.getX(), (double) blockposition2.getY(), (double) blockposition2.getZ()) * geodeconfiguration.noiseMultiplier;
            double d7 = 0.0D;
            double d8 = 0.0D;

            for (Pair<BlockPosition, Integer> pair : list) {
                d7 += MathHelper.invSqrt(blockposition2.distSqr((BaseBlockPosition) pair.getFirst()) + (double) (Integer) pair.getSecond()) + d6;
            }

            for (BlockPosition blockposition3 : list1) {
                d8 += MathHelper.invSqrt(blockposition2.distSqr(blockposition3) + (double) geodecracksettings.crackPointOffset) + d6;
            }

            if (d7 >= d4) {
                if (flag && d8 >= d5 && d7 < d1) {
                    this.safeSetBlock(generatoraccessseed, blockposition2, Blocks.AIR.defaultBlockState(), predicate);

                    for (EnumDirection enumdirection : GeodeFeature.DIRECTIONS) {
                        BlockPosition blockposition4 = blockposition2.relative(enumdirection);
                        Fluid fluid = generatoraccessseed.getFluidState(blockposition4);

                        if (!fluid.isEmpty()) {
                            generatoraccessseed.scheduleTick(blockposition4, fluid.getType(), 0);
                        }
                    }
                } else if (d7 >= d1) {
                    this.safeSetBlock(generatoraccessseed, blockposition2, geodeblocksettings.fillingProvider.getState(randomsource, blockposition2), predicate);
                } else if (d7 >= d2) {
                    boolean flag1 = (double) randomsource.nextFloat() < geodeconfiguration.useAlternateLayer0Chance;

                    if (flag1) {
                        this.safeSetBlock(generatoraccessseed, blockposition2, geodeblocksettings.alternateInnerLayerProvider.getState(randomsource, blockposition2), predicate);
                    } else {
                        this.safeSetBlock(generatoraccessseed, blockposition2, geodeblocksettings.innerLayerProvider.getState(randomsource, blockposition2), predicate);
                    }

                    if ((!geodeconfiguration.placementsRequireLayer0Alternate || flag1) && (double) randomsource.nextFloat() < geodeconfiguration.usePotentialPlacementsChance) {
                        list2.add(blockposition2.immutable());
                    }
                } else if (d7 >= d3) {
                    this.safeSetBlock(generatoraccessseed, blockposition2, geodeblocksettings.middleLayerProvider.getState(randomsource, blockposition2), predicate);
                } else if (d7 >= d4) {
                    this.safeSetBlock(generatoraccessseed, blockposition2, geodeblocksettings.outerLayerProvider.getState(randomsource, blockposition2), predicate);
                }
            }
        }

        List<IBlockData> list3 = geodeblocksettings.innerPlacements;

        for (BlockPosition blockposition5 : list2) {
            IBlockData iblockdata1 = (IBlockData) SystemUtils.getRandom(list3, randomsource);

            for (EnumDirection enumdirection1 : GeodeFeature.DIRECTIONS) {
                if (iblockdata1.hasProperty(BlockProperties.FACING)) {
                    iblockdata1 = (IBlockData) iblockdata1.setValue(BlockProperties.FACING, enumdirection1);
                }

                BlockPosition blockposition6 = blockposition5.relative(enumdirection1);
                IBlockData iblockdata2 = generatoraccessseed.getBlockState(blockposition6);

                if (iblockdata1.hasProperty(BlockProperties.WATERLOGGED)) {
                    iblockdata1 = (IBlockData) iblockdata1.setValue(BlockProperties.WATERLOGGED, iblockdata2.getFluidState().isSource());
                }

                if (BuddingAmethystBlock.canClusterGrowAtState(iblockdata2)) {
                    this.safeSetBlock(generatoraccessseed, blockposition6, iblockdata1, predicate);
                    break;
                }
            }
        }

        return true;
    }
}
