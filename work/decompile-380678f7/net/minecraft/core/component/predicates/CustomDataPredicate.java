package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.CriterionConditionNBT;
import net.minecraft.core.component.DataComponentGetter;

public record CustomDataPredicate(CriterionConditionNBT value) implements DataComponentPredicate {

    public static final Codec<CustomDataPredicate> CODEC = CriterionConditionNBT.CODEC.xmap(CustomDataPredicate::new, CustomDataPredicate::value);

    @Override
    public boolean matches(DataComponentGetter datacomponentgetter) {
        return this.value.matches(datacomponentgetter);
    }

    public static CustomDataPredicate customData(CriterionConditionNBT criterionconditionnbt) {
        return new CustomDataPredicate(criterionconditionnbt);
    }
}
