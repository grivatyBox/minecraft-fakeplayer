package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;

public class DripstoneClusterFeature extends WorldGenerator<DripstoneClusterConfiguration> {

    public DripstoneClusterFeature(Codec<DripstoneClusterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<DripstoneClusterConfiguration> featureplacecontext) {
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        BlockPosition blockposition = featureplacecontext.origin();
        DripstoneClusterConfiguration dripstoneclusterconfiguration = featureplacecontext.config();
        RandomSource randomsource = featureplacecontext.random();

        if (!DripstoneUtils.isEmptyOrWater(generatoraccessseed, blockposition)) {
            return false;
        } else {
            int i = dripstoneclusterconfiguration.height.sample(randomsource);
            float f = dripstoneclusterconfiguration.wetness.sample(randomsource);
            float f1 = dripstoneclusterconfiguration.density.sample(randomsource);
            int j = dripstoneclusterconfiguration.radius.sample(randomsource);
            int k = dripstoneclusterconfiguration.radius.sample(randomsource);

            for (int l = -j; l <= j; ++l) {
                for (int i1 = -k; i1 <= k; ++i1) {
                    double d0 = this.getChanceOfStalagmiteOrStalactite(j, k, l, i1, dripstoneclusterconfiguration);
                    BlockPosition blockposition1 = blockposition.offset(l, 0, i1);

                    this.placeColumn(generatoraccessseed, randomsource, blockposition1, l, i1, f, d0, i, f1, dripstoneclusterconfiguration);
                }
            }

            return true;
        }
    }

    private void placeColumn(GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, BlockPosition blockposition, int i, int j, float f, double d0, int k, float f1, DripstoneClusterConfiguration dripstoneclusterconfiguration) {
        Optional<Column> optional = Column.scan(generatoraccessseed, blockposition, dripstoneclusterconfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);

        if (!optional.isEmpty()) {
            OptionalInt optionalint = ((Column) optional.get()).getCeiling();
            OptionalInt optionalint1 = ((Column) optional.get()).getFloor();

            if (!optionalint.isEmpty() || !optionalint1.isEmpty()) {
                boolean flag = randomsource.nextFloat() < f;
                Column column;

                if (flag && optionalint1.isPresent() && this.canPlacePool(generatoraccessseed, blockposition.atY(optionalint1.getAsInt()))) {
                    int l = optionalint1.getAsInt();

                    column = ((Column) optional.get()).withFloor(OptionalInt.of(l - 1));
                    generatoraccessseed.setBlock(blockposition.atY(l), Blocks.WATER.defaultBlockState(), 2);
                } else {
                    column = (Column) optional.get();
                }

                OptionalInt optionalint2 = column.getFloor();
                boolean flag1 = randomsource.nextDouble() < d0;
                int i1;

                if (optionalint.isPresent() && flag1 && !this.isLava(generatoraccessseed, blockposition.atY(optionalint.getAsInt()))) {
                    int j1 = dripstoneclusterconfiguration.dripstoneBlockLayerThickness.sample(randomsource);

                    this.replaceBlocksWithDripstoneBlocks(generatoraccessseed, blockposition.atY(optionalint.getAsInt()), j1, EnumDirection.UP);
                    int k1;

                    if (optionalint2.isPresent()) {
                        k1 = Math.min(k, optionalint.getAsInt() - optionalint2.getAsInt());
                    } else {
                        k1 = k;
                    }

                    i1 = this.getDripstoneHeight(randomsource, i, j, f1, k1, dripstoneclusterconfiguration);
                } else {
                    i1 = 0;
                }

                boolean flag2 = randomsource.nextDouble() < d0;
                int l1;

                if (optionalint2.isPresent() && flag2 && !this.isLava(generatoraccessseed, blockposition.atY(optionalint2.getAsInt()))) {
                    int i2 = dripstoneclusterconfiguration.dripstoneBlockLayerThickness.sample(randomsource);

                    this.replaceBlocksWithDripstoneBlocks(generatoraccessseed, blockposition.atY(optionalint2.getAsInt()), i2, EnumDirection.DOWN);
                    if (optionalint.isPresent()) {
                        l1 = Math.max(0, i1 + MathHelper.randomBetweenInclusive(randomsource, -dripstoneclusterconfiguration.maxStalagmiteStalactiteHeightDiff, dripstoneclusterconfiguration.maxStalagmiteStalactiteHeightDiff));
                    } else {
                        l1 = this.getDripstoneHeight(randomsource, i, j, f1, k, dripstoneclusterconfiguration);
                    }
                } else {
                    l1 = 0;
                }

                int j2;
                int k2;

                if (optionalint.isPresent() && optionalint2.isPresent() && optionalint.getAsInt() - i1 <= optionalint2.getAsInt() + l1) {
                    int l2 = optionalint2.getAsInt();
                    int i3 = optionalint.getAsInt();
                    int j3 = Math.max(i3 - i1, l2 + 1);
                    int k3 = Math.min(l2 + l1, i3 - 1);
                    int l3 = MathHelper.randomBetweenInclusive(randomsource, j3, k3 + 1);
                    int i4 = l3 - 1;

                    k2 = i3 - l3;
                    j2 = i4 - l2;
                } else {
                    k2 = i1;
                    j2 = l1;
                }

                boolean flag3 = randomsource.nextBoolean() && k2 > 0 && j2 > 0 && column.getHeight().isPresent() && k2 + j2 == column.getHeight().getAsInt();

                if (optionalint.isPresent()) {
                    DripstoneUtils.growPointedDripstone(generatoraccessseed, blockposition.atY(optionalint.getAsInt() - 1), EnumDirection.DOWN, k2, flag3);
                }

                if (optionalint2.isPresent()) {
                    DripstoneUtils.growPointedDripstone(generatoraccessseed, blockposition.atY(optionalint2.getAsInt() + 1), EnumDirection.UP, j2, flag3);
                }

            }
        }
    }

    private boolean isLava(IWorldReader iworldreader, BlockPosition blockposition) {
        return iworldreader.getBlockState(blockposition).is(Blocks.LAVA);
    }

    private int getDripstoneHeight(RandomSource randomsource, int i, int j, float f, int k, DripstoneClusterConfiguration dripstoneclusterconfiguration) {
        if (randomsource.nextFloat() > f) {
            return 0;
        } else {
            int l = Math.abs(i) + Math.abs(j);
            float f1 = (float) MathHelper.clampedMap((double) l, 0.0D, (double) dripstoneclusterconfiguration.maxDistanceFromCenterAffectingHeightBias, (double) k / 2.0D, 0.0D);

            return (int) randomBetweenBiased(randomsource, 0.0F, (float) k, f1, (float) dripstoneclusterconfiguration.heightDeviation);
        }
    }

    private boolean canPlacePool(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition);

        if (!iblockdata.is(Blocks.WATER) && !iblockdata.is(Blocks.DRIPSTONE_BLOCK) && !iblockdata.is(Blocks.POINTED_DRIPSTONE)) {
            if (generatoraccessseed.getBlockState(blockposition.above()).getFluidState().is(TagsFluid.WATER)) {
                return false;
            } else {
                for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                    if (!this.canBeAdjacentToWater(generatoraccessseed, blockposition.relative(enumdirection))) {
                        return false;
                    }
                }

                return this.canBeAdjacentToWater(generatoraccessseed, blockposition.below());
            }
        } else {
            return false;
        }
    }

    private boolean canBeAdjacentToWater(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);

        return iblockdata.is(TagsBlock.BASE_STONE_OVERWORLD) || iblockdata.getFluidState().is(TagsFluid.WATER);
    }

    private void replaceBlocksWithDripstoneBlocks(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition, int i, EnumDirection enumdirection) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        for (int j = 0; j < i; ++j) {
            if (!DripstoneUtils.placeDripstoneBlockIfPossible(generatoraccessseed, blockposition_mutableblockposition)) {
                return;
            }

            blockposition_mutableblockposition.move(enumdirection);
        }

    }

    private double getChanceOfStalagmiteOrStalactite(int i, int j, int k, int l, DripstoneClusterConfiguration dripstoneclusterconfiguration) {
        int i1 = i - Math.abs(k);
        int j1 = j - Math.abs(l);
        int k1 = Math.min(i1, j1);

        return (double) MathHelper.clampedMap((float) k1, 0.0F, (float) dripstoneclusterconfiguration.maxDistanceFromEdgeAffectingChanceOfDripstoneColumn, dripstoneclusterconfiguration.chanceOfDripstoneColumnAtMaxDistanceFromCenter, 1.0F);
    }

    private static float randomBetweenBiased(RandomSource randomsource, float f, float f1, float f2, float f3) {
        return ClampedNormalFloat.sample(randomsource, f2, f3, f, f1);
    }
}
