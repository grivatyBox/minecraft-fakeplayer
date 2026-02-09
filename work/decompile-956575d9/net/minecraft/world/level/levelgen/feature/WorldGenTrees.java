package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.IWorldWriter;
import net.minecraft.world.level.VirtualLevelReadable;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.WorldGenFoilagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.WorldGenFeatureTree;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.phys.shapes.VoxelShapeBitSet;
import net.minecraft.world.phys.shapes.VoxelShapeDiscrete;

public class WorldGenTrees extends WorldGenerator<WorldGenFeatureTreeConfiguration> {

    private static final int BLOCK_UPDATE_FLAGS = 19;

    public WorldGenTrees(Codec<WorldGenFeatureTreeConfiguration> codec) {
        super(codec);
    }

    public static boolean isVine(VirtualLevelReadable virtuallevelreadable, BlockPosition blockposition) {
        return virtuallevelreadable.isStateAtPosition(blockposition, (iblockdata) -> {
            return iblockdata.is(Blocks.VINE);
        });
    }

    public static boolean isAirOrLeaves(VirtualLevelReadable virtuallevelreadable, BlockPosition blockposition) {
        return virtuallevelreadable.isStateAtPosition(blockposition, (iblockdata) -> {
            return iblockdata.isAir() || iblockdata.is(TagsBlock.LEAVES);
        });
    }

    private static void setBlockKnownShape(IWorldWriter iworldwriter, BlockPosition blockposition, IBlockData iblockdata) {
        iworldwriter.setBlock(blockposition, iblockdata, 19);
    }

    public static boolean validTreePos(VirtualLevelReadable virtuallevelreadable, BlockPosition blockposition) {
        return virtuallevelreadable.isStateAtPosition(blockposition, (iblockdata) -> {
            return iblockdata.isAir() || iblockdata.is(TagsBlock.REPLACEABLE_BY_TREES);
        });
    }

    private boolean doPlace(GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, BlockPosition blockposition, BiConsumer<BlockPosition, IBlockData> biconsumer, BiConsumer<BlockPosition, IBlockData> biconsumer1, WorldGenFoilagePlacer.b worldgenfoilageplacer_b, WorldGenFeatureTreeConfiguration worldgenfeaturetreeconfiguration) {
        int i = worldgenfeaturetreeconfiguration.trunkPlacer.getTreeHeight(randomsource);
        int j = worldgenfeaturetreeconfiguration.foliagePlacer.foliageHeight(randomsource, i, worldgenfeaturetreeconfiguration);
        int k = i - j;
        int l = worldgenfeaturetreeconfiguration.foliagePlacer.foliageRadius(randomsource, k);
        BlockPosition blockposition1 = (BlockPosition) worldgenfeaturetreeconfiguration.rootPlacer.map((rootplacer) -> {
            return rootplacer.getTrunkOrigin(blockposition, randomsource);
        }).orElse(blockposition);
        int i1 = Math.min(blockposition.getY(), blockposition1.getY());
        int j1 = Math.max(blockposition.getY(), blockposition1.getY()) + i + 1;

        if (i1 >= generatoraccessseed.getMinY() + 1 && j1 <= generatoraccessseed.getMaxY() + 1) {
            OptionalInt optionalint = worldgenfeaturetreeconfiguration.minimumSize.minClippedHeight();
            int k1 = this.getMaxFreeTreeHeight(generatoraccessseed, i, blockposition1, worldgenfeaturetreeconfiguration);

            if (k1 >= i || !optionalint.isEmpty() && k1 >= optionalint.getAsInt()) {
                if (worldgenfeaturetreeconfiguration.rootPlacer.isPresent() && !((RootPlacer) worldgenfeaturetreeconfiguration.rootPlacer.get()).placeRoots(generatoraccessseed, biconsumer, randomsource, blockposition, blockposition1, worldgenfeaturetreeconfiguration)) {
                    return false;
                } else {
                    List<WorldGenFoilagePlacer.a> list = worldgenfeaturetreeconfiguration.trunkPlacer.placeTrunk(generatoraccessseed, biconsumer1, randomsource, k1, blockposition1, worldgenfeaturetreeconfiguration);

                    list.forEach((worldgenfoilageplacer_a) -> {
                        worldgenfeaturetreeconfiguration.foliagePlacer.createFoliage(generatoraccessseed, worldgenfoilageplacer_b, randomsource, worldgenfeaturetreeconfiguration, k1, worldgenfoilageplacer_a, j, l);
                    });
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int getMaxFreeTreeHeight(VirtualLevelReadable virtuallevelreadable, int i, BlockPosition blockposition, WorldGenFeatureTreeConfiguration worldgenfeaturetreeconfiguration) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

        for (int j = 0; j <= i + 1; ++j) {
            int k = worldgenfeaturetreeconfiguration.minimumSize.getSizeAtHeight(i, j);

            for (int l = -k; l <= k; ++l) {
                for (int i1 = -k; i1 <= k; ++i1) {
                    blockposition_mutableblockposition.setWithOffset(blockposition, l, j, i1);
                    if (!worldgenfeaturetreeconfiguration.trunkPlacer.isFree(virtuallevelreadable, blockposition_mutableblockposition) || !worldgenfeaturetreeconfiguration.ignoreVines && isVine(virtuallevelreadable, blockposition_mutableblockposition)) {
                        return j - 2;
                    }
                }
            }
        }

        return i;
    }

    @Override
    protected void setBlock(IWorldWriter iworldwriter, BlockPosition blockposition, IBlockData iblockdata) {
        setBlockKnownShape(iworldwriter, blockposition, iblockdata);
    }

    @Override
    public final boolean place(FeaturePlaceContext<WorldGenFeatureTreeConfiguration> featureplacecontext) {
        final GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        RandomSource randomsource = featureplacecontext.random();
        BlockPosition blockposition = featureplacecontext.origin();
        WorldGenFeatureTreeConfiguration worldgenfeaturetreeconfiguration = featureplacecontext.config();
        Set<BlockPosition> set = Sets.newHashSet();
        Set<BlockPosition> set1 = Sets.newHashSet();
        final Set<BlockPosition> set2 = Sets.newHashSet();
        Set<BlockPosition> set3 = Sets.newHashSet();
        BiConsumer<BlockPosition, IBlockData> biconsumer = (blockposition1, iblockdata) -> {
            set.add(blockposition1.immutable());
            generatoraccessseed.setBlock(blockposition1, iblockdata, 19);
        };
        BiConsumer<BlockPosition, IBlockData> biconsumer1 = (blockposition1, iblockdata) -> {
            set1.add(blockposition1.immutable());
            generatoraccessseed.setBlock(blockposition1, iblockdata, 19);
        };
        WorldGenFoilagePlacer.b worldgenfoilageplacer_b = new WorldGenFoilagePlacer.b() {
            @Override
            public void set(BlockPosition blockposition1, IBlockData iblockdata) {
                set2.add(blockposition1.immutable());
                generatoraccessseed.setBlock(blockposition1, iblockdata, 19);
            }

            @Override
            public boolean isSet(BlockPosition blockposition1) {
                return set2.contains(blockposition1);
            }
        };
        BiConsumer<BlockPosition, IBlockData> biconsumer2 = (blockposition1, iblockdata) -> {
            set3.add(blockposition1.immutable());
            generatoraccessseed.setBlock(blockposition1, iblockdata, 19);
        };
        boolean flag = this.doPlace(generatoraccessseed, randomsource, blockposition, biconsumer, biconsumer1, worldgenfoilageplacer_b, worldgenfeaturetreeconfiguration);

        if (flag && (!set1.isEmpty() || !set2.isEmpty())) {
            if (!worldgenfeaturetreeconfiguration.decorators.isEmpty()) {
                WorldGenFeatureTree.a worldgenfeaturetree_a = new WorldGenFeatureTree.a(generatoraccessseed, biconsumer2, randomsource, set1, set2, set);

                worldgenfeaturetreeconfiguration.decorators.forEach((worldgenfeaturetree) -> {
                    worldgenfeaturetree.place(worldgenfeaturetree_a);
                });
            }

            return (Boolean) StructureBoundingBox.encapsulatingPositions(Iterables.concat(set, set1, set2, set3)).map((structureboundingbox) -> {
                VoxelShapeDiscrete voxelshapediscrete = updateLeaves(generatoraccessseed, structureboundingbox, set1, set3, set);

                DefinedStructure.updateShapeAtEdge(generatoraccessseed, 3, voxelshapediscrete, structureboundingbox.minX(), structureboundingbox.minY(), structureboundingbox.minZ());
                return true;
            }).orElse(false);
        } else {
            return false;
        }
    }

    private static VoxelShapeDiscrete updateLeaves(GeneratorAccess generatoraccess, StructureBoundingBox structureboundingbox, Set<BlockPosition> set, Set<BlockPosition> set1, Set<BlockPosition> set2) {
        VoxelShapeDiscrete voxelshapediscrete = new VoxelShapeBitSet(structureboundingbox.getXSpan(), structureboundingbox.getYSpan(), structureboundingbox.getZSpan());
        int i = 7;
        List<Set<BlockPosition>> list = Lists.newArrayList();

        for (int j = 0; j < 7; ++j) {
            list.add(Sets.newHashSet());
        }

        for (BlockPosition blockposition : Lists.newArrayList(Sets.union(set1, set2))) {
            if (structureboundingbox.isInside(blockposition)) {
                voxelshapediscrete.fill(blockposition.getX() - structureboundingbox.minX(), blockposition.getY() - structureboundingbox.minY(), blockposition.getZ() - structureboundingbox.minZ());
            }
        }

        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        int k = 0;

        ((Set) list.get(0)).addAll(set);

        while (true) {
            while (k >= 7 || !((Set) ((List) list).get(k)).isEmpty()) {
                if (k >= 7) {
                    return voxelshapediscrete;
                }

                Iterator<BlockPosition> iterator = ((Set) list.get(k)).iterator();
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                iterator.remove();
                if (structureboundingbox.isInside(blockposition1)) {
                    if (k != 0) {
                        IBlockData iblockdata = generatoraccess.getBlockState(blockposition1);

                        setBlockKnownShape(generatoraccess, blockposition1, (IBlockData) iblockdata.setValue(BlockProperties.DISTANCE, k));
                    }

                    voxelshapediscrete.fill(blockposition1.getX() - structureboundingbox.minX(), blockposition1.getY() - structureboundingbox.minY(), blockposition1.getZ() - structureboundingbox.minZ());

                    for (EnumDirection enumdirection : EnumDirection.values()) {
                        blockposition_mutableblockposition.setWithOffset(blockposition1, enumdirection);
                        if (structureboundingbox.isInside(blockposition_mutableblockposition)) {
                            int l = blockposition_mutableblockposition.getX() - structureboundingbox.minX();
                            int i1 = blockposition_mutableblockposition.getY() - structureboundingbox.minY();
                            int j1 = blockposition_mutableblockposition.getZ() - structureboundingbox.minZ();

                            if (!voxelshapediscrete.isFull(l, i1, j1)) {
                                IBlockData iblockdata1 = generatoraccess.getBlockState(blockposition_mutableblockposition);
                                OptionalInt optionalint = BlockLeaves.getOptionalDistanceAt(iblockdata1);

                                if (!optionalint.isEmpty()) {
                                    int k1 = Math.min(optionalint.getAsInt(), k + 1);

                                    if (k1 < 7) {
                                        ((Set) list.get(k1)).add(blockposition_mutableblockposition.immutable());
                                        k = Math.min(k, k1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ++k;
        }
    }

    public static List<BlockPosition> getLowestTrunkOrRootOfTree(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        List<BlockPosition> list = Lists.newArrayList();
        List<BlockPosition> list1 = worldgenfeaturetree_a.roots();
        List<BlockPosition> list2 = worldgenfeaturetree_a.logs();

        if (list1.isEmpty()) {
            list.addAll(list2);
        } else if (!list2.isEmpty() && ((BlockPosition) list1.get(0)).getY() == ((BlockPosition) list2.get(0)).getY()) {
            list.addAll(list2);
            list.addAll(list1);
        } else {
            list.addAll(list1);
        }

        return list;
    }
}
