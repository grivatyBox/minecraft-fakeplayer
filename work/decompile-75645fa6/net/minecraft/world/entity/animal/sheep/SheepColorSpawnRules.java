package net.minecraft.world.entity.animal.sheep;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.biome.BiomeBase;

public class SheepColorSpawnRules {

    private static final SheepColorSpawnRules.b TEMPERATE_SPAWN_CONFIGURATION = new SheepColorSpawnRules.b(weighted(builder().add(single(EnumColor.BLACK), 5).add(single(EnumColor.GRAY), 5).add(single(EnumColor.LIGHT_GRAY), 5).add(single(EnumColor.BROWN), 3).add(commonColors(EnumColor.WHITE), 82).build()));
    private static final SheepColorSpawnRules.b WARM_SPAWN_CONFIGURATION = new SheepColorSpawnRules.b(weighted(builder().add(single(EnumColor.GRAY), 5).add(single(EnumColor.LIGHT_GRAY), 5).add(single(EnumColor.WHITE), 5).add(single(EnumColor.BLACK), 3).add(commonColors(EnumColor.BROWN), 82).build()));
    private static final SheepColorSpawnRules.b COLD_SPAWN_CONFIGURATION = new SheepColorSpawnRules.b(weighted(builder().add(single(EnumColor.LIGHT_GRAY), 5).add(single(EnumColor.GRAY), 5).add(single(EnumColor.WHITE), 5).add(single(EnumColor.BROWN), 3).add(commonColors(EnumColor.BLACK), 82).build()));

    public SheepColorSpawnRules() {}

    private static SheepColorSpawnRules.a commonColors(EnumColor enumcolor) {
        return weighted(builder().add(single(enumcolor), 499).add(single(EnumColor.PINK), 1).build());
    }

    public static EnumColor getSheepColor(Holder<BiomeBase> holder, RandomSource randomsource) {
        SheepColorSpawnRules.b sheepcolorspawnrules_b = getSheepColorConfiguration(holder);

        return sheepcolorspawnrules_b.colors().get(randomsource);
    }

    private static SheepColorSpawnRules.b getSheepColorConfiguration(Holder<BiomeBase> holder) {
        return holder.is(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS) ? SheepColorSpawnRules.WARM_SPAWN_CONFIGURATION : (holder.is(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS) ? SheepColorSpawnRules.COLD_SPAWN_CONFIGURATION : SheepColorSpawnRules.TEMPERATE_SPAWN_CONFIGURATION);
    }

    private static SheepColorSpawnRules.a weighted(WeightedList<SheepColorSpawnRules.a> weightedlist) {
        if (weightedlist.isEmpty()) {
            throw new IllegalArgumentException("List must be non-empty");
        } else {
            return (randomsource) -> {
                return ((SheepColorSpawnRules.a) weightedlist.getRandomOrThrow(randomsource)).get(randomsource);
            };
        }
    }

    private static SheepColorSpawnRules.a single(EnumColor enumcolor) {
        return (randomsource) -> {
            return enumcolor;
        };
    }

    private static WeightedList.a<SheepColorSpawnRules.a> builder() {
        return WeightedList.<SheepColorSpawnRules.a>builder();
    }

    private static record b(SheepColorSpawnRules.a colors) {

    }

    @FunctionalInterface
    private interface a {

        EnumColor get(RandomSource randomsource);
    }
}
