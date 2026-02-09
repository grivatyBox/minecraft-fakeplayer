package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPosition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

@FunctionalInterface
public interface PoolAliasLookup {

    PoolAliasLookup EMPTY = (resourcekey) -> {
        return resourcekey;
    };

    ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> lookup(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey);

    static PoolAliasLookup create(List<PoolAliasBinding> list, BlockPosition blockposition, long i) {
        if (list.isEmpty()) {
            return PoolAliasLookup.EMPTY;
        } else {
            RandomSource randomsource = RandomSource.create(i).forkPositional().at(blockposition);
            ImmutableMap.Builder<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> immutablemap_builder = ImmutableMap.builder();

            list.forEach((poolaliasbinding) -> {
                Objects.requireNonNull(immutablemap_builder);
                poolaliasbinding.forEachResolved(randomsource, immutablemap_builder::put);
            });
            Map<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> map = immutablemap_builder.build();

            return (resourcekey) -> {
                return (ResourceKey) Objects.requireNonNull((ResourceKey) map.getOrDefault(resourcekey, resourcekey), () -> {
                    return "alias " + String.valueOf(resourcekey.location()) + " was mapped to null value";
                });
            };
        }
    }
}
