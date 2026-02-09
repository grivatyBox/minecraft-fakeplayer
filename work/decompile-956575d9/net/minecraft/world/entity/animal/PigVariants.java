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

public class PigVariants {

    public static final ResourceKey<PigVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
    public static final ResourceKey<PigVariant> WARM = createKey(TemperatureVariants.WARM);
    public static final ResourceKey<PigVariant> COLD = createKey(TemperatureVariants.COLD);
    public static final ResourceKey<PigVariant> DEFAULT = PigVariants.TEMPERATE;

    public PigVariants() {}

    private static ResourceKey<PigVariant> createKey(MinecraftKey minecraftkey) {
        return ResourceKey.create(Registries.PIG_VARIANT, minecraftkey);
    }

    public static void bootstrap(BootstrapContext<PigVariant> bootstrapcontext) {
        register(bootstrapcontext, PigVariants.TEMPERATE, PigVariant.a.NORMAL, "temperate_pig", SpawnPrioritySelectors.fallback(0));
        register(bootstrapcontext, PigVariants.WARM, PigVariant.a.NORMAL, "warm_pig", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
        register(bootstrapcontext, PigVariants.COLD, PigVariant.a.COLD, "cold_pig", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
    }

    private static void register(BootstrapContext<PigVariant> bootstrapcontext, ResourceKey<PigVariant> resourcekey, PigVariant.a pigvariant_a, String s, TagKey<BiomeBase> tagkey) {
        HolderSet<BiomeBase> holderset = bootstrapcontext.lookup(Registries.BIOME).getOrThrow(tagkey);

        register(bootstrapcontext, resourcekey, pigvariant_a, s, SpawnPrioritySelectors.single(new BiomeCheck(holderset), 1));
    }

    private static void register(BootstrapContext<PigVariant> bootstrapcontext, ResourceKey<PigVariant> resourcekey, PigVariant.a pigvariant_a, String s, SpawnPrioritySelectors spawnpriorityselectors) {
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace("entity/pig/" + s);

        bootstrapcontext.register(resourcekey, new PigVariant(new ModelAndTexture(pigvariant_a, minecraftkey), spawnpriorityselectors));
    }

    public static Optional<Holder.c<PigVariant>> selectVariantToSpawn(RandomSource randomsource, IRegistryCustom iregistrycustom, SpawnContext spawncontext) {
        return PriorityProvider.pick(iregistrycustom.lookupOrThrow(Registries.PIG_VARIANT).listElements(), Holder::value, randomsource, spawncontext);
    }
}
