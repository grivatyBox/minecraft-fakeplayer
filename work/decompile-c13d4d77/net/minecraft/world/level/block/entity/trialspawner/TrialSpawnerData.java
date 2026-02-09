package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.MobSpawnerData;
import net.minecraft.world.level.World;

public class TrialSpawnerData {

    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(UUIDUtil.CODEC_SET.optionalFieldOf("registered_players", Sets.newHashSet()).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.detectedPlayers;
        }), UUIDUtil.CODEC_SET.optionalFieldOf("current_mobs", Sets.newHashSet()).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.currentMobs;
        }), Codec.LONG.optionalFieldOf("cooldown_ends_at", 0L).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.cooldownEndsAt;
        }), Codec.LONG.optionalFieldOf("next_mob_spawns_at", 0L).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.nextMobSpawnsAt;
        }), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("total_mobs_spawned", 0).forGetter((trialspawnerdata) -> {
            return trialspawnerdata.totalMobsSpawned;
        }), MobSpawnerData.CODEC.optionalFieldOf("spawn_data").forGetter((trialspawnerdata) -> {
            return trialspawnerdata.nextSpawnData;
        }), MinecraftKey.CODEC.optionalFieldOf("ejecting_loot_table").forGetter((trialspawnerdata) -> {
            return trialspawnerdata.ejectingLootTable;
        })).apply(instance, TrialSpawnerData::new);
    });
    protected final Set<UUID> detectedPlayers;
    protected final Set<UUID> currentMobs;
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<MobSpawnerData> nextSpawnData;
    protected Optional<MinecraftKey> ejectingLootTable;
    protected SimpleWeightedRandomList<MobSpawnerData> spawnPotentials;
    @Nullable
    protected Entity displayEntity;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(Set<UUID> set, Set<UUID> set1, long i, long j, int k, Optional<MobSpawnerData> optional, Optional<MinecraftKey> optional1) {
        this.detectedPlayers = new HashSet();
        this.currentMobs = new HashSet();
        this.detectedPlayers.addAll(set);
        this.currentMobs.addAll(set1);
        this.cooldownEndsAt = i;
        this.nextMobSpawnsAt = j;
        this.totalMobsSpawned = k;
        this.nextSpawnData = optional;
        this.ejectingLootTable = optional1;
    }

    public void setSpawnPotentialsFromConfig(TrialSpawnerConfig trialspawnerconfig) {
        SimpleWeightedRandomList<MobSpawnerData> simpleweightedrandomlist = trialspawnerconfig.spawnPotentialsDefinition();

        if (simpleweightedrandomlist.isEmpty()) {
            this.spawnPotentials = SimpleWeightedRandomList.single((MobSpawnerData) this.nextSpawnData.orElseGet(MobSpawnerData::new));
        } else {
            this.spawnPotentials = simpleweightedrandomlist;
        }

    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
    }

    public boolean hasMobToSpawn() {
        boolean flag = this.nextSpawnData.isPresent() && ((MobSpawnerData) this.nextSpawnData.get()).getEntityToSpawn().contains("id", 8);

        return flag || !this.spawnPotentials.isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig trialspawnerconfig, int i) {
        return this.totalMobsSpawned >= trialspawnerconfig.calculateTargetTotalMobs(i);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(WorldServer worldserver, TrialSpawnerConfig trialspawnerconfig, int i) {
        return worldserver.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < trialspawnerconfig.calculateTargetSimultaneousMobs(i);
    }

    public int countAdditionalPlayers(BlockPosition blockposition) {
        if (this.detectedPlayers.isEmpty()) {
            SystemUtils.logAndPauseIfInIde("Trial Spawner at " + blockposition + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(WorldServer worldserver, BlockPosition blockposition, PlayerDetector playerdetector, int i) {
        List<UUID> list = playerdetector.detect(worldserver, blockposition, i);
        boolean flag = this.detectedPlayers.addAll(list);

        if (flag) {
            this.nextMobSpawnsAt = Math.max(worldserver.getGameTime() + 40L, this.nextMobSpawnsAt);
            worldserver.levelEvent(3013, blockposition, this.detectedPlayers.size());
        }

    }

    public boolean isReadyToOpenShutter(WorldServer worldserver, TrialSpawnerConfig trialspawnerconfig, float f) {
        long i = this.cooldownEndsAt - (long) trialspawnerconfig.targetCooldownLength();

        return (float) worldserver.getGameTime() >= (float) i + f;
    }

    public boolean isReadyToEjectItems(WorldServer worldserver, TrialSpawnerConfig trialspawnerconfig, float f) {
        long i = this.cooldownEndsAt - (long) trialspawnerconfig.targetCooldownLength();

        return (float) (worldserver.getGameTime() - i) % f == 0.0F;
    }

    public boolean isCooldownFinished(WorldServer worldserver) {
        return worldserver.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner trialspawner, RandomSource randomsource, EntityTypes<?> entitytypes) {
        this.getOrCreateNextSpawnData(trialspawner, randomsource).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entitytypes).toString());
    }

    protected MobSpawnerData getOrCreateNextSpawnData(TrialSpawner trialspawner, RandomSource randomsource) {
        if (this.nextSpawnData.isPresent()) {
            return (MobSpawnerData) this.nextSpawnData.get();
        } else {
            this.nextSpawnData = Optional.of((MobSpawnerData) this.spawnPotentials.getRandom(randomsource).map(WeightedEntry.b::getData).orElseGet(MobSpawnerData::new));
            trialspawner.markUpdated();
            return (MobSpawnerData) this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner trialspawner, World world, TrialSpawnerState trialspawnerstate) {
        if (trialspawner.canSpawnInLevel(world) && trialspawnerstate.hasSpinningMob()) {
            if (this.displayEntity == null) {
                NBTTagCompound nbttagcompound = this.getOrCreateNextSpawnData(trialspawner, world.getRandom()).getEntityToSpawn();

                if (nbttagcompound.contains("id", 8)) {
                    this.displayEntity = EntityTypes.loadEntityRecursive(nbttagcompound, world, Function.identity());
                }
            }

            return this.displayEntity;
        } else {
            return null;
        }
    }

    public NBTTagCompound getUpdateTag(TrialSpawnerState trialspawnerstate) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if (trialspawnerstate == TrialSpawnerState.ACTIVE) {
            nbttagcompound.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData.ifPresent((mobspawnerdata) -> {
            nbttagcompound.put("spawn_data", (NBTBase) MobSpawnerData.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, mobspawnerdata).result().orElseThrow(() -> {
                return new IllegalStateException("Invalid SpawnData");
            }));
        });
        return nbttagcompound;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }
}
