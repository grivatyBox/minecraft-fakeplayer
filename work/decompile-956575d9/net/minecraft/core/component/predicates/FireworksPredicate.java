package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

public record FireworksPredicate(Optional<CollectionPredicate<FireworkExplosion, FireworkExplosionPredicate.a>> explosions, CriterionConditionValue.IntegerRange flightDuration) implements SingleComponentItemPredicate<Fireworks> {

    public static final Codec<FireworksPredicate> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(CollectionPredicate.codec(FireworkExplosionPredicate.a.CODEC).optionalFieldOf("explosions").forGetter(FireworksPredicate::explosions), CriterionConditionValue.IntegerRange.CODEC.optionalFieldOf("flight_duration", CriterionConditionValue.IntegerRange.ANY).forGetter(FireworksPredicate::flightDuration)).apply(instance, FireworksPredicate::new);
    });

    @Override
    public DataComponentType<Fireworks> componentType() {
        return DataComponents.FIREWORKS;
    }

    public boolean matches(Fireworks fireworks) {
        return this.explosions.isPresent() && !((CollectionPredicate) this.explosions.get()).test(fireworks.explosions()) ? false : this.flightDuration.matches(fireworks.flightDuration());
    }
}
