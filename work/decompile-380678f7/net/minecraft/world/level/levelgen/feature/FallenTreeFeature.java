package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.BlockRotatable;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.FallenTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.WorldGenFeatureTree;

public class FallenTreeFeature extends WorldGenerator<FallenTreeConfiguration> {

    private static final int STUMP_HEIGHT = 1;
    private static final int STUMP_HEIGHT_PLUS_EMPTY_SPACE = 2;
    private static final int FALLEN_LOG_MAX_FALL_HEIGHT_TO_GROUND = 5;
    private static final int FALLEN_LOG_MAX_GROUND_GAP = 2;
    private static final int FALLEN_LOG_MAX_SPACE_FROM_STUMP = 2;
    private static final int BLOCK_UPDATE_FLAGS = 19;

    public FallenTreeFeature(Codec<FallenTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FallenTreeConfiguration> featureplacecontext) {
        this.placeFallenTree(featureplacecontext.config(), featureplacecontext.origin(), featureplacecontext.level(), featureplacecontext.random());
        return true;
    }

    private void placeFallenTree(FallenTreeConfiguration fallentreeconfiguration, BlockPosition blockposition, GeneratorAccessSeed generatoraccessseed, RandomSource randomsource) {
        this.placeStump(fallentreeconfiguration, generatoraccessseed, randomsource, blockposition.mutable());
        EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
        int i = fallentreeconfiguration.logLength.sample(randomsource) - 2;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.relative(enumdirection, 2 + randomsource.nextInt(2)).mutable();

        this.setGroundHeightForFallenLogStartPos(generatoraccessseed, blockposition_mutableblockposition);
        if (this.canPlaceEntireFallenLog(generatoraccessseed, i, blockposition_mutableblockposition, enumdirection)) {
            this.placeFallenLog(fallentreeconfiguration, generatoraccessseed, randomsource, i, blockposition_mutableblockposition, enumdirection);
        }

    }

    private void setGroundHeightForFallenLogStartPos(GeneratorAccessSeed generatoraccessseed, BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        blockposition_mutableblockposition.move(EnumDirection.UP, 1);

        for (int i = 0; i < 6; ++i) {
            if (this.mayPlaceOn(generatoraccessseed, blockposition_mutableblockposition)) {
                return;
            }

            blockposition_mutableblockposition.move(EnumDirection.DOWN);
        }

    }

    private void placeStump(FallenTreeConfiguration fallentreeconfiguration, GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        BlockPosition blockposition = this.placeLogBlock(fallentreeconfiguration, generatoraccessseed, randomsource, blockposition_mutableblockposition, Function.identity());

        this.decorateLogs(generatoraccessseed, randomsource, Set.of(blockposition), fallentreeconfiguration.stumpDecorators);
    }

    private boolean canPlaceEntireFallenLog(GeneratorAccessSeed generatoraccessseed, int i, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, EnumDirection enumdirection) {
        int j = 0;

        for (int k = 0; k < i; ++k) {
            if (!WorldGenTrees.validTreePos(generatoraccessseed, blockposition_mutableblockposition)) {
                return false;
            }

            if (!this.isOverSolidGround(generatoraccessseed, blockposition_mutableblockposition)) {
                ++j;
                if (j > 2) {
                    return false;
                }
            } else {
                j = 0;
            }

            blockposition_mutableblockposition.move(enumdirection);
        }

        blockposition_mutableblockposition.move(enumdirection.getOpposite(), i);
        return true;
    }

    private void placeFallenLog(FallenTreeConfiguration fallentreeconfiguration, GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, int i, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, EnumDirection enumdirection) {
        Set<BlockPosition> set = new HashSet();

        for (int j = 0; j < i; ++j) {
            set.add(this.placeLogBlock(fallentreeconfiguration, generatoraccessseed, randomsource, blockposition_mutableblockposition, getSidewaysStateModifier(enumdirection)));
            blockposition_mutableblockposition.move(enumdirection);
        }

        this.decorateLogs(generatoraccessseed, randomsource, set, fallentreeconfiguration.logDecorators);
    }

    private boolean mayPlaceOn(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        return WorldGenTrees.validTreePos(generatoraccess, blockposition) && this.isOverSolidGround(generatoraccess, blockposition);
    }

    private boolean isOverSolidGround(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        return generatoraccess.getBlockState(blockposition.below()).isFaceSturdy(generatoraccess, blockposition, EnumDirection.UP);
    }

    private BlockPosition placeLogBlock(FallenTreeConfiguration fallentreeconfiguration, GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, BlockPosition.MutableBlockPosition blockposition_mutableblockposition, Function<IBlockData, IBlockData> function) {
        generatoraccessseed.setBlock(blockposition_mutableblockposition, (IBlockData) function.apply(fallentreeconfiguration.trunkProvider.getState(randomsource, blockposition_mutableblockposition)), 3);
        this.markAboveForPostProcessing(generatoraccessseed, blockposition_mutableblockposition);
        return blockposition_mutableblockposition.immutable();
    }

    private void decorateLogs(GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, Set<BlockPosition> set, List<WorldGenFeatureTree> list) {
        if (!list.isEmpty()) {
            WorldGenFeatureTree.a worldgenfeaturetree_a = new WorldGenFeatureTree.a(generatoraccessseed, this.getDecorationSetter(generatoraccessseed), randomsource, set, Set.of(), Set.of());

            list.forEach((worldgenfeaturetree) -> {
                worldgenfeaturetree.place(worldgenfeaturetree_a);
            });
        }

    }

    private BiConsumer<BlockPosition, IBlockData> getDecorationSetter(GeneratorAccessSeed generatoraccessseed) {
        return (blockposition, iblockdata) -> {
            generatoraccessseed.setBlock(blockposition, iblockdata, 19);
        };
    }

    private static Function<IBlockData, IBlockData> getSidewaysStateModifier(EnumDirection enumdirection) {
        return (iblockdata) -> {
            return (IBlockData) iblockdata.trySetValue(BlockRotatable.AXIS, enumdirection.getAxis());
        };
    }
}
