package net.minecraft.world.entity.animal.coppergolem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.AnimalPanic;
import net.minecraft.world.entity.ai.behavior.BehaviorGateSingle;
import net.minecraft.world.entity.ai.behavior.BehaviorInteractDoor;
import net.minecraft.world.entity.ai.behavior.BehaviorLook;
import net.minecraft.world.entity.ai.behavior.BehaviorNop;
import net.minecraft.world.entity.ai.behavior.BehaviorStrollRandomUnconstrained;
import net.minecraft.world.entity.ai.behavior.BehavorMove;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityChest;
import net.minecraft.world.level.block.state.IBlockData;

public class CopperGolemAi {

    private static final float SPEED_MULTIPLIER_WHEN_PANICKING = 1.5F;
    private static final float SPEED_MULTIPLIER_WHEN_IDLING = 1.0F;
    private static final int TRANSPORT_ITEM_HORIZONTAL_SEARCH_RADIUS = 32;
    private static final int TRANSPORT_ITEM_VERTICAL_SEARCH_RADIUS = 8;
    private static final int TICK_TO_START_ON_REACHED_INTERACTION = 1;
    private static final int TICK_TO_PLAY_ON_REACHED_SOUND = 9;
    private static final Predicate<IBlockData> TRANSPORT_ITEM_SOURCE_BLOCK = (iblockdata) -> {
        return iblockdata.is(TagsBlock.COPPER_CHESTS);
    };
    private static final Predicate<IBlockData> TRANSPORT_ITEM_DESTINATION_BLOCK = (iblockdata) -> {
        return iblockdata.is(Blocks.CHEST) || iblockdata.is(Blocks.TRAPPED_CHEST);
    };
    private static final ImmutableList<SensorType<? extends Sensor<? super CopperGolem>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, MemoryModuleType.VISITED_BLOCK_POSITIONS, new MemoryModuleType[]{MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, MemoryModuleType.DOORS_TO_CLOSE});

    public CopperGolemAi() {}

    public static BehaviorController.b<CopperGolem> brainProvider() {
        return BehaviorController.<CopperGolem>provider(CopperGolemAi.MEMORY_TYPES, CopperGolemAi.SENSOR_TYPES);
    }

    protected static BehaviorController<?> makeBrain(BehaviorController<CopperGolem> behaviorcontroller) {
        initCoreActivity(behaviorcontroller);
        initIdleActivity(behaviorcontroller);
        behaviorcontroller.setCoreActivities(Set.of(Activity.CORE));
        behaviorcontroller.setDefaultActivity(Activity.IDLE);
        behaviorcontroller.useDefaultActivity();
        return behaviorcontroller;
    }

    public static void updateActivity(CopperGolem coppergolem) {
        coppergolem.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    private static void initCoreActivity(BehaviorController<CopperGolem> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.CORE, 0, ImmutableList.of(new AnimalPanic(1.5F), new BehaviorLook(45, 90), new BehavorMove(), BehaviorInteractDoor.create(), new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS)));
    }

    private static void initIdleActivity(BehaviorController<CopperGolem> behaviorcontroller) {
        behaviorcontroller.addActivity(Activity.IDLE, ImmutableList.of(Pair.of(0, new TransportItemsBetweenContainers(1.0F, CopperGolemAi.TRANSPORT_ITEM_SOURCE_BLOCK, CopperGolemAi.TRANSPORT_ITEM_DESTINATION_BLOCK, 32, 8, getTargetReachedInteractions(), onTravelling(), shouldQueueForTarget())), Pair.of(1, SetEntityLookTargetSometimes.create(EntityTypes.PLAYER, 6.0F, UniformInt.of(40, 80))), Pair.of(2, new BehaviorGateSingle(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT), ImmutableList.of(Pair.of(BehaviorStrollRandomUnconstrained.stroll(1.0F, 2, 2), 1), Pair.of(new BehaviorNop(30, 60), 1))))));
    }

    private static Map<TransportItemsBetweenContainers.a, TransportItemsBetweenContainers.b> getTargetReachedInteractions() {
        return Map.of(TransportItemsBetweenContainers.a.PICKUP_ITEM, onReachedTargetInteraction(CopperGolemState.GETTING_ITEM, SoundEffects.COPPER_GOLEM_ITEM_GET), TransportItemsBetweenContainers.a.PICKUP_NO_ITEM, onReachedTargetInteraction(CopperGolemState.GETTING_NO_ITEM, SoundEffects.COPPER_GOLEM_ITEM_NO_GET), TransportItemsBetweenContainers.a.PLACE_ITEM, onReachedTargetInteraction(CopperGolemState.DROPPING_ITEM, SoundEffects.COPPER_GOLEM_ITEM_DROP), TransportItemsBetweenContainers.a.PLACE_NO_ITEM, onReachedTargetInteraction(CopperGolemState.DROPPING_NO_ITEM, SoundEffects.COPPER_GOLEM_ITEM_NO_DROP));
    }

    private static TransportItemsBetweenContainers.b onReachedTargetInteraction(CopperGolemState coppergolemstate, @Nullable SoundEffect soundeffect) {
        return (entitycreature, transportitemsbetweencontainers_d, integer) -> {
            if (entitycreature instanceof CopperGolem coppergolem) {
                IInventory iinventory = transportitemsbetweencontainers_d.container();

                if (integer == 1) {
                    iinventory.startOpen(coppergolem);
                    coppergolem.setOpenedChestPos(transportitemsbetweencontainers_d.pos());
                    coppergolem.setState(coppergolemstate);
                }

                if (integer == 9 && soundeffect != null) {
                    coppergolem.playSound(soundeffect);
                }

                if (integer == 60) {
                    if (iinventory.getEntitiesWithContainerOpen().contains(entitycreature)) {
                        iinventory.stopOpen(coppergolem);
                    }

                    coppergolem.clearOpenedChestPos();
                }
            }

        };
    }

    private static Consumer<EntityCreature> onTravelling() {
        return (entitycreature) -> {
            if (entitycreature instanceof CopperGolem coppergolem) {
                coppergolem.clearOpenedChestPos();
                coppergolem.setState(CopperGolemState.IDLE);
            }

        };
    }

    private static Predicate<TransportItemsBetweenContainers.d> shouldQueueForTarget() {
        return (transportitemsbetweencontainers_d) -> {
            TileEntity tileentity = transportitemsbetweencontainers_d.blockEntity();

            if (tileentity instanceof TileEntityChest tileentitychest) {
                return !tileentitychest.getEntitiesWithContainerOpen().isEmpty();
            } else {
                return false;
            }
        };
    }
}
