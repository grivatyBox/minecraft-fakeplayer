package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class WeightedListHeight extends HeightProvider {

    public static final MapCodec<WeightedListHeight> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeightedList.nonEmptyCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter((weightedlistheight) -> {
            return weightedlistheight.distribution;
        })).apply(instance, WeightedListHeight::new);
    });
    private final WeightedList<HeightProvider> distribution;

    public WeightedListHeight(WeightedList<HeightProvider> weightedlist) {
        this.distribution = weightedlist;
    }

    @Override
    public int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext) {
        return ((HeightProvider) this.distribution.getRandomOrThrow(randomsource)).sample(randomsource, worldgenerationcontext);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}
