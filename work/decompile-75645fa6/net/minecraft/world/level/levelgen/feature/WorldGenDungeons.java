package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.LootTables;
import org.slf4j.Logger;

public class WorldGenDungeons extends WorldGenerator<WorldGenFeatureEmptyConfiguration> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityTypes<?>[] MOBS = new EntityTypes[]{EntityTypes.SKELETON, EntityTypes.ZOMBIE, EntityTypes.ZOMBIE, EntityTypes.SPIDER};
    private static final IBlockData AIR = Blocks.CAVE_AIR.defaultBlockState();

    public WorldGenDungeons(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureEmptyConfiguration> featureplacecontext) {
        Predicate<IBlockData> predicate = WorldGenerator.isReplaceable(TagsBlock.FEATURES_CANNOT_REPLACE);
        BlockPosition blockposition = featureplacecontext.origin();
        RandomSource randomsource = featureplacecontext.random();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        int i = 3;
        int j = randomsource.nextInt(2) + 2;
        int k = -j - 1;
        int l = j + 1;
        int i1 = -1;
        int j1 = 4;
        int k1 = randomsource.nextInt(2) + 2;
        int l1 = -k1 - 1;
        int i2 = k1 + 1;
        int j2 = 0;

        for (int k2 = k; k2 <= l; ++k2) {
            for (int l2 = -1; l2 <= 4; ++l2) {
                for (int i3 = l1; i3 <= i2; ++i3) {
                    BlockPosition blockposition1 = blockposition.offset(k2, l2, i3);
                    boolean flag = generatoraccessseed.getBlockState(blockposition1).isSolid();

                    if (l2 == -1 && !flag) {
                        return false;
                    }

                    if (l2 == 4 && !flag) {
                        return false;
                    }

                    if ((k2 == k || k2 == l || i3 == l1 || i3 == i2) && l2 == 0 && generatoraccessseed.isEmptyBlock(blockposition1) && generatoraccessseed.isEmptyBlock(blockposition1.above())) {
                        ++j2;
                    }
                }
            }
        }

        if (j2 >= 1 && j2 <= 5) {
            for (int j3 = k; j3 <= l; ++j3) {
                for (int k3 = 3; k3 >= -1; --k3) {
                    for (int l3 = l1; l3 <= i2; ++l3) {
                        BlockPosition blockposition2 = blockposition.offset(j3, k3, l3);
                        IBlockData iblockdata = generatoraccessseed.getBlockState(blockposition2);

                        if (j3 != k && k3 != -1 && l3 != l1 && j3 != l && k3 != 4 && l3 != i2) {
                            if (!iblockdata.is(Blocks.CHEST) && !iblockdata.is(Blocks.SPAWNER)) {
                                this.safeSetBlock(generatoraccessseed, blockposition2, WorldGenDungeons.AIR, predicate);
                            }
                        } else if (blockposition2.getY() >= generatoraccessseed.getMinY() && !generatoraccessseed.getBlockState(blockposition2.below()).isSolid()) {
                            generatoraccessseed.setBlock(blockposition2, WorldGenDungeons.AIR, 2);
                        } else if (iblockdata.isSolid() && !iblockdata.is(Blocks.CHEST)) {
                            if (k3 == -1 && randomsource.nextInt(4) != 0) {
                                this.safeSetBlock(generatoraccessseed, blockposition2, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), predicate);
                            } else {
                                this.safeSetBlock(generatoraccessseed, blockposition2, Blocks.COBBLESTONE.defaultBlockState(), predicate);
                            }
                        }
                    }
                }
            }

            for (int i4 = 0; i4 < 2; ++i4) {
                for (int j4 = 0; j4 < 3; ++j4) {
                    int k4 = blockposition.getX() + randomsource.nextInt(j * 2 + 1) - j;
                    int l4 = blockposition.getY();
                    int i5 = blockposition.getZ() + randomsource.nextInt(k1 * 2 + 1) - k1;
                    BlockPosition blockposition3 = new BlockPosition(k4, l4, i5);

                    if (generatoraccessseed.isEmptyBlock(blockposition3)) {
                        int j5 = 0;

                        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                            if (generatoraccessseed.getBlockState(blockposition3.relative(enumdirection)).isSolid()) {
                                ++j5;
                            }
                        }

                        if (j5 == 1) {
                            this.safeSetBlock(generatoraccessseed, blockposition3, StructurePiece.reorient(generatoraccessseed, blockposition3, Blocks.CHEST.defaultBlockState()), predicate);
                            RandomizableContainer.setBlockEntityLootTable(generatoraccessseed, randomsource, blockposition3, LootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                }
            }

            this.safeSetBlock(generatoraccessseed, blockposition, Blocks.SPAWNER.defaultBlockState(), predicate);
            TileEntity tileentity = generatoraccessseed.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityMobSpawner) {
                TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) tileentity;

                tileentitymobspawner.setEntityId(this.randomEntityId(randomsource), randomsource);
            } else {
                WorldGenDungeons.LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", new Object[]{blockposition.getX(), blockposition.getY(), blockposition.getZ()});
            }

            return true;
        } else {
            return false;
        }
    }

    private EntityTypes<?> randomEntityId(RandomSource randomsource) {
        return (EntityTypes) SystemUtils.getRandom(WorldGenDungeons.MOBS, randomsource);
    }
}
