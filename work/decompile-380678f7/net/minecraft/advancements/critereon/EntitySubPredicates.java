package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntitySubPredicates {

    public static final MapCodec<LightningBoltPredicate> LIGHTNING = register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<CriterionConditionInOpenWater> FISHING_HOOK = register("fishing_hook", CriterionConditionInOpenWater.CODEC);
    public static final MapCodec<CriterionConditionPlayer> PLAYER = register("player", CriterionConditionPlayer.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = register("raider", RaiderPredicate.CODEC);
    public static final MapCodec<SheepPredicate> SHEEP = register("sheep", SheepPredicate.CODEC);

    public EntitySubPredicates() {}

    private static <T extends EntitySubPredicate> MapCodec<T> register(String s, MapCodec<T> mapcodec) {
        return (MapCodec) IRegistry.register(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE, s, mapcodec);
    }

    public static MapCodec<? extends EntitySubPredicate> bootstrap(IRegistry<MapCodec<? extends EntitySubPredicate>> iregistry) {
        return EntitySubPredicates.LIGHTNING;
    }
}
