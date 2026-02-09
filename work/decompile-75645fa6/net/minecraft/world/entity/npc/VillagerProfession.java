package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.ai.village.poi.VillagePlaceType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record VillagerProfession(IChatBaseComponent name, Predicate<Holder<VillagePlaceType>> heldJobSite, Predicate<Holder<VillagePlaceType>> acquirableJobSite, ImmutableSet<Item> requestedItems, ImmutableSet<Block> secondaryPoi, @Nullable SoundEffect workSound) {

    public static final Predicate<Holder<VillagePlaceType>> ALL_ACQUIRABLE_JOBS = (holder) -> {
        return holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE);
    };
    public static final ResourceKey<VillagerProfession> NONE = createKey("none");
    public static final ResourceKey<VillagerProfession> ARMORER = createKey("armorer");
    public static final ResourceKey<VillagerProfession> BUTCHER = createKey("butcher");
    public static final ResourceKey<VillagerProfession> CARTOGRAPHER = createKey("cartographer");
    public static final ResourceKey<VillagerProfession> CLERIC = createKey("cleric");
    public static final ResourceKey<VillagerProfession> FARMER = createKey("farmer");
    public static final ResourceKey<VillagerProfession> FISHERMAN = createKey("fisherman");
    public static final ResourceKey<VillagerProfession> FLETCHER = createKey("fletcher");
    public static final ResourceKey<VillagerProfession> LEATHERWORKER = createKey("leatherworker");
    public static final ResourceKey<VillagerProfession> LIBRARIAN = createKey("librarian");
    public static final ResourceKey<VillagerProfession> MASON = createKey("mason");
    public static final ResourceKey<VillagerProfession> NITWIT = createKey("nitwit");
    public static final ResourceKey<VillagerProfession> SHEPHERD = createKey("shepherd");
    public static final ResourceKey<VillagerProfession> TOOLSMITH = createKey("toolsmith");
    public static final ResourceKey<VillagerProfession> WEAPONSMITH = createKey("weaponsmith");

    private static ResourceKey<VillagerProfession> createKey(String s) {
        return ResourceKey.create(Registries.VILLAGER_PROFESSION, MinecraftKey.withDefaultNamespace(s));
    }

    private static VillagerProfession register(IRegistry<VillagerProfession> iregistry, ResourceKey<VillagerProfession> resourcekey, ResourceKey<VillagePlaceType> resourcekey1, @Nullable SoundEffect soundeffect) {
        return register(iregistry, resourcekey, (holder) -> {
            return holder.is(resourcekey1);
        }, (holder) -> {
            return holder.is(resourcekey1);
        }, soundeffect);
    }

    private static VillagerProfession register(IRegistry<VillagerProfession> iregistry, ResourceKey<VillagerProfession> resourcekey, Predicate<Holder<VillagePlaceType>> predicate, Predicate<Holder<VillagePlaceType>> predicate1, @Nullable SoundEffect soundeffect) {
        return register(iregistry, resourcekey, predicate, predicate1, ImmutableSet.of(), ImmutableSet.of(), soundeffect);
    }

    private static VillagerProfession register(IRegistry<VillagerProfession> iregistry, ResourceKey<VillagerProfession> resourcekey, ResourceKey<VillagePlaceType> resourcekey1, ImmutableSet<Item> immutableset, ImmutableSet<Block> immutableset1, @Nullable SoundEffect soundeffect) {
        return register(iregistry, resourcekey, (holder) -> {
            return holder.is(resourcekey1);
        }, (holder) -> {
            return holder.is(resourcekey1);
        }, immutableset, immutableset1, soundeffect);
    }

    private static VillagerProfession register(IRegistry<VillagerProfession> iregistry, ResourceKey<VillagerProfession> resourcekey, Predicate<Holder<VillagePlaceType>> predicate, Predicate<Holder<VillagePlaceType>> predicate1, ImmutableSet<Item> immutableset, ImmutableSet<Block> immutableset1, @Nullable SoundEffect soundeffect) {
        String s = resourcekey.location().getNamespace();

        return (VillagerProfession) IRegistry.register(iregistry, resourcekey, new VillagerProfession(IChatBaseComponent.translatable("entity." + s + ".villager." + resourcekey.location().getPath()), predicate, predicate1, immutableset, immutableset1, soundeffect));
    }

    public static VillagerProfession bootstrap(IRegistry<VillagerProfession> iregistry) {
        register(iregistry, VillagerProfession.NONE, VillagePlaceType.NONE, VillagerProfession.ALL_ACQUIRABLE_JOBS, (SoundEffect) null);
        register(iregistry, VillagerProfession.ARMORER, PoiTypes.ARMORER, SoundEffects.VILLAGER_WORK_ARMORER);
        register(iregistry, VillagerProfession.BUTCHER, PoiTypes.BUTCHER, SoundEffects.VILLAGER_WORK_BUTCHER);
        register(iregistry, VillagerProfession.CARTOGRAPHER, PoiTypes.CARTOGRAPHER, SoundEffects.VILLAGER_WORK_CARTOGRAPHER);
        register(iregistry, VillagerProfession.CLERIC, PoiTypes.CLERIC, SoundEffects.VILLAGER_WORK_CLERIC);
        register(iregistry, VillagerProfession.FARMER, PoiTypes.FARMER, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL), ImmutableSet.of(Blocks.FARMLAND), SoundEffects.VILLAGER_WORK_FARMER);
        register(iregistry, VillagerProfession.FISHERMAN, PoiTypes.FISHERMAN, SoundEffects.VILLAGER_WORK_FISHERMAN);
        register(iregistry, VillagerProfession.FLETCHER, PoiTypes.FLETCHER, SoundEffects.VILLAGER_WORK_FLETCHER);
        register(iregistry, VillagerProfession.LEATHERWORKER, PoiTypes.LEATHERWORKER, SoundEffects.VILLAGER_WORK_LEATHERWORKER);
        register(iregistry, VillagerProfession.LIBRARIAN, PoiTypes.LIBRARIAN, SoundEffects.VILLAGER_WORK_LIBRARIAN);
        register(iregistry, VillagerProfession.MASON, PoiTypes.MASON, SoundEffects.VILLAGER_WORK_MASON);
        register(iregistry, VillagerProfession.NITWIT, VillagePlaceType.NONE, VillagePlaceType.NONE, (SoundEffect) null);
        register(iregistry, VillagerProfession.SHEPHERD, PoiTypes.SHEPHERD, SoundEffects.VILLAGER_WORK_SHEPHERD);
        register(iregistry, VillagerProfession.TOOLSMITH, PoiTypes.TOOLSMITH, SoundEffects.VILLAGER_WORK_TOOLSMITH);
        return register(iregistry, VillagerProfession.WEAPONSMITH, PoiTypes.WEAPONSMITH, SoundEffects.VILLAGER_WORK_WEAPONSMITH);
    }
}
