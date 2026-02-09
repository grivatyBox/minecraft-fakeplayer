package net.minecraft.world.item.equipment;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;

public record EquipmentModel(Map<EquipmentModel.d, List<EquipmentModel.c>> layers) {

    private static final Codec<List<EquipmentModel.c>> LAYER_LIST_CODEC = ExtraCodecs.nonEmptyList(EquipmentModel.c.CODEC.listOf());
    public static final Codec<EquipmentModel> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.nonEmptyMap(Codec.unboundedMap(EquipmentModel.d.CODEC, EquipmentModel.LAYER_LIST_CODEC)).fieldOf("layers").forGetter(EquipmentModel::layers)).apply(instance, EquipmentModel::new);
    });

    public static EquipmentModel.a builder() {
        return new EquipmentModel.a();
    }

    public List<EquipmentModel.c> getLayers(EquipmentModel.d equipmentmodel_d) {
        return (List) this.layers.getOrDefault(equipmentmodel_d, List.of());
    }

    public static class a {

        private final Map<EquipmentModel.d, List<EquipmentModel.c>> layersByType = new EnumMap(EquipmentModel.d.class);

        a() {}

        public EquipmentModel.a addHumanoidLayers(MinecraftKey minecraftkey) {
            return this.addHumanoidLayers(minecraftkey, false);
        }

        public EquipmentModel.a addHumanoidLayers(MinecraftKey minecraftkey, boolean flag) {
            this.addLayers(EquipmentModel.d.HUMANOID_LEGGINGS, EquipmentModel.c.leatherDyeable(minecraftkey, flag));
            this.addMainHumanoidLayer(minecraftkey, flag);
            return this;
        }

        public EquipmentModel.a addMainHumanoidLayer(MinecraftKey minecraftkey, boolean flag) {
            return this.addLayers(EquipmentModel.d.HUMANOID, EquipmentModel.c.leatherDyeable(minecraftkey, flag));
        }

        public EquipmentModel.a addLayers(EquipmentModel.d equipmentmodel_d, EquipmentModel.c... aequipmentmodel_c) {
            Collections.addAll((Collection) this.layersByType.computeIfAbsent(equipmentmodel_d, (equipmentmodel_d1) -> {
                return new ArrayList();
            }), aequipmentmodel_c);
            return this;
        }

        public EquipmentModel build() {
            return new EquipmentModel((Map) this.layersByType.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (entry) -> {
                return List.copyOf((Collection) entry.getValue());
            })));
        }
    }

    public static enum d implements INamable {

        HUMANOID("humanoid"), HUMANOID_LEGGINGS("humanoid_leggings"), WINGS("wings"), WOLF_BODY("wolf_body"), HORSE_BODY("horse_body"), LLAMA_BODY("llama_body");

        public static final Codec<EquipmentModel.d> CODEC = INamable.fromEnum(EquipmentModel.d::values);
        private final String id;

        private d(final String s) {
            this.id = s;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static record c(MinecraftKey textureId, Optional<EquipmentModel.b> dyeable, boolean usePlayerTexture) {

        public static final Codec<EquipmentModel.c> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(MinecraftKey.CODEC.fieldOf("texture").forGetter(EquipmentModel.c::textureId), EquipmentModel.b.CODEC.optionalFieldOf("dyeable").forGetter(EquipmentModel.c::dyeable), Codec.BOOL.optionalFieldOf("use_player_texture", false).forGetter(EquipmentModel.c::usePlayerTexture)).apply(instance, EquipmentModel.c::new);
        });

        public c(MinecraftKey minecraftkey) {
            this(minecraftkey, Optional.empty(), false);
        }

        public static EquipmentModel.c leatherDyeable(MinecraftKey minecraftkey, boolean flag) {
            return new EquipmentModel.c(minecraftkey, flag ? Optional.of(new EquipmentModel.b(Optional.of(-6265536))) : Optional.empty(), false);
        }

        public static EquipmentModel.c onlyIfDyed(MinecraftKey minecraftkey, boolean flag) {
            return new EquipmentModel.c(minecraftkey, flag ? Optional.of(new EquipmentModel.b(Optional.empty())) : Optional.empty(), false);
        }

        public MinecraftKey getTextureLocation(EquipmentModel.d equipmentmodel_d) {
            return this.textureId.withPath((s) -> {
                String s1 = equipmentmodel_d.getSerializedName();

                return "textures/entity/equipment/" + s1 + "/" + s + ".png";
            });
        }
    }

    public static record b(Optional<Integer> colorWhenUndyed) {

        public static final Codec<EquipmentModel.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color_when_undyed").forGetter(EquipmentModel.b::colorWhenUndyed)).apply(instance, EquipmentModel.b::new);
        });
    }
}
