package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// CraftBukkit start
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public interface IEntityAngerable {

    String TAG_ANGER_TIME = "AngerTime";
    String TAG_ANGRY_AT = "AngryAt";

    int getRemainingPersistentAngerTime();

    void setRemainingPersistentAngerTime(int i);

    @Nullable
    UUID getPersistentAngerTarget();

    void setPersistentAngerTarget(@Nullable UUID uuid);

    void startPersistentAngerTimer();

    default void addPersistentAngerSaveData(ValueOutput valueoutput) {
        valueoutput.putInt("AngerTime", this.getRemainingPersistentAngerTime());
        valueoutput.storeNullable("AngryAt", UUIDUtil.CODEC, this.getPersistentAngerTarget());
    }

    default void readPersistentAngerSaveData(World world, ValueInput valueinput) {
        this.setRemainingPersistentAngerTime(valueinput.getIntOr("AngerTime", 0));
        if (world instanceof WorldServer worldserver) {
            UUID uuid = (UUID) valueinput.read("AngryAt", UUIDUtil.CODEC).orElse(null); // CraftBukkit - decompile error

            this.setPersistentAngerTarget(uuid);
            Entity entity = uuid != null ? worldserver.getEntity(uuid) : null;

            if (entity instanceof EntityLiving entityliving) {
                this.setTarget(entityliving, EntityTargetEvent.TargetReason.UNKNOWN, false); // CraftBukkit
            }

        }
    }

    default void updatePersistentAnger(WorldServer worldserver, boolean flag) {
        EntityLiving entityliving = this.getTarget();
        UUID uuid = this.getPersistentAngerTarget();

        if ((entityliving == null || entityliving.isDeadOrDying()) && uuid != null && worldserver.getEntity(uuid) instanceof EntityInsentient) {
            this.stopBeingAngry();
        } else {
            if (entityliving != null && !Objects.equals(uuid, entityliving.getUUID())) {
                this.setPersistentAngerTarget(entityliving.getUUID());
                this.startPersistentAngerTimer();
            }

            if (this.getRemainingPersistentAngerTime() > 0 && (entityliving == null || entityliving.getType() != EntityTypes.PLAYER || !flag)) {
                this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
                if (this.getRemainingPersistentAngerTime() == 0) {
                    this.stopBeingAngry();
                }
            }

        }
    }

    default boolean isAngryAt(EntityLiving entityliving, WorldServer worldserver) {
        return !this.canAttack(entityliving) ? false : (entityliving.getType() == EntityTypes.PLAYER && this.isAngryAtAllPlayers(worldserver) ? true : entityliving.getUUID().equals(this.getPersistentAngerTarget()));
    }

    default boolean isAngryAtAllPlayers(WorldServer worldserver) {
        return worldserver.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
    }

    default boolean isAngry() {
        return this.getRemainingPersistentAngerTime() > 0;
    }

    default void playerDied(WorldServer worldserver, EntityHuman entityhuman) {
        if (worldserver.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            if (entityhuman.getUUID().equals(this.getPersistentAngerTarget())) {
                this.stopBeingAngry();
            }
        }
    }

    default void forgetCurrentTargetAndRefreshUniversalAnger() {
        this.stopBeingAngry();
        this.startPersistentAngerTimer();
    }

    default void stopBeingAngry() {
        this.setLastHurtByMob((EntityLiving) null);
        this.setPersistentAngerTarget((UUID) null);
        this.setTarget((EntityLiving) null, org.bukkit.event.entity.EntityTargetEvent.TargetReason.FORGOT_TARGET, true); // CraftBukkit
        this.setRemainingPersistentAngerTime(0);
    }

    @Nullable
    EntityLiving getLastHurtByMob();

    void setLastHurtByMob(@Nullable EntityLiving entityliving);

    void setTarget(@Nullable EntityLiving entityliving);

    boolean setTarget(@Nullable EntityLiving entityliving, org.bukkit.event.entity.EntityTargetEvent.TargetReason reason, boolean fireEvent); // CraftBukkit

    boolean canAttack(EntityLiving entityliving);

    @Nullable
    EntityLiving getTarget();
}
