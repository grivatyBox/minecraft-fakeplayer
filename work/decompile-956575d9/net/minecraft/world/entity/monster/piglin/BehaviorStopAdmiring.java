package net.minecraft.world.entity.monster.piglin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorStopAdmiring {

    public BehaviorStopAdmiring() {}

    public static BehaviorControl<EntityPiglin> create() {
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.group(behaviorbuilder_b.absent(MemoryModuleType.ADMIRING_ITEM)).apply(behaviorbuilder_b, (memoryaccessor) -> {
                return (worldserver, entitypiglin, i) -> {
                    if (!entitypiglin.getOffhandItem().isEmpty() && !entitypiglin.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)) {
                        PiglinAI.stopHoldingOffHandItem(worldserver, entitypiglin, true);
                        return true;
                    } else {
                        return false;
                    }
                };
            });
        });
    }
}
