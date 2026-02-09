package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;

public record DamagePredicate(CriterionConditionValue.IntegerRange durability, CriterionConditionValue.IntegerRange damage) implements DataComponentPredicate {

    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("durability", CriterionConditionValue.IntegerRange.ANY).forGetter(DamagePredicate::durability), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("damage", CriterionConditionValue.IntegerRange.ANY).forGetter(DamagePredicate::damage)).apply(instance, DamagePredicate::new);
    });

    @Override
    public boolean matches(DataComponentGetter datacomponentgetter) {
        Integer integer = (Integer) datacomponentgetter.get(DataComponents.DAMAGE);

        if (integer == null) {
            return false;
        } else {
            int i = (Integer) datacomponentgetter.getOrDefault(DataComponents.MAX_DAMAGE, 0);

            return !this.durability.matches(i - integer) ? false : this.damage.matches(integer);
        }
    }

    public static DamagePredicate durability(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
        return new DamagePredicate(criterionconditionvalue_integerrange, CriterionConditionValue.IntegerRange.ANY);
    }
}
