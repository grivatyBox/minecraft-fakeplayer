package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

record RandomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> groups) implements PoolAliasBinding {

    static Codec<RandomGroup> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(SimpleWeightedRandomList.wrappedCodec(Codec.list(PoolAliasBinding.CODEC)).fieldOf("groups").forGetter(RandomGroup::groups)).apply(instance, RandomGroup::new);
    });

    @Override
    public void forEachResolved(RandomSource randomsource, BiConsumer<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> biconsumer) {
        this.groups.getRandom(randomsource).ifPresent((weightedentry_b) -> {
            ((List) weightedentry_b.getData()).forEach((poolaliasbinding) -> {
                poolaliasbinding.forEachResolved(randomsource, biconsumer);
            });
        });
    }

    @Override
    public Stream<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> allTargets() {
        return this.groups.unwrap().stream().flatMap((weightedentry_b) -> {
            return ((List) weightedentry_b.getData()).stream();
        }).flatMap(PoolAliasBinding::allTargets);
    }

    @Override
    public Codec<RandomGroup> codec() {
        return RandomGroup.CODEC;
    }
}
