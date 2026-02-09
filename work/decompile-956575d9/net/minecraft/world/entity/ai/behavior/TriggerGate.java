package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;

public class TriggerGate {

    public TriggerGate() {}

    public static <E extends EntityLiving> OneShot<E> triggerOneShuffled(List<Pair<? extends Trigger<? super E>, Integer>> list) {
        return triggerGate(list, BehaviorGate.Order.SHUFFLED, BehaviorGate.Execution.RUN_ONE);
    }

    public static <E extends EntityLiving> OneShot<E> triggerGate(List<Pair<? extends Trigger<? super E>, Integer>> list, BehaviorGate.Order behaviorgate_order, BehaviorGate.Execution behaviorgate_execution) {
        ShufflingList<Trigger<? super E>> shufflinglist = new ShufflingList<Trigger<? super E>>();

        list.forEach((pair) -> {
            shufflinglist.add((Trigger) pair.getFirst(), (Integer) pair.getSecond());
        });
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.point((Trigger) (worldserver, entityliving, i) -> {
                if (behaviorgate_order == BehaviorGate.Order.SHUFFLED) {
                    shufflinglist.shuffle();
                }

                for (Trigger<? super E> trigger : shufflinglist) {
                    if (trigger.trigger(worldserver, entityliving, i) && behaviorgate_execution == BehaviorGate.Execution.RUN_ONE) {
                        break;
                    }
                }

                return true;
            });
        });
    }
}
