package net.minecraft.world.entity.monster.creaking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorAttack;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetForget;
import net.minecraft.world.entity.ai.behavior.BehaviorAttackTargetSet;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorSwim;
import net.minecraft.world.entity.ai.behavior.BehaviorWalkAwayOutOfRange;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.schedule.Activity;

public class CreakingAi {

    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Creaking>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

    public CreakingAi() {}

    static void initCoreActivity(BehaviorController<Creaking> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorSwim<Creaking>(0.8F) {
            protected boolean checkExtraStartConditions(WorldServer worldserver, Creaking creaking) {
                return creaking.canMove() && super.checkExtraStartConditions(worldserver, (EntityLiving) creaking);
            }
        }, new BehaviorLook(45, 90), new BehavorMove()));
    }

    static void initIdleActivity(BehaviorController<Creaking> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, 10, ImmutableList.of(BehaviorAttackTargetSet.create((worldserver, creaking) -> {
            return creaking.isActive();
        }, (worldserver, creaking) -> {
            return creaking.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
        }), SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)), new BehaviorGateSingle(ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(0.3F), 2), Pair.of(BehaviorLookWalk.create(0.3F, 3), 2), Pair.of(new BehaviorNop(30, 60), 1)))));
    }

    static void initFightActivity(Creaking creaking, BehaviorController<Creaking> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.FIGHT, 10, ImmutableList.of(BehaviorWalkAwayOutOfRange.create(1.0F), BehaviorAttack.create(Creaking::canMove, 40), BehaviorAttackTargetForget.create((worldserver, entityliving) -> {
            return !isAttackTargetStillReachable(creaking, entityliving);
        })), ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)));
    }

    private static boolean isAttackTargetStillReachable(Creaking creaking, EntityLiving entityliving) {
        Optional<List<EntityHuman>> optional = creaking.getBrain().<List<EntityHuman>>getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS);

        return (Boolean) optional.map((list) -> {
            boolean flag;

            if (entityliving instanceof EntityHuman entityhuman) {
                if (list.contains(entityhuman)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }).orElse(false);
    }

    public static BehaviorController.b<Creaking> brainProvider() {
        return BehaviorController.<Creaking>provider(CreakingAi.MEMORY_TYPES, CreakingAi.SENSOR_TYPES);
    }

    public static BehaviorController<Creaking> makeBrain(Creaking creaking, BehaviorController<Creaking> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initFightActivity(creaking, behaviorcontroller);
        behaviorcontroller.setCoreActivities(ImmutableSet.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    public static void updateActivity(Creaking creaking) {
        if (!creaking.canMove()) {
            creaking.getBrain().useDefaultActivity();
        } else {
            creaking.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        }

    }
}
