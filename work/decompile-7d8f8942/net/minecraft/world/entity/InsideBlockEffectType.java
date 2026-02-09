package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.world.level.block.BlockFireAbstract;

public enum InsideBlockEffectType {

    FREEZE((entity) -> {
        entity.setIsInPowderSnow(true);
        if (entity.canFreeze()) {
            entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen() + 1));
        }

    }), FIRE_IGNITE(BlockFireAbstract::fireIgnite), LAVA_IGNITE(Entity::lavaIgnite), EXTINGUISH(Entity::clearFire);

    private final Consumer<Entity> effect;

    private InsideBlockEffectType(final Consumer consumer) {
        this.effect = consumer;
    }

    public Consumer<Entity> effect() {
        return this.effect;
    }
}
