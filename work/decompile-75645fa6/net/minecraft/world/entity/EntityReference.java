package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameReferencingFileConverter;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EntityReference<StoredEntityType extends UniquelyIdentifyable> {

    private static final Codec<? extends EntityReference<?>> CODEC = UUIDUtil.CODEC.xmap(EntityReference::new, EntityReference::getUUID);
    private static final StreamCodec<ByteBuf, ? extends EntityReference<?>> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(EntityReference::new, EntityReference::getUUID);
    private Either<UUID, StoredEntityType> entity;

    public static <Type extends UniquelyIdentifyable> Codec<EntityReference<Type>> codec() {
        return EntityReference.CODEC;
    }

    public static <Type extends UniquelyIdentifyable> StreamCodec<ByteBuf, EntityReference<Type>> streamCodec() {
        return EntityReference.STREAM_CODEC;
    }

    private EntityReference(StoredEntityType storedentitytype) {
        this.entity = Either.right(storedentitytype);
    }

    private EntityReference(UUID uuid) {
        this.entity = Either.left(uuid);
    }

    @Nullable
    public static <T extends UniquelyIdentifyable> EntityReference<T> of(@Nullable T t0) {
        return t0 != null ? new EntityReference(t0) : null;
    }

    public static <T extends UniquelyIdentifyable> EntityReference<T> of(UUID uuid) {
        return new EntityReference<T>(uuid);
    }

    public UUID getUUID() {
        return (UUID) this.entity.map((uuid) -> {
            return uuid;
        }, UniquelyIdentifyable::getUUID);
    }

    @Nullable
    public StoredEntityType getEntity(UUIDLookup<? extends UniquelyIdentifyable> uuidlookup, Class<StoredEntityType> oclass) {
        Optional<StoredEntityType> optional = this.entity.right();

        if (optional.isPresent()) {
            StoredEntityType storedentitytype = (StoredEntityType) (optional.get());

            if (!storedentitytype.isRemoved()) {
                return storedentitytype;
            }

            this.entity = Either.left(storedentitytype.getUUID());
        }

        Optional<UUID> optional1 = this.entity.left();

        if (optional1.isPresent()) {
            StoredEntityType storedentitytype1 = this.resolve(uuidlookup.lookup((UUID) optional1.get()), oclass);

            if (storedentitytype1 != null && !storedentitytype1.isRemoved()) {
                this.entity = Either.right(storedentitytype1);
                return storedentitytype1;
            }
        }

        return null;
    }

    @Nullable
    public StoredEntityType getEntity(World world, Class<StoredEntityType> oclass) {
        if (EntityHuman.class.isAssignableFrom(oclass)) {
            Objects.requireNonNull(world);
            return (StoredEntityType) this.getEntity(world::getPlayerInAnyDimension, oclass);
        } else {
            Objects.requireNonNull(world);
            return (StoredEntityType) this.getEntity(world::getEntityInAnyDimension, oclass);
        }
    }

    @Nullable
    private StoredEntityType resolve(@Nullable UniquelyIdentifyable uniquelyidentifyable, Class<StoredEntityType> oclass) {
        return (StoredEntityType) (uniquelyidentifyable != null && oclass.isAssignableFrom(uniquelyidentifyable.getClass()) ? (UniquelyIdentifyable) oclass.cast(uniquelyidentifyable) : null);
    }

    public boolean matches(StoredEntityType storedentitytype) {
        return this.getUUID().equals(storedentitytype.getUUID());
    }

    public void store(ValueOutput valueoutput, String s) {
        valueoutput.store(s, UUIDUtil.CODEC, this.getUUID());
    }

    public static void store(@Nullable EntityReference<?> entityreference, ValueOutput valueoutput, String s) {
        if (entityreference != null) {
            entityreference.store(valueoutput, s);
        }

    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> StoredEntityType get(@Nullable EntityReference<StoredEntityType> entityreference, World world, Class<StoredEntityType> oclass) {
        return (StoredEntityType) (entityreference != null ? entityreference.getEntity(world, oclass) : null);
    }

    @Nullable
    public static Entity getEntity(@Nullable EntityReference<Entity> entityreference, World world) {
        return (Entity) get(entityreference, world, Entity.class);
    }

    @Nullable
    public static EntityLiving getLivingEntity(@Nullable EntityReference<EntityLiving> entityreference, World world) {
        return (EntityLiving) get(entityreference, world, EntityLiving.class);
    }

    @Nullable
    public static EntityHuman getPlayer(@Nullable EntityReference<EntityHuman> entityreference, World world) {
        return (EntityHuman) get(entityreference, world, EntityHuman.class);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> EntityReference<StoredEntityType> read(ValueInput valueinput, String s) {
        return (EntityReference) valueinput.read(s, codec()).orElse((Object) null);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> EntityReference<StoredEntityType> readWithOldOwnerConversion(ValueInput valueinput, String s, World world) {
        Optional<UUID> optional = valueinput.<UUID>read(s, UUIDUtil.CODEC);

        return optional.isPresent() ? of((UUID) optional.get()) : (EntityReference) valueinput.getString(s).map((s1) -> {
            return NameReferencingFileConverter.convertMobOwnerIfNecessary(world.getServer(), s1);
        }).map(EntityReference::new).orElse((Object) null);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else {
            boolean flag;

            if (object instanceof EntityReference) {
                EntityReference<?> entityreference = (EntityReference) object;

                if (this.getUUID().equals(entityreference.getUUID())) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        return this.getUUID().hashCode();
    }
}
