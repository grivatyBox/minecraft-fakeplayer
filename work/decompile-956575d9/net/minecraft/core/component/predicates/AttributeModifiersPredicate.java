package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record AttributeModifiersPredicate(Optional<CollectionPredicate<ItemAttributeModifiers.b, AttributeModifiersPredicate.a>> modifiers) implements SingleComponentItemPredicate<ItemAttributeModifiers> {

    public static final Codec<AttributeModifiersPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(AttributeModifiersPredicate.a.CODEC).optionalFieldOf("modifiers").forGetter(AttributeModifiersPredicate::modifiers)).apply(instance, AttributeModifiersPredicate::new);
    });

    @Override
    public DataComponentType<ItemAttributeModifiers> componentType() {
        return DataComponents.ATTRIBUTE_MODIFIERS;
    }

    public boolean matches(ItemAttributeModifiers itemattributemodifiers) {
        return !this.modifiers.isPresent() || ((CollectionPredicate) this.modifiers.get()).test(itemattributemodifiers.modifiers());
    }

    public static record a(Optional<HolderSet<AttributeBase>> attribute, Optional<MinecraftKey> id, CriterionConditionValue.DoubleRange amount, Optional<AttributeModifier.Operation> operation, Optional<EquipmentSlotGroup> slot) implements Predicate<ItemAttributeModifiers.b> {

        public static final Codec<AttributeModifiersPredicate.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(RegistryCodecs.homogeneousList(Registries.ATTRIBUTE).optionalFieldOf("attribute").forGetter(AttributeModifiersPredicate.a::attribute), MinecraftKey.CODEC.optionalFieldOf("id").forGetter(AttributeModifiersPredicate.a::id), CriterionConditionValue.DoubleRange.CODEC.optionalFieldOf("amount", CriterionConditionValue.DoubleRange.ANY).forGetter(AttributeModifiersPredicate.a::amount), AttributeModifier.Operation.CODEC.optionalFieldOf("operation").forGetter(AttributeModifiersPredicate.a::operation), EquipmentSlotGroup.CODEC.optionalFieldOf("slot").forGetter(AttributeModifiersPredicate.a::slot)).apply(instance, AttributeModifiersPredicate.a::new);
        });

        public boolean test(ItemAttributeModifiers.b itemattributemodifiers_b) {
            return this.attribute.isPresent() && !((HolderSet) this.attribute.get()).contains(itemattributemodifiers_b.attribute()) ? false : (this.id.isPresent() && !((MinecraftKey) this.id.get()).equals(itemattributemodifiers_b.modifier().id()) ? false : (!this.amount.matches(itemattributemodifiers_b.modifier().amount()) ? false : (this.operation.isPresent() && this.operation.get() != itemattributemodifiers_b.modifier().operation() ? false : !this.slot.isPresent() || this.slot.get() == itemattributemodifiers_b.slot())));
        }
    }
}
