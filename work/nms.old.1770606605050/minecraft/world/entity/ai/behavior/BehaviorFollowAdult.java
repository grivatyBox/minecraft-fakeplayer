package net.minecraft.world.entity.ai.behavior;

import java.util.function.Function;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryTarget;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
// CraftBukkit end

public class BehaviorFollowAdult {

    public BehaviorFollowAdult() {}

    public static OneShot<EntityLiving> create(UniformInt uniformint, float f) {
        return create(uniformint, (entityliving) -> {
            return f;
        }, MemoryModuleType.NEAREST_VISIBLE_ADULT, false);
    }

    public static OneShot<EntityLiving> create(UniformInt uniformint, Function<EntityLiving, Float> function, MemoryModuleType<? extends EntityLiving> memorymoduletype, boolean flag) {
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.group(behaviorbuilder_b.present(memorymoduletype), behaviorbuilder_b.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_b.absent(MemoryModuleType.WALK_TARGET)).apply(behaviorbuilder_b, (memoryaccessor, memoryaccessor1, memoryaccessor2) -> {
                return (worldserver, entityliving, i) -> {
                    if (!entityliving.isBaby()) {
                        return false;
                    } else {
                        EntityLiving entityliving1 = (EntityLiving) behaviorbuilder_b.get(memoryaccessor);

                        if (entityliving.closerThan(entityliving1, (double) (uniformint.getMaxValue() + 1)) && !entityliving.closerThan(entityliving1, (double) uniformint.getMinValue())) {
                            // CraftBukkit start
                            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(entityliving, entityliving1, EntityTargetEvent.TargetReason.FOLLOW_LEADER);
                            if (event.isCancelled()) {
                                return false;
                            }
                            if (event.getTarget() == null) {
                                memoryaccessor.erase();
                                return true;
                            }
                            entityliving1 = ((CraftLivingEntity) event.getTarget()).getHandle();
                            // CraftBukkit end
                            MemoryTarget memorytarget = new MemoryTarget(new BehaviorPositionEntity(entityliving1, flag, flag), (Float) function.apply(entityliving), uniformint.getMinValue() - 1);

                            memoryaccessor1.set(new BehaviorPositionEntity(entityliving1, true, flag));
                            memoryaccessor2.set(memorytarget);
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
            });
        });
    }
}
