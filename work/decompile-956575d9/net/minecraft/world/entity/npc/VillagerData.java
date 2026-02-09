package net.minecraft.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record VillagerData(Holder<VillagerType> type, Holder<VillagerProfession> profession, int level) {

    public static final int MIN_VILLAGER_LEVEL = 1;
    public static final int MAX_VILLAGER_LEVEL = 5;
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BuiltInRegistries.VILLAGER_TYPE.holderByNameCodec().fieldOf("type").orElseGet(() -> {
            return BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS);
        }).forGetter((villagerdata) -> {
            return villagerdata.type;
        }), BuiltInRegistries.VILLAGER_PROFESSION.holderByNameCodec().fieldOf("profession").orElseGet(() -> {
            return BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE);
        }).forGetter((villagerdata) -> {
            return villagerdata.profession;
        }), Codec.INT.fieldOf("level").orElse(1).forGetter((villagerdata) -> {
            return villagerdata.level;
        })).apply(instance, VillagerData::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.VILLAGER_TYPE), VillagerData::type, ByteBufCodecs.holderRegistry(Registries.VILLAGER_PROFESSION), VillagerData::profession, ByteBufCodecs.VAR_INT, VillagerData::level, VillagerData::new);

    public VillagerData(Holder<VillagerType> holder, Holder<VillagerProfession> holder1, int i) {
        i = Math.max(1, i);
        this.type = holder;
        this.profession = holder1;
        this.level = i;
    }

    public VillagerData withType(Holder<VillagerType> holder) {
        return new VillagerData(holder, this.profession, this.level);
    }

    public VillagerData withType(HolderGetter.a holdergetter_a, ResourceKey<VillagerType> resourcekey) {
        return this.withType(holdergetter_a.getOrThrow(resourcekey));
    }

    public VillagerData withProfession(Holder<VillagerProfession> holder) {
        return new VillagerData(this.type, holder, this.level);
    }

    public VillagerData withProfession(HolderGetter.a holdergetter_a, ResourceKey<VillagerProfession> resourcekey) {
        return this.withProfession(holdergetter_a.getOrThrow(resourcekey));
    }

    public VillagerData withLevel(int i) {
        return new VillagerData(this.type, this.profession, i);
    }

    public static int getMinXpPerLevel(int i) {
        return canLevelUp(i) ? VillagerData.NEXT_LEVEL_XP_THRESHOLDS[i - 1] : 0;
    }

    public static int getMaxXpPerLevel(int i) {
        return canLevelUp(i) ? VillagerData.NEXT_LEVEL_XP_THRESHOLDS[i] : 0;
    }

    public static boolean canLevelUp(int i) {
        return i >= 1 && i < 5;
    }
}
