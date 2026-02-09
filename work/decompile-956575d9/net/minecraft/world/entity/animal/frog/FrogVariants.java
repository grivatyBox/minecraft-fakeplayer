package net.minecraft.world.entity.animal.frog;

import java.util.Optional;
import net.minecraft.core.ClientAsset;
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
import net.minecraft.world.entity.animal.TemperatureVariants;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.BiomeBase;

public interface FrogVariants {

    ResourceKey<FrogVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
    ResourceKey<FrogVariant> WARM = createKey(TemperatureVariants.WARM);
    ResourceKey<FrogVariant> COLD = createKey(TemperatureVariants.COLD);

    private static ResourceKey<FrogVariant> createKey(MinecraftKey minecraftkey) {
        return ResourceKey.create(Registries.FROG_VARIANT, minecraftkey);
    }

    static void bootstrap(BootstrapContext<FrogVariant> bootstrapcontext) {
        register(bootstrapcontext, FrogVariants.TEMPERATE, "entity/frog/temperate_frog", SpawnPrioritySelectors.fallback(0));
        register(bootstrapcontext, FrogVariants.WARM, "entity/frog/warm_frog", BiomeTags.SPAWNS_WARM_VARIANT_FROGS);
        register(bootstrapcontext, FrogVariants.COLD, "entity/frog/cold_frog", BiomeTags.SPAWNS_COLD_VARIANT_FROGS);
    }

    private static void register(BootstrapContext<FrogVariant> bootstrapcontext, ResourceKey<FrogVariant> resourcekey, String s, TagKey<BiomeBase> tagkey) {
        HolderSet<BiomeBase> holderset = bootstrapcontext.lookup(Registries.BIOME).getOrThrow(tagkey);

        register(bootstrapcontext, resourcekey, s, SpawnPrioritySelectors.single(new BiomeCheck(holderset), 1));
    }

    private static void register(BootstrapContext<FrogVariant> bootstrapcontext, ResourceKey<FrogVariant> resourcekey, String s, SpawnPrioritySelectors spawnpriorityselectors) {
        bootstrapcontext.register(resourcekey, new FrogVariant(new ClientAsset(MinecraftKey.withDefaultNamespace(s)), spawnpriorityselectors));
    }

    static Optional<Holder.c<FrogVariant>> selectVariantToSpawn(RandomSource randomsource, IRegistryCustom iregistrycustom, SpawnContext spawncontext) {
        return PriorityProvider.pick(iregistrycustom.lookupOrThrow(Registries.FROG_VARIANT).listElements(), Holder::value, randomsource, spawncontext);
    }
}
