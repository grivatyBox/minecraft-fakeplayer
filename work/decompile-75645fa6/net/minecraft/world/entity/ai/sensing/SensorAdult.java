package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SensorAdult extends Sensor<EntityLiving> {

    public SensorAdult() {}

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
    }

    @Override
    protected void doTick(WorldServer worldserver, EntityLiving entityliving) {
        entityliving.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((nearestvisiblelivingentities) -> {
            this.setNearestVisibleAdult(entityliving, nearestvisiblelivingentities);
        });
    }

    protected void setNearestVisibleAdult(EntityLiving entityliving, NearestVisibleLivingEntities nearestvisiblelivingentities) {
        Optional optional = nearestvisiblelivingentities.findClosest((entityliving1) -> {
            return entityliving1.getType() == entityliving.getType() && !entityliving1.isBaby();
        });

        Objects.requireNonNull(EntityLiving.class);
        Optional<EntityLiving> optional1 = optional.map(EntityLiving.class::cast);

        entityliving.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional1);
    }
}
