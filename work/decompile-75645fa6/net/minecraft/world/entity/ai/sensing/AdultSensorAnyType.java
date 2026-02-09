package net.minecraft.world.entity.ai.sensing;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.tags.TagsEntity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class AdultSensorAnyType extends SensorAdult {

    public AdultSensorAnyType() {}

    @Override
    protected void setNearestVisibleAdult(EntityLiving entityliving, NearestVisibleLivingEntities nearestvisiblelivingentities) {
        Optional optional = nearestvisiblelivingentities.findClosest((entityliving1) -> {
            return entityliving1.getType().is(TagsEntity.FOLLOWABLE_FRIENDLY_MOBS) && !entityliving1.isBaby();
        });

        Objects.requireNonNull(EntityLiving.class);
        Optional<EntityLiving> optional1 = optional.map(EntityLiving.class::cast);

        entityliving.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, optional1);
    }
}
