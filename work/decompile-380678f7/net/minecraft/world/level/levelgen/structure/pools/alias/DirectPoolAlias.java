package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public record DirectPoolAlias(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> alias, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> target) implements PoolAliasBinding {

    static MapCodec<DirectPoolAlias> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("alias").forGetter(DirectPoolAlias::alias), ResourceKey.codec(Registries.TEMPLATE_POOL).fieldOf("target").forGetter(DirectPoolAlias::target)).apply(instance, DirectPoolAlias::new);
    });

    @Override
    public void forEachResolved(RandomSource randomsource, BiConsumer<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> biconsumer) {
        biconsumer.accept(this.alias, this.target);
    }

    @Override
    public Stream<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> allTargets() {
        return Stream.of(this.target);
    }

    @Override
    public MapCodec<DirectPoolAlias> codec() {
        return DirectPoolAlias.CODEC;
    }
}
