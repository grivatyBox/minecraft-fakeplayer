package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.IBlockData;

public class WorldGenFeatureStateProviderWeighted extends WorldGenFeatureStateProvider {

    public static final MapCodec<WorldGenFeatureStateProviderWeighted> CODEC = WeightedList.nonEmptyCodec(IBlockData.CODEC).comapFlatMap(WorldGenFeatureStateProviderWeighted::create, (worldgenfeaturestateproviderweighted) -> {
        return worldgenfeaturestateproviderweighted.weightedList;
    }).fieldOf("entries");
    private final WeightedList<IBlockData> weightedList;

    private static DataResult<WorldGenFeatureStateProviderWeighted> create(WeightedList<IBlockData> weightedlist) {
        return weightedlist.isEmpty() ? DataResult.error(() -> {
            return "WeightedStateProvider with no states";
        }) : DataResult.success(new WorldGenFeatureStateProviderWeighted(weightedlist));
    }

    public WorldGenFeatureStateProviderWeighted(WeightedList<IBlockData> weightedlist) {
        this.weightedList = weightedlist;
    }

    public WorldGenFeatureStateProviderWeighted(WeightedList.a<IBlockData> weightedlist_a) {
        this(weightedlist_a.build());
    }

    @Override
    protected WorldGenFeatureStateProviders<?> type() {
        return WorldGenFeatureStateProviders.WEIGHTED_STATE_PROVIDER;
    }

    @Override
    public IBlockData getState(RandomSource randomsource, BlockPosition blockposition) {
        return this.weightedList.getRandomOrThrow(randomsource);
    }
}
