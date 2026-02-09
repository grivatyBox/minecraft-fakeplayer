package net.minecraft.util.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.UtilColor;
import net.minecraft.world.IInventory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorPositionEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorTarget;
import net.minecraft.world.entity.ai.memory.ExpirableMemory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryTarget;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.schedule.Activity;

public record DebugBrainDump(String name, String profession, int xp, float health, float maxHealth, String inventory, boolean wantsGolem, int angerLevel, List<String> activities, List<String> behaviors, List<String> memories, List<String> gossips, Set<BlockPosition> pois, Set<BlockPosition> potentialPois) {

    public static final StreamCodec<PacketDataSerializer, DebugBrainDump> STREAM_CODEC = StreamCodec.<PacketDataSerializer, DebugBrainDump>of((packetdataserializer, debugbraindump) -> {
        debugbraindump.write(packetdataserializer);
    }, DebugBrainDump::new);

    public DebugBrainDump(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(), packetdataserializer.readUtf(), packetdataserializer.readInt(), packetdataserializer.readFloat(), packetdataserializer.readFloat(), packetdataserializer.readUtf(), packetdataserializer.readBoolean(), packetdataserializer.readInt(), packetdataserializer.readList(PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readUtf), packetdataserializer.readList(PacketDataSerializer::readUtf), (Set) packetdataserializer.readCollection(HashSet::new, BlockPosition.STREAM_CODEC), (Set) packetdataserializer.readCollection(HashSet::new, BlockPosition.STREAM_CODEC));
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.name);
        packetdataserializer.writeUtf(this.profession);
        packetdataserializer.writeInt(this.xp);
        packetdataserializer.writeFloat(this.health);
        packetdataserializer.writeFloat(this.maxHealth);
        packetdataserializer.writeUtf(this.inventory);
        packetdataserializer.writeBoolean(this.wantsGolem);
        packetdataserializer.writeInt(this.angerLevel);
        packetdataserializer.writeCollection(this.activities, PacketDataSerializer::writeUtf);
        packetdataserializer.writeCollection(this.behaviors, PacketDataSerializer::writeUtf);
        packetdataserializer.writeCollection(this.memories, PacketDataSerializer::writeUtf);
        packetdataserializer.writeCollection(this.gossips, PacketDataSerializer::writeUtf);
        packetdataserializer.writeCollection(this.pois, BlockPosition.STREAM_CODEC);
        packetdataserializer.writeCollection(this.potentialPois, BlockPosition.STREAM_CODEC);
    }

    public static DebugBrainDump takeBrainDump(WorldServer worldserver, EntityLiving entityliving) {
        String s = DebugEntityNameGenerator.getEntityName((Entity) entityliving);
        String s1;
        int i;

        if (entityliving instanceof EntityVillager entityvillager) {
            s1 = entityvillager.getVillagerData().profession().getRegisteredName();
            i = entityvillager.getVillagerXp();
        } else {
            s1 = "";
            i = 0;
        }

        float f = entityliving.getHealth();
        float f1 = entityliving.getMaxHealth();
        BehaviorController<?> behaviorcontroller = entityliving.getBrain();
        long j = entityliving.level().getGameTime();
        String s2;

        if (entityliving instanceof InventoryCarrier inventorycarrier) {
            IInventory iinventory = inventorycarrier.getInventory();

            s2 = iinventory.isEmpty() ? "" : iinventory.toString();
        } else {
            s2 = "";
        }

        boolean flag;
        label36:
        {
            if (entityliving instanceof EntityVillager entityvillager1) {
                if (entityvillager1.wantsToSpawnGolem(j)) {
                    flag = 1;
                    break label36;
                }
            }

            flag = 0;
        }

        boolean flag1 = (boolean) flag;

        if (entityliving instanceof Warden warden) {
            flag = warden.getClientAngerLevel();
        } else {
            flag = -1;
        }

        int k = flag;
        List<String> list = behaviorcontroller.getActiveActivities().stream().map(Activity::getName).toList();
        List<String> list1 = behaviorcontroller.getRunningBehaviors().stream().map(BehaviorControl::debugString).toList();
        List<String> list2 = getMemoryDescriptions(worldserver, entityliving, j).map((s3) -> {
            return UtilColor.truncateStringIfNecessary(s3, 255, true);
        }).toList();
        Set<BlockPosition> set = getKnownBlockPositions(behaviorcontroller, MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT);
        Set<BlockPosition> set1 = getKnownBlockPositions(behaviorcontroller, MemoryModuleType.POTENTIAL_JOB_SITE);
        List list3;

        if (entityliving instanceof EntityVillager entityvillager2) {
            list3 = getVillagerGossips(entityvillager2);
        } else {
            list3 = List.of();
        }

        List<String> list4 = list3;

        return new DebugBrainDump(s, s1, i, f, f1, s2, flag1, k, list, list1, list2, list4, set, set1);
    }

    @SafeVarargs
    private static Set<BlockPosition> getKnownBlockPositions(BehaviorController<?> behaviorcontroller, MemoryModuleType<GlobalPos>... amemorymoduletype) {
        Stream stream = Stream.of(amemorymoduletype);

        Objects.requireNonNull(behaviorcontroller);
        stream = stream.filter(behaviorcontroller::hasMemoryValue);
        Objects.requireNonNull(behaviorcontroller);
        return (Set) stream.map(behaviorcontroller::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
    }

    private static List<String> getVillagerGossips(EntityVillager entityvillager) {
        List<String> list = new ArrayList();

        entityvillager.getGossips().getGossipEntries().forEach((uuid, object2intmap) -> {
            String s = DebugEntityNameGenerator.getEntityName(uuid);

            object2intmap.forEach((reputationtype, integer) -> {
                list.add(s + ": " + String.valueOf(reputationtype) + ": " + integer);
            });
        });
        return list;
    }

    private static Stream<String> getMemoryDescriptions(WorldServer worldserver, EntityLiving entityliving, long i) {
        return entityliving.getBrain().getMemories().entrySet().stream().map((entry) -> {
            MemoryModuleType<?> memorymoduletype = (MemoryModuleType) entry.getKey();
            Optional<? extends ExpirableMemory<?>> optional = (Optional) entry.getValue();

            return getMemoryDescription(worldserver, i, memorymoduletype, optional);
        }).sorted();
    }

    private static String getMemoryDescription(WorldServer worldserver, long i, MemoryModuleType<?> memorymoduletype, Optional<? extends ExpirableMemory<?>> optional) {
        String s;

        if (optional.isPresent()) {
            ExpirableMemory<?> expirablememory = (ExpirableMemory) optional.get();
            Object object = expirablememory.getValue();

            if (memorymoduletype == MemoryModuleType.HEARD_BELL_TIME) {
                long j = i - (Long) object;

                s = j + " ticks ago";
            } else if (expirablememory.canExpire()) {
                String s1 = getShortDescription(worldserver, object);

                s = s1 + " (ttl: " + expirablememory.getTimeToLive() + ")";
            } else {
                s = getShortDescription(worldserver, object);
            }
        } else {
            s = "-";
        }

        String s2 = BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memorymoduletype).getPath();

        return s2 + ": " + s;
    }

    private static String getShortDescription(WorldServer worldserver, @Nullable Object object) {
        byte b0 = 0;
        String s;

        //$FF: b0->value
        //0->java/util/UUID
        //1->net/minecraft/world/entity/Entity
        //2->net/minecraft/world/entity/ai/memory/MemoryTarget
        //3->net/minecraft/world/entity/ai/behavior/BehaviorPositionEntity
        //4->net/minecraft/core/GlobalPos
        //5->net/minecraft/world/entity/ai/behavior/BehaviorTarget
        //6->net/minecraft/world/damagesource/DamageSource
        //7->java/util/Collection
        switch (((Class)object).typeSwitch<invokedynamic>(object, b0)) {
            case -1:
                s = "-";
                break;
            case 0:
                UUID uuid = (UUID)object;

                s = getShortDescription(worldserver, worldserver.getEntity(uuid));
                break;
            case 1:
                Entity entity = (Entity)object;

                s = DebugEntityNameGenerator.getEntityName(entity);
                break;
            case 2:
                MemoryTarget memorytarget = (MemoryTarget)object;

                s = getShortDescription(worldserver, memorytarget.getTarget());
                break;
            case 3:
                BehaviorPositionEntity behaviorpositionentity = (BehaviorPositionEntity)object;

                s = getShortDescription(worldserver, behaviorpositionentity.getEntity());
                break;
            case 4:
                GlobalPos globalpos = (GlobalPos)object;

                s = getShortDescription(worldserver, globalpos.pos());
                break;
            case 5:
                BehaviorTarget behaviortarget = (BehaviorTarget)object;

                s = getShortDescription(worldserver, behaviortarget.currentBlockPosition());
                break;
            case 6:
                DamageSource damagesource = (DamageSource)object;
                Entity entity1 = damagesource.getEntity();

                s = entity1 == null ? object.toString() : getShortDescription(worldserver, entity1);
                break;
            case 7:
                Collection<?> collection = (Collection)object;

                s = "[" + (String)collection.stream().map((object1) -> {
                    return getShortDescription(worldserver, object1);
                }).collect(Collectors.joining(", ")) + "]";
                break;
            default:
                s = object.toString();
        }

        return s;
    }

    public boolean hasPoi(BlockPosition blockposition) {
        return this.pois.contains(blockposition);
    }

    public boolean hasPotentialPoi(BlockPosition blockposition) {
        return this.potentialPois.contains(blockposition);
    }
}
