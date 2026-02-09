package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.IEntitySelector;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.EntityHuman;

public class SensorNearestPlayers extends Sensor<EntityLiving> {

    public SensorNearestPlayers() {}

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS);
    }

    @Override
    protected void doTick(WorldServer worldserver, EntityLiving entityliving) {
        Stream stream = worldserver.players().stream().filter(IEntitySelector.NO_SPECTATORS).filter((entityplayer) -> {
            return entityliving.closerThan(entityplayer, this.getFollowDistance(entityliving));
        });

        Objects.requireNonNull(entityliving);
        List<EntityHuman> list = (List) stream.sorted(Comparator.comparingDouble(entityliving::distanceToSqr)).collect(Collectors.toList());
        BehaviorController<?> behaviorcontroller = entityliving.getBrain();

        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
        List<EntityHuman> list1 = (List) list.stream().filter((entityhuman) -> {
            return isEntityTargetable(worldserver, entityliving, entityhuman);
        }).collect(Collectors.toList());

        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list1.isEmpty() ? null : (EntityHuman) list1.get(0));
        List<EntityHuman> list2 = list1.stream().filter((entityhuman) -> {
            return isEntityAttackable(worldserver, entityliving, entityhuman);
        }).toList();

        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS, list2);
        behaviorcontroller.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, list2.isEmpty() ? null : (EntityHuman) list2.get(0));
    }

    protected double getFollowDistance(EntityLiving entityliving) {
        return entityliving.getAttributeValue(GenericAttributes.FOLLOW_RANGE);
    }
}
