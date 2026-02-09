package net.minecraft.world.item.equipment.trim;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public record MaterialAssetGroup(MaterialAssetGroup.a base, Map<ResourceKey<EquipmentAsset>, MaterialAssetGroup.a> overrides) {

    public static final String SEPARATOR = "_";
    public static final MapCodec<MaterialAssetGroup> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MaterialAssetGroup.a.CODEC.fieldOf("asset_name").forGetter(MaterialAssetGroup::base), Codec.unboundedMap(ResourceKey.codec(EquipmentAssets.ROOT_ID), MaterialAssetGroup.a.CODEC).optionalFieldOf("override_armor_assets", Map.of()).forGetter(MaterialAssetGroup::overrides)).apply(instance, MaterialAssetGroup::new);
    });
    public static final StreamCodec<ByteBuf, MaterialAssetGroup> STREAM_CODEC = StreamCodec.composite(MaterialAssetGroup.a.STREAM_CODEC, MaterialAssetGroup::base, ByteBufCodecs.map(Object2ObjectOpenHashMap::new, ResourceKey.streamCodec(EquipmentAssets.ROOT_ID), MaterialAssetGroup.a.STREAM_CODEC), MaterialAssetGroup::overrides, MaterialAssetGroup::new);
    public static final MaterialAssetGroup QUARTZ = create("quartz");
    public static final MaterialAssetGroup IRON = create("iron", Map.of(EquipmentAssets.IRON, "iron_darker"));
    public static final MaterialAssetGroup NETHERITE = create("netherite", Map.of(EquipmentAssets.NETHERITE, "netherite_darker"));
    public static final MaterialAssetGroup REDSTONE = create("redstone");
    public static final MaterialAssetGroup COPPER = create("copper");
    public static final MaterialAssetGroup GOLD = create("gold", Map.of(EquipmentAssets.GOLD, "gold_darker"));
    public static final MaterialAssetGroup EMERALD = create("emerald");
    public static final MaterialAssetGroup DIAMOND = create("diamond", Map.of(EquipmentAssets.DIAMOND, "diamond_darker"));
    public static final MaterialAssetGroup LAPIS = create("lapis");
    public static final MaterialAssetGroup AMETHYST = create("amethyst");
    public static final MaterialAssetGroup RESIN = create("resin");

    public static MaterialAssetGroup create(String s) {
        return new MaterialAssetGroup(new MaterialAssetGroup.a(s), Map.of());
    }

    public static MaterialAssetGroup create(String s, Map<ResourceKey<EquipmentAsset>, String> map) {
        return new MaterialAssetGroup(new MaterialAssetGroup.a(s), Map.copyOf(Maps.transformValues(map, MaterialAssetGroup.a::new)));
    }

    public MaterialAssetGroup.a assetId(ResourceKey<EquipmentAsset> resourcekey) {
        return (MaterialAssetGroup.a) this.overrides.getOrDefault(resourcekey, this.base);
    }

    public static record a(String suffix) {

        public static final Codec<MaterialAssetGroup.a> CODEC = ExtraCodecs.RESOURCE_PATH_CODEC.xmap(MaterialAssetGroup.a::new, MaterialAssetGroup.a::suffix);
        public static final StreamCodec<ByteBuf, MaterialAssetGroup.a> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(MaterialAssetGroup.a::new, MaterialAssetGroup.a::suffix);

        public a(String s) {
            if (!MinecraftKey.isValidPath(s)) {
                throw new IllegalArgumentException("Invalid string to use as a resource path element: " + s);
            } else {
                this.suffix = s;
            }
        }
    }
}
