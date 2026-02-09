package net.minecraft.world.entity.animal;

import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.variant.MoonBrightnessCheck;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.entity.variant.StructureCheck;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface CatVariants {

    ResourceKey<CatVariant> TABBY = createKey("tabby");
    ResourceKey<CatVariant> BLACK = createKey("black");
    ResourceKey<CatVariant> RED = createKey("red");
    ResourceKey<CatVariant> SIAMESE = createKey("siamese");
    ResourceKey<CatVariant> BRITISH_SHORTHAIR = createKey("british_shorthair");
    ResourceKey<CatVariant> CALICO = createKey("calico");
    ResourceKey<CatVariant> PERSIAN = createKey("persian");
    ResourceKey<CatVariant> RAGDOLL = createKey("ragdoll");
    ResourceKey<CatVariant> WHITE = createKey("white");
    ResourceKey<CatVariant> JELLIE = createKey("jellie");
    ResourceKey<CatVariant> ALL_BLACK = createKey("all_black");

    private static ResourceKey<CatVariant> createKey(String s) {
        return ResourceKey.create(Registries.CAT_VARIANT, MinecraftKey.withDefaultNamespace(s));
    }

    static void bootstrap(BootstrapContext<CatVariant> bootstrapcontext) {
        HolderGetter<Structure> holdergetter = bootstrapcontext.<Structure>lookup(Registries.STRUCTURE);

        registerForAnyConditions(bootstrapcontext, CatVariants.TABBY, "entity/cat/tabby");
        registerForAnyConditions(bootstrapcontext, CatVariants.BLACK, "entity/cat/black");
        registerForAnyConditions(bootstrapcontext, CatVariants.RED, "entity/cat/red");
        registerForAnyConditions(bootstrapcontext, CatVariants.SIAMESE, "entity/cat/siamese");
        registerForAnyConditions(bootstrapcontext, CatVariants.BRITISH_SHORTHAIR, "entity/cat/british_shorthair");
        registerForAnyConditions(bootstrapcontext, CatVariants.CALICO, "entity/cat/calico");
        registerForAnyConditions(bootstrapcontext, CatVariants.PERSIAN, "entity/cat/persian");
        registerForAnyConditions(bootstrapcontext, CatVariants.RAGDOLL, "entity/cat/ragdoll");
        registerForAnyConditions(bootstrapcontext, CatVariants.WHITE, "entity/cat/white");
        registerForAnyConditions(bootstrapcontext, CatVariants.JELLIE, "entity/cat/jellie");
        register(bootstrapcontext, CatVariants.ALL_BLACK, "entity/cat/all_black", new SpawnPrioritySelectors(List.of(new PriorityProvider.a(new StructureCheck(holdergetter.getOrThrow(StructureTags.CATS_SPAWN_AS_BLACK)), 1), new PriorityProvider.a(new MoonBrightnessCheck(CriterionConditionValue.DoubleRange.atLeast(0.9D)), 0))));
    }

    private static void registerForAnyConditions(BootstrapContext<CatVariant> bootstrapcontext, ResourceKey<CatVariant> resourcekey, String s) {
        register(bootstrapcontext, resourcekey, s, SpawnPrioritySelectors.fallback(0));
    }

    private static void register(BootstrapContext<CatVariant> bootstrapcontext, ResourceKey<CatVariant> resourcekey, String s, SpawnPrioritySelectors spawnpriorityselectors) {
        bootstrapcontext.register(resourcekey, new CatVariant(new ClientAsset(MinecraftKey.withDefaultNamespace(s)), spawnpriorityselectors));
    }

    static Optional<Holder.c<CatVariant>> selectVariantToSpawn(RandomSource randomsource, IRegistryCustom iregistrycustom, SpawnContext spawncontext) {
        return PriorityProvider.pick(iregistrycustom.lookupOrThrow(Registries.CAT_VARIANT).listElements(), Holder::value, randomsource, spawncontext);
    }
}
