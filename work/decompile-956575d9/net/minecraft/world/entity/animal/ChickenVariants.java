package net.minecraft.world.entity.animal;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.BiomeBase;

public class ChickenVariants {

    public static final ResourceKey<ChickenVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
    public static final ResourceKey<ChickenVariant> WARM = createKey(TemperatureVariants.WARM);
    public static final ResourceKey<ChickenVariant> COLD = createKey(TemperatureVariants.COLD);
    public static final ResourceKey<ChickenVariant> DEFAULT = ChickenVariants.TEMPERATE;

    public ChickenVariants() {}

    private static ResourceKey<ChickenVariant> createKey(MinecraftKey minecraftkey) {
        return ResourceKey.create(Registries.CHICKEN_VARIANT, minecraftkey);
    }

    public static void bootstrap(BootstrapContext<ChickenVariant> bootstrapcontext) {
        register(bootstrapcontext, ChickenVariants.TEMPERATE, ChickenVariant.a.NORMAL, "temperate_chicken", SpawnPrioritySelectors.fallback(0));
        register(bootstrapcontext, ChickenVariants.WARM, ChickenVariant.a.NORMAL, "warm_chicken", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
        register(bootstrapcontext, ChickenVariants.COLD, ChickenVariant.a.COLD, "cold_chicken", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
    }

    private static void register(BootstrapContext<ChickenVariant> bootstrapcontext, ResourceKey<ChickenVariant> resourcekey, ChickenVariant.a chickenvariant_a, String s, TagKey<BiomeBase> tagkey) {
        HolderSet<BiomeBase> holderset = bootstrapcontext.lookup(Registries.BIOME).getOrThrow(tagkey);

        register(bootstrapcontext, resourcekey, chickenvariant_a, s, SpawnPrioritySelectors.single(new BiomeCheck(holderset), 1));
    }

    private static void register(BootstrapContext<ChickenVariant> bootstrapcontext, ResourceKey<ChickenVariant> resourcekey, ChickenVariant.a chickenvariant_a, String s, SpawnPrioritySelectors spawnpriorityselectors) {
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace("entity/chicken/" + s);

        bootstrapcontext.register(resourcekey, new ChickenVariant(new ModelAndTexture(chickenvariant_a, minecraftkey), spawnpriorityselectors));
    }

    public static Optional<Holder.c<ChickenVariant>> selectVariantToSpawn(RandomSource randomsource, IRegistryCustom iregistrycustom, SpawnContext spawncontext) {
        return PriorityProvider.pick(iregistrycustom.lookupOrThrow(Registries.CHICKEN_VARIANT).listElements(), Holder::value, randomsource, spawncontext);
    }
}
