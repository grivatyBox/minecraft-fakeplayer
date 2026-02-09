package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.SystemUtils;
import net.minecraft.util.ExtraCodecs;

public record DropChances(Map<EnumItemSlot, Float> byEquipment) {

    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
    public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0F;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final DropChances DEFAULT = new DropChances(SystemUtils.makeEnumMap(EnumItemSlot.class, (enumitemslot) -> {
        return 0.085F;
    }));
    public static final Codec<DropChances> CODEC = Codec.unboundedMap(EnumItemSlot.CODEC, ExtraCodecs.NON_NEGATIVE_FLOAT).xmap(DropChances::toEnumMap, DropChances::filterDefaultValues).xmap(DropChances::new, DropChances::byEquipment);

    private static Map<EnumItemSlot, Float> filterDefaultValues(Map<EnumItemSlot, Float> map) {
        Map<EnumItemSlot, Float> map1 = new HashMap(map);

        map1.values().removeIf((ofloat) -> {
            return ofloat == 0.085F;
        });
        return map1;
    }

    private static Map<EnumItemSlot, Float> toEnumMap(Map<EnumItemSlot, Float> map) {
        return SystemUtils.<EnumItemSlot, Float>makeEnumMap(EnumItemSlot.class, (enumitemslot) -> {
            return (Float) map.getOrDefault(enumitemslot, 0.085F);
        });
    }

    public DropChances withGuaranteedDrop(EnumItemSlot enumitemslot) {
        return this.withEquipmentChance(enumitemslot, 2.0F);
    }

    public DropChances withEquipmentChance(EnumItemSlot enumitemslot, float f) {
        if (f < 0.0F) {
            throw new IllegalArgumentException("Tried to set invalid equipment chance " + f + " for " + String.valueOf(enumitemslot));
        } else {
            return this.byEquipment(enumitemslot) == f ? this : new DropChances(SystemUtils.makeEnumMap(EnumItemSlot.class, (enumitemslot1) -> {
                return enumitemslot1 == enumitemslot ? f : this.byEquipment(enumitemslot1);
            }));
        }
    }

    public float byEquipment(EnumItemSlot enumitemslot) {
        return (Float) this.byEquipment.getOrDefault(enumitemslot, 0.085F);
    }

    public boolean isPreserved(EnumItemSlot enumitemslot) {
        return this.byEquipment(enumitemslot) > 1.0F;
    }
}
