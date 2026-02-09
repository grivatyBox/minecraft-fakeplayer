package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public record RandomPoolAlias(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> alias, WeightedList<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> targets) implements PoolAliasBinding {

    static MapCodec<RandomPoolAlias> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("alias").forGetter(RandomPoolAlias::alias), WeightedList.nonEmptyCodec(ResourceKey.codec(Registries.TEMPLATE_POOL)).fieldOf("targets").forGetter(RandomPoolAlias::targets)).apply(instance, RandomPoolAlias::new);
    });

    @Override
    public void forEachResolved(RandomSource randomsource, BiConsumer<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> biconsumer) {
        this.targets.getRandom(randomsource).ifPresent((resourcekey) -> {
            biconsumer.accept(this.alias, resourcekey);
        });
    }

    @Override
    public Stream<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> allTargets() {
        return this.targets.unwrap().stream().map(Weighted::value);
    }

    @Override
    public MapCodec<RandomPoolAlias> codec() {
        return RandomPoolAlias.CODEC;
    }
}
