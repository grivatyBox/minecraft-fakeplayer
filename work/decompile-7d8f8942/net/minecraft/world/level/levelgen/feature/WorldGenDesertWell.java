package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;
import net.minecraft.world.level.storage.loot.LootTables;

public class WorldGenDesertWell extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final IBlockData sand;
    private final IBlockData sandSlab;
    private final IBlockData sandstone;
    private final IBlockData water;

    public WorldGenDesertWell(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
        this.sand = Blocks.SAND.defaultBlockState();
        this.sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
        this.sandstone = Blocks.SANDSTONE.defaultBlockState();
        this.water = Blocks.WATER.defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        BlockPosition blockposition = featureplacecontext.origin();

        for (blockposition = blockposition.above(); generatoraccessseed.isEmptyBlock(blockposition) && blockposition.getY() > generatoraccessseed.getMinY() + 2; blockposition = blockposition.below()) {
            ;
        }

        if (!WorldGenDesertWell.IS_SAND.test(generatoraccessseed.getBlockState(blockposition))) {
            return false;
        } else {
            for (int i = -2; i <= 2; ++i) {
                for (int j = -2; j <= 2; ++j) {
                    if (generatoraccessseed.isEmptyBlock(blockposition.offset(i, -1, j)) && generatoraccessseed.isEmptyBlock(blockposition.offset(i, -2, j))) {
                        return false;
                    }
                }
            }

            for (int k = -2; k <= 0; ++k) {
                for (int l = -2; l <= 2; ++l) {
                    for (int i1 = -2; i1 <= 2; ++i1) {
                        generatoraccessseed.setBlock(blockposition.offset(l, k, i1), this.sandstone, 2);
                    }
                }
            }

            generatoraccessseed.setBlock(blockposition, this.water, 2);

            for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                generatoraccessseed.setBlock(blockposition.relative(enumdirection), this.water, 2);
            }

            BlockPosition blockposition1 = blockposition.below();

            generatoraccessseed.setBlock(blockposition1, this.sand, 2);

            for (EnumDirection enumdirection1 : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                generatoraccessseed.setBlock(blockposition1.relative(enumdirection1), this.sand, 2);
            }

            for (int j1 = -2; j1 <= 2; ++j1) {
                for (int k1 = -2; k1 <= 2; ++k1) {
                    if (j1 == -2 || j1 == 2 || k1 == -2 || k1 == 2) {
                        generatoraccessseed.setBlock(blockposition.offset(j1, 1, k1), this.sandstone, 2);
                    }
                }
            }

            generatoraccessseed.setBlock(blockposition.offset(2, 1, 0), this.sandSlab, 2);
            generatoraccessseed.setBlock(blockposition.offset(-2, 1, 0), this.sandSlab, 2);
            generatoraccessseed.setBlock(blockposition.offset(0, 1, 2), this.sandSlab, 2);
            generatoraccessseed.setBlock(blockposition.offset(0, 1, -2), this.sandSlab, 2);

            for (int l1 = -1; l1 <= 1; ++l1) {
                for (int i2 = -1; i2 <= 1; ++i2) {
                    if (l1 == 0 && i2 == 0) {
                        generatoraccessseed.setBlock(blockposition.offset(l1, 4, i2), this.sandstone, 2);
                    } else {
                        generatoraccessseed.setBlock(blockposition.offset(l1, 4, i2), this.sandSlab, 2);
                    }
                }
            }

            for (int j2 = 1; j2 <= 3; ++j2) {
                generatoraccessseed.setBlock(blockposition.offset(-1, j2, -1), this.sandstone, 2);
                generatoraccessseed.setBlock(blockposition.offset(-1, j2, 1), this.sandstone, 2);
                generatoraccessseed.setBlock(blockposition.offset(1, j2, -1), this.sandstone, 2);
                generatoraccessseed.setBlock(blockposition.offset(1, j2, 1), this.sandstone, 2);
            }

            List<BlockPosition> list = List.of(blockposition, blockposition.east(), blockposition.south(), blockposition.west(), blockposition.north());
            RandomSource randomsource = featureplacecontext.random();

            placeSusSand(generatoraccessseed, ((BlockPosition) SystemUtils.getRandom(list, randomsource)).below(1));
            placeSusSand(generatoraccessseed, ((BlockPosition) SystemUtils.getRandom(list, randomsource)).below(2));
            return true;
        }
    }

    private static void placeSusSand(GeneratorAccessSeed generatoraccessseed, BlockPosition blockposition) {
        generatoraccessseed.setBlock(blockposition, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 3);
        generatoraccessseed.getBlockEntity(blockposition, TileEntityTypes.BRUSHABLE_BLOCK).ifPresent((brushableblockentity) -> {
            brushableblockentity.setLootTable(LootTables.DESERT_WELL_ARCHAEOLOGY, blockposition.asLong());
        });
    }
}
