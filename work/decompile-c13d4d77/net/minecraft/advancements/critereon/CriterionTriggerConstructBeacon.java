package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.util.ExtraCodecs;

public class CriterionTriggerConstructBeacon extends CriterionTriggerAbstract<CriterionTriggerConstructBeacon.a> {

    public CriterionTriggerConstructBeacon() {}

    @Override
    public Codec<CriterionTriggerConstructBeacon.a> codec() {
        return CriterionTriggerConstructBeacon.a.CODEC;
    }

    public void trigger(EntityPlayer entityplayer, int i) {
        this.trigger(entityplayer, (criteriontriggerconstructbeacon_a) -> {
            return criteriontriggerconstructbeacon_a.matches(i);
        });
    }

    public static record a(Optional<ContextAwarePredicate> player, CriterionConditionValue.IntegerRange level) implements CriterionTriggerAbstract.a {

        public static final Codec<CriterionTriggerConstructBeacon.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(ExtraCodecs.strictOptionalField(CriterionConditionEntity.ADVANCEMENT_CODEC, "player").forGetter(CriterionTriggerConstructBeacon.a::player), ExtraCodecs.strictOptionalField(CriterionConditionValue.IntegerRange.CODEC, "level", CriterionConditionValue.IntegerRange.ANY).forGetter(CriterionTriggerConstructBeacon.a::level)).apply(instance, CriterionTriggerConstructBeacon.a::new);
        });

        public static Criterion<CriterionTriggerConstructBeacon.a> constructedBeacon() {
            return CriterionTriggers.CONSTRUCT_BEACON.createCriterion(new CriterionTriggerConstructBeacon.a(Optional.empty(), CriterionConditionValue.IntegerRange.ANY));
        }

        public static Criterion<CriterionTriggerConstructBeacon.a> constructedBeacon(CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) {
            return CriterionTriggers.CONSTRUCT_BEACON.createCriterion(new CriterionTriggerConstructBeacon.a(Optional.empty(), criterionconditionvalue_integerrange));
        }

        public boolean matches(int i) {
            return this.level.matches(i);
        }
    }
}
