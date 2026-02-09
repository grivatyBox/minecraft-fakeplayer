package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.levelgen.feature.WorldGenTrees;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;

public class PlaceOnGroundDecorator extends WorldGenFeatureTree {

    public static final MapCodec<PlaceOnGroundDecorator> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter((placeongrounddecorator) -> {
            return placeongrounddecorator.tries;
        }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").orElse(2).forGetter((placeongrounddecorator) -> {
            return placeongrounddecorator.radius;
        }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("height").orElse(1).forGetter((placeongrounddecorator) -> {
            return placeongrounddecorator.height;
        }), WorldGenFeatureStateProvider.CODEC.fieldOf("block_state_provider").forGetter((placeongrounddecorator) -> {
            return placeongrounddecorator.blockStateProvider;
        })).apply(instance, PlaceOnGroundDecorator::new);
    });
    private final int tries;
    private final int radius;
    private final int height;
    private final WorldGenFeatureStateProvider blockStateProvider;

    public PlaceOnGroundDecorator(int i, int j, int k, WorldGenFeatureStateProvider worldgenfeaturestateprovider) {
        this.tries = i;
        this.radius = j;
        this.height = k;
        this.blockStateProvider = worldgenfeaturestateprovider;
    }

    @Override
    protected WorldGenFeatureTrees<?> type() {
        return WorldGenFeatureTrees.PLACE_ON_GROUND;
    }

    @Override
    public void place(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        List<BlockPosition> list = WorldGenTrees.getLowestTrunkOrRootOfTree(worldgenfeaturetree_a);

        if (!list.isEmpty()) {
            BlockPosition blockposition = (BlockPosition) list.getFirst();
            int i = blockposition.getY();
            int j = blockposition.getX();
            int k = blockposition.getX();
            int l = blockposition.getZ();
            int i1 = blockposition.getZ();

            for (BlockPosition blockposition1 : list) {
                if (blockposition1.getY() == i) {
                    j = Math.min(j, blockposition1.getX());
                    k = Math.max(k, blockposition1.getX());
                    l = Math.min(l, blockposition1.getZ());
                    i1 = Math.max(i1, blockposition1.getZ());
                }
            }

            RandomSource randomsource = worldgenfeaturetree_a.random();
            StructureBoundingBox structureboundingbox = (new StructureBoundingBox(j, i, l, k, i, i1)).inflatedBy(this.radius, this.height, this.radius);
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();

            for (int j1 = 0; j1 < this.tries; ++j1) {
                blockposition_mutableblockposition.set(randomsource.nextIntBetweenInclusive(structureboundingbox.minX(), structureboundingbox.maxX()), randomsource.nextIntBetweenInclusive(structureboundingbox.minY(), structureboundingbox.maxY()), randomsource.nextIntBetweenInclusive(structureboundingbox.minZ(), structureboundingbox.maxZ()));
                this.attemptToPlaceBlockAbove(worldgenfeaturetree_a, blockposition_mutableblockposition);
            }

        }
    }

    private void attemptToPlaceBlockAbove(WorldGenFeatureTree.a worldgenfeaturetree_a, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.above();

        if (worldgenfeaturetree_a.level().isStateAtPosition(blockposition1, (iblockdata) -> {
            return iblockdata.isAir() || iblockdata.is(Blocks.VINE);
        }) && worldgenfeaturetree_a.checkBlock(blockposition, BlockBase.BlockData::isSolidRender)) {
            worldgenfeaturetree_a.setBlock(blockposition1, this.blockStateProvider.getState(worldgenfeaturetree_a.random(), blockposition1));
        }

    }
}
