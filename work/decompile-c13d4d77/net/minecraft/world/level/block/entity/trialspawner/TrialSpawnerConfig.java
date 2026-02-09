package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.MobSpawnerData;

public record TrialSpawnerConfig(int requiredPlayerRange, int spawnRange, float totalMobs, float simultaneousMobs, float totalMobsAddedPerPlayer, float simultaneousMobsAddedPerPlayer, int ticksBetweenSpawn, int targetCooldownLength, SimpleWeightedRandomList<MobSpawnerData> spawnPotentialsDefinition, SimpleWeightedRandomList<MinecraftKey> lootTablesToEject) {

    public static TrialSpawnerConfig DEFAULT = new TrialSpawnerConfig(14, 4, 6.0F, 2.0F, 2.0F, 1.0F, 40, 36000, SimpleWeightedRandomList.empty(), SimpleWeightedRandomList.empty());
    public static MapCodec<TrialSpawnerConfig> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.intRange(1, 128).optionalFieldOf("required_player_range", TrialSpawnerConfig.DEFAULT.requiredPlayerRange).forGetter(TrialSpawnerConfig::requiredPlayerRange), Codec.intRange(1, 128).optionalFieldOf("spawn_range", TrialSpawnerConfig.DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", TrialSpawnerConfig.DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("simultaneous_mobs", TrialSpawnerConfig.DEFAULT.simultaneousMobs).forGetter(TrialSpawnerConfig::simultaneousMobs), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs_added_per_player", TrialSpawnerConfig.DEFAULT.totalMobsAddedPerPlayer).forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer), Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("simultaneous_mobs_added_per_player", TrialSpawnerConfig.DEFAULT.simultaneousMobsAddedPerPlayer).forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("ticks_between_spawn", TrialSpawnerConfig.DEFAULT.ticksBetweenSpawn).forGetter(TrialSpawnerConfig::ticksBetweenSpawn), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", TrialSpawnerConfig.DEFAULT.targetCooldownLength).forGetter(TrialSpawnerConfig::targetCooldownLength), MobSpawnerData.LIST_CODEC.optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::spawnPotentialsDefinition), SimpleWeightedRandomList.wrappedCodecAllowingEmpty(MinecraftKey.CODEC).optionalFieldOf("loot_tables_to_eject", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::lootTablesToEject)).apply(instance, TrialSpawnerConfig::new);
    });

    public int calculateTargetTotalMobs(int i) {
        return (int) Math.floor((double) (this.totalMobs + this.totalMobsAddedPerPlayer * (float) i));
    }

    public int calculateTargetSimultaneousMobs(int i) {
        return (int) Math.floor((double) (this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float) i));
    }
}
