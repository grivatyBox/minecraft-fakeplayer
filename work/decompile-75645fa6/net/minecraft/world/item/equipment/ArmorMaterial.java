package net.minecraft.world.item.equipment;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmorMaterial(int durability, Map<ArmorType, Integer> defense, int enchantmentValue, Holder<SoundEffect> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId) {

    public ItemAttributeModifiers createAttributes(ArmorType armortype) {
        int i = (Integer) this.defense.getOrDefault(armortype, 0);
        ItemAttributeModifiers.a itemattributemodifiers_a = ItemAttributeModifiers.builder();
        EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(armortype.getSlot());
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace("armor." + armortype.getName());

        itemattributemodifiers_a.add(GenericAttributes.ARMOR, new AttributeModifier(minecraftkey, (double) i, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
        itemattributemodifiers_a.add(GenericAttributes.ARMOR_TOUGHNESS, new AttributeModifier(minecraftkey, (double) this.toughness, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
        if (this.knockbackResistance > 0.0F) {
            itemattributemodifiers_a.add(GenericAttributes.KNOCKBACK_RESISTANCE, new AttributeModifier(minecraftkey, (double) this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup);
        }

        return itemattributemodifiers_a.build();
    }
}
