package net.minecraft.data.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WinterDropBiomes {

    public static final ResourceKey<BiomeBase> PALE_GARDEN = createKey("pale_garden");

    public WinterDropBiomes() {}

    public static ResourceKey<BiomeBase> createKey(String s) {
        return ResourceKey.create(Registries.BIOME, MinecraftKey.withDefaultNamespace(s));
    }

    public static void register(BootstrapContext<BiomeBase> bootstrapcontext, String s, BiomeBase biomebase) {
        bootstrapcontext.register(createKey(s), biomebase);
    }

    public static void bootstrap(BootstrapContext<BiomeBase> bootstrapcontext) {
        HolderGetter<PlacedFeature> holdergetter = bootstrapcontext.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldGenCarverWrapper<?>> holdergetter1 = bootstrapcontext.lookup(Registries.CONFIGURED_CARVER);

        bootstrapcontext.register(WinterDropBiomes.PALE_GARDEN, OverworldBiomes.darkForest(holdergetter, holdergetter1, true));
    }
}
