package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;

public record FireworkExplosionPredicate(FireworkExplosionPredicate.a predicate) implements SingleComponentItemPredicate<FireworkExplosion> {

    public static final Codec<FireworkExplosionPredicate> CODEC = FireworkExplosionPredicate.a.CODEC.xmap(FireworkExplosionPredicate::new, FireworkExplosionPredicate::predicate);

    @Override
    public DataComponentType<FireworkExplosion> componentType() {
        return DataComponents.FIREWORK_EXPLOSION;
    }

    public boolean matches(FireworkExplosion fireworkexplosion) {
        return this.predicate.test(fireworkexplosion);
    }

    public static record a(Optional<FireworkExplosion.a> shape, Optional<Boolean> twinkle, Optional<Boolean> trail) implements Predicate<FireworkExplosion> {

        public static final Codec<FireworkExplosionPredicate.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(FireworkExplosion.a.CODEC.optionalFieldOf("shape").forGetter(FireworkExplosionPredicate.a::shape), Codec.BOOL.optionalFieldOf("has_twinkle").forGetter(FireworkExplosionPredicate.a::twinkle), Codec.BOOL.optionalFieldOf("has_trail").forGetter(FireworkExplosionPredicate.a::trail)).apply(instance, FireworkExplosionPredicate.a::new);
        });

        public boolean test(FireworkExplosion fireworkexplosion) {
            return this.shape.isPresent() && this.shape.get() != fireworkexplosion.shape() ? false : (this.twinkle.isPresent() && (Boolean) this.twinkle.get() != fireworkexplosion.hasTwinkle() ? false : !this.trail.isPresent() || (Boolean) this.trail.get() == fireworkexplosion.hasTrail());
        }
    }
}
