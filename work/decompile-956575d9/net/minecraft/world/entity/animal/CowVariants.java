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

public class CowVariants {

    public static final ResourceKey<CowVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
    public static final ResourceKey<CowVariant> WARM = createKey(TemperatureVariants.WARM);
    public static final ResourceKey<CowVariant> COLD = createKey(TemperatureVariants.COLD);
    public static final ResourceKey<CowVariant> DEFAULT = CowVariants.TEMPERATE;

    public CowVariants() {}

    private static ResourceKey<CowVariant> createKey(MinecraftKey minecraftkey) {
        return ResourceKey.create(Registries.COW_VARIANT, minecraftkey);
    }

    public static void bootstrap(BootstrapContext<CowVariant> bootstrapcontext) {
        register(bootstrapcontext, CowVariants.TEMPERATE, CowVariant.a.NORMAL, "temperate_cow", SpawnPrioritySelectors.fallback(0));
        register(bootstrapcontext, CowVariants.WARM, CowVariant.a.WARM, "warm_cow", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
        register(bootstrapcontext, CowVariants.COLD, CowVariant.a.COLD, "cold_cow", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
    }

    private static void register(BootstrapContext<CowVariant> bootstrapcontext, ResourceKey<CowVariant> resourcekey, CowVariant.a cowvariant_a, String s, TagKey<BiomeBase> tagkey) {
        HolderSet<BiomeBase> holderset = bootstrapcontext.lookup(Registries.BIOME).getOrThrow(tagkey);

        register(bootstrapcontext, resourcekey, cowvariant_a, s, SpawnPrioritySelectors.single(new BiomeCheck(holderset), 1));
    }

    private static void register(BootstrapContext<CowVariant> bootstrapcontext, ResourceKey<CowVariant> resourcekey, CowVariant.a cowvariant_a, String s, SpawnPrioritySelectors spawnpriorityselectors) {
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace("entity/cow/" + s);

        bootstrapcontext.register(resourcekey, new CowVariant(new ModelAndTexture(cowvariant_a, minecraftkey), spawnpriorityselectors));
    }

    public static Optional<Holder.c<CowVariant>> selectVariantToSpawn(RandomSource randomsource, IRegistryCustom iregistrycustom, SpawnContext spawncontext) {
        return PriorityProvider.pick(iregistrycustom.lookupOrThrow(Registries.COW_VARIANT).listElements(), Holder::value, randomsource, spawncontext);
    }
}
