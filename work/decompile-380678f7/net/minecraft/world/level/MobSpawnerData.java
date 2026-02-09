package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EquipmentTable;

public record MobSpawnerData(NBTTagCompound entityToSpawn, Optional<MobSpawnerData.a> customSpawnRules, Optional<EquipmentTable> equipment) {

    public static final String ENTITY_TAG = "entity";
    public static final Codec<MobSpawnerData> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(NBTTagCompound.CODEC.fieldOf("entity").forGetter((mobspawnerdata) -> {
            return mobspawnerdata.entityToSpawn;
        }), MobSpawnerData.a.CODEC.optionalFieldOf("custom_spawn_rules").forGetter((mobspawnerdata) -> {
            return mobspawnerdata.customSpawnRules;
        }), EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter((mobspawnerdata) -> {
            return mobspawnerdata.equipment;
        })).apply(instance, MobSpawnerData::new);
    });
    public static final Codec<WeightedList<MobSpawnerData>> LIST_CODEC = WeightedList.codec(MobSpawnerData.CODEC);

    public MobSpawnerData() {
        this(new NBTTagCompound(), Optional.empty(), Optional.empty());
    }

    public MobSpawnerData(NBTTagCompound nbttagcompound, Optional<MobSpawnerData.a> optional, Optional<EquipmentTable> optional1) {
        Optional<MinecraftKey> optional2 = nbttagcompound.read("id", MinecraftKey.CODEC);

        if (optional2.isPresent()) {
            nbttagcompound.store("id", MinecraftKey.CODEC, (MinecraftKey) optional2.get());
        } else {
            nbttagcompound.remove("id");
        }

        this.entityToSpawn = nbttagcompound;
        this.customSpawnRules = optional;
        this.equipment = optional1;
    }

    public NBTTagCompound getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<MobSpawnerData.a> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<EquipmentTable> getEquipment() {
        return this.equipment;
    }

    public static record a(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {

        private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<Integer>(0, 15);
        public static final Codec<MobSpawnerData.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(lightLimit("block_light_limit").forGetter((mobspawnerdata_a) -> {
                return mobspawnerdata_a.blockLightLimit;
            }), lightLimit("sky_light_limit").forGetter((mobspawnerdata_a) -> {
                return mobspawnerdata_a.skyLightLimit;
            })).apply(instance, MobSpawnerData.a::new);
        });

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> inclusiverange) {
            return !MobSpawnerData.a.LIGHT_RANGE.contains(inclusiverange) ? DataResult.error(() -> {
                return "Light values must be withing range " + String.valueOf(MobSpawnerData.a.LIGHT_RANGE);
            }) : DataResult.success(inclusiverange);
        }

        private static MapCodec<InclusiveRange<Integer>> lightLimit(String s) {
            return InclusiveRange.INT.lenientOptionalFieldOf(s, MobSpawnerData.a.LIGHT_RANGE).validate(MobSpawnerData.a::checkLightBoundaries);
        }

        public boolean isValidPosition(BlockPosition blockposition, WorldServer worldserver) {
            return this.blockLightLimit.isValueInRange(worldserver.getBrightness(EnumSkyBlock.BLOCK, blockposition)) && this.skyLightLimit.isValueInRange(worldserver.getBrightness(EnumSkyBlock.SKY, blockposition));
        }
    }
}
