package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.VirtualLevelReadable;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureTreeConfiguration;

public class WorldGenFoilagePlacerSpruce extends WorldGenFoilagePlacer {

    public static final MapCodec<WorldGenFoilagePlacerSpruce> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return foliagePlacerParts(instance).and(IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter((worldgenfoilageplacerspruce) -> {
            return worldgenfoilageplacerspruce.trunkHeight;
        })).apply(instance, WorldGenFoilagePlacerSpruce::new);
    });
    private final IntProvider trunkHeight;

    public WorldGenFoilagePlacerSpruce(IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2) {
        super(intprovider, intprovider1);
        this.trunkHeight = intprovider2;
    }

    @Override
    protected WorldGenFoilagePlacers<?> type() {
        return WorldGenFoilagePlacers.SPRUCE_FOLIAGE_PLACER;
    }

    @Override
    protected void createFoliage(VirtualLevelReadable virtuallevelreadable, WorldGenFoilagePlacer.b worldgenfoilageplacer_b, RandomSource randomsource, WorldGenFeatureTreeConfiguration worldgenfeaturetreeconfiguration, int i, WorldGenFoilagePlacer.a worldgenfoilageplacer_a, int j, int k, int l) {
        BlockPosition blockposition = worldgenfoilageplacer_a.pos();
        int i1 = randomsource.nextInt(2);
        int j1 = 1;
        int k1 = 0;

        for (int l1 = l; l1 >= -j; --l1) {
            this.placeLeavesRow(virtuallevelreadable, worldgenfoilageplacer_b, randomsource, worldgenfeaturetreeconfiguration, blockposition, i1, l1, worldgenfoilageplacer_a.doubleTrunk());
            if (i1 >= j1) {
                i1 = k1;
                k1 = 1;
                j1 = Math.min(j1 + 1, k + worldgenfoilageplacer_a.radiusOffset());
            } else {
                ++i1;
            }
        }

    }

    @Override
    public int foliageHeight(RandomSource randomsource, int i, WorldGenFeatureTreeConfiguration worldgenfeaturetreeconfiguration) {
        return Math.max(4, i - this.trunkHeight.sample(randomsource));
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomsource, int i, int j, int k, int l, boolean flag) {
        return i == l && k == l && l > 0;
    }
}
