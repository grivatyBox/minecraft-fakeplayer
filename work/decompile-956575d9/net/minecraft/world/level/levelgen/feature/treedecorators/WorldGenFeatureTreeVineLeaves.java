package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BlockVine;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;

public class WorldGenFeatureTreeVineLeaves extends WorldGenFeatureTree {

    public static final MapCodec<WorldGenFeatureTreeVineLeaves> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(WorldGenFeatureTreeVineLeaves::new, (worldgenfeaturetreevineleaves) -> {
        return worldgenfeaturetreevineleaves.probability;
    });
    private final float probability;

    @Override
    protected WorldGenFeatureTrees<?> type() {
        return WorldGenFeatureTrees.LEAVE_VINE;
    }

    public WorldGenFeatureTreeVineLeaves(float f) {
        this.probability = f;
    }

    @Override
    public void place(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        RandomSource randomsource = worldgenfeaturetree_a.random();

        worldgenfeaturetree_a.leaves().forEach((blockposition) -> {
            if (randomsource.nextFloat() < this.probability) {
                BlockPosition blockposition1 = blockposition.west();

                if (worldgenfeaturetree_a.isAir(blockposition1)) {
                    addHangingVine(blockposition1, BlockVine.EAST, worldgenfeaturetree_a);
                }
            }

            if (randomsource.nextFloat() < this.probability) {
                BlockPosition blockposition2 = blockposition.east();

                if (worldgenfeaturetree_a.isAir(blockposition2)) {
                    addHangingVine(blockposition2, BlockVine.WEST, worldgenfeaturetree_a);
                }
            }

            if (randomsource.nextFloat() < this.probability) {
                BlockPosition blockposition3 = blockposition.north();

                if (worldgenfeaturetree_a.isAir(blockposition3)) {
                    addHangingVine(blockposition3, BlockVine.SOUTH, worldgenfeaturetree_a);
                }
            }

            if (randomsource.nextFloat() < this.probability) {
                BlockPosition blockposition4 = blockposition.south();

                if (worldgenfeaturetree_a.isAir(blockposition4)) {
                    addHangingVine(blockposition4, BlockVine.NORTH, worldgenfeaturetree_a);
                }
            }

        });
    }

    private static void addHangingVine(BlockPosition blockposition, BlockStateBoolean blockstateboolean, WorldGenFeatureTree.a worldgenfeaturetree_a) {
        worldgenfeaturetree_a.placeVine(blockposition, blockstateboolean);
        int i = 4;

        for (BlockPosition blockposition1 = blockposition.below(); worldgenfeaturetree_a.isAir(blockposition1) && i > 0; --i) {
            worldgenfeaturetree_a.placeVine(blockposition1, blockstateboolean);
            blockposition1 = blockposition1.below();
        }

    }
}
