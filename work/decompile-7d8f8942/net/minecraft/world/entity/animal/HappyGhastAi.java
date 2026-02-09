package net.minecraft.world.entity.animal;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorFollowAdult;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorLookWalk;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehaviorSwim;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class HappyGhastAi {

    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 1.25F;
    private static final float SPEED_MULTIPLIER_WHEN_FOLLOWING_ADULT = 1.1F;
    private static final double BABY_GHAST_CLOSE_ENOUGH_DIST = 3.0D;
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(3, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super HappyGhast>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.HAPPY_GHAST_TEMPTATIONS, SensorType.NEAREST_ADULT_ANY_TYPE, SensorType.NEAREST_PLAYERS);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_ADULT, new MemoryModuleType[]{MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS});

    public HappyGhastAi() {}

    public static BehaviorController.b<HappyGhast> brainProvider() {
        return BehaviorController.<HappyGhast>provider(HappyGhastAi.MEMORY_TYPES, HappyGhastAi.SENSOR_TYPES);
    }

    protected static BehaviorController<?> makeBrain(BehaviorController<HappyGhast> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        initPanicActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(Set.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    private static void initCoreActivity(BehaviorController<HappyGhast> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new BehaviorSwim(0.8F), new AnimalPanic(2.0F, 0), new BehaviorLook(45, 90), new BehavorMove(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(BehaviorController<HappyGhast> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(1, new FollowTemptation((entityliving) -> {
            return 1.25F;
        }, (entityliving) -> {
            return 3.0D;
        }, true)), Pair.of(2, BehaviorFollowAdult.create(HappyGhastAi.ADULT_FOLLOW_RANGE, (entityliving) -> {
            return 1.1F;
        }, MemoryModuleType.NEAREST_VISIBLE_PLAYER, true)), Pair.of(3, BehaviorFollowAdult.create(HappyGhastAi.ADULT_FOLLOW_RANGE, (entityliving) -> {
            return 1.1F;
        }, MemoryModuleType.NEAREST_VISIBLE_ADULT, true)), Pair.of(4, new BehaviorGateSingle(ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.fly(1.0F), 1), Pair.of(BehaviorLookWalk.create(1.0F, 3), 1))))));
    }

    private static void initPanicActivity(BehaviorController<HappyGhast> behaviorcontroller) {
        behaviorcontroller.addActivityWithConditions(Activity.PANIC, ImmutableList.of(), Set.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_PRESENT)));
    }

    public static void updateActivity(HappyGhast happyghast) {
        happyghast.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.PANIC, Activity.IDLE));
    }
}
