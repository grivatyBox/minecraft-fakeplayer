package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.WorldGenFeaturePieces;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public interface PoolAliasBinding {

    Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

    void forEachResolved(RandomSource randomsource, BiConsumer<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> biconsumer);

    Stream<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> allTargets();

    static DirectPoolAlias direct(String s, String s1) {
        return direct(WorldGenFeaturePieces.createKey(s), WorldGenFeaturePieces.createKey(s1));
    }

    static DirectPoolAlias direct(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey, ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey1) {
        return new DirectPoolAlias(resourcekey, resourcekey1);
    }

    static RandomPoolAlias random(String s, WeightedList<String> weightedlist) {
        WeightedList.a<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> weightedlist_a = WeightedList.<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>>builder();

        weightedlist.unwrap().forEach((weighted) -> {
            weightedlist_a.add(WorldGenFeaturePieces.createKey((String) weighted.value()), weighted.weight());
        });
        return random(WorldGenFeaturePieces.createKey(s), weightedlist_a.build());
    }

    static RandomPoolAlias random(ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey, WeightedList<ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate>> weightedlist) {
        return new RandomPoolAlias(resourcekey, weightedlist);
    }

    static RandomGroupPoolAlias randomGroup(WeightedList<List<PoolAliasBinding>> weightedlist) {
        return new RandomGroupPoolAlias(weightedlist);
    }

    MapCodec<? extends PoolAliasBinding> codec();
}
