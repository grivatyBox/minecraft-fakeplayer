package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameReferencingFileConverter;
import net.minecraft.world.level.World;
import net.minecraft.world.level.entity.UUIDLookup;
import net.minecraft.world.level.entity.UniquelyIdentifyable;

public class EntityReference<StoredEntityType extends UniquelyIdentifyable> {

    private static final Codec<? extends EntityReference<?>> CODEC = UUIDUtil.CODEC.xmap(EntityReference::new, EntityReference::getUUID);
    private static final StreamCodec<ByteBuf, ? extends EntityReference<?>> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(EntityReference::new, EntityReference::getUUID);
    private Either<UUID, StoredEntityType> entity;

    public static <Type extends UniquelyIdentifyable> Codec<EntityReference<Type>> codec() {
        return EntityReference.CODEC;
    }

    public static <Type extends UniquelyIdentifyable> StreamCodec<ByteBuf, EntityReference<Type>> streamCodec() {
        return EntityReference.STREAM_CODEC;
    }

    public EntityReference(StoredEntityType storedentitytype) {
        this.entity = Either.right(storedentitytype);
    }

    public EntityReference(UUID uuid) {
        this.entity = Either.left(uuid);
    }

    public UUID getUUID() {
        return (UUID) this.entity.map((uuid) -> {
            return uuid;
        }, UniquelyIdentifyable::getUUID);
    }

    @Nullable
    public StoredEntityType getEntity(UUIDLookup<? super StoredEntityType> uuidlookup, Class<StoredEntityType> oclass) {
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
            StoredEntityType storedentitytype1 = this.resolve(uuidlookup.getEntity((UUID) optional1.get()), oclass);

            if (storedentitytype1 != null && !storedentitytype1.isRemoved()) {
                this.entity = Either.right(storedentitytype1);
                return storedentitytype1;
            }
        }

        return null;
    }

    @Nullable
    private StoredEntityType resolve(@Nullable UniquelyIdentifyable uniquelyidentifyable, Class<StoredEntityType> oclass) {
        return (StoredEntityType) (uniquelyidentifyable != null && oclass.isAssignableFrom(uniquelyidentifyable.getClass()) ? (UniquelyIdentifyable) oclass.cast(uniquelyidentifyable) : null);
    }

    public boolean matches(StoredEntityType storedentitytype) {
        return this.getUUID().equals(storedentitytype.getUUID());
    }

    public void store(NBTTagCompound nbttagcompound, String s) {
        nbttagcompound.store(s, UUIDUtil.CODEC, this.getUUID());
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> StoredEntityType get(@Nullable EntityReference<StoredEntityType> entityreference, UUIDLookup<? super StoredEntityType> uuidlookup, Class<StoredEntityType> oclass) {
        return (StoredEntityType) (entityreference != null ? entityreference.getEntity(uuidlookup, oclass) : null);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> EntityReference<StoredEntityType> read(NBTTagCompound nbttagcompound, String s) {
        return (EntityReference) nbttagcompound.read(s, codec()).orElse((Object) null);
    }

    @Nullable
    public static <StoredEntityType extends UniquelyIdentifyable> EntityReference<StoredEntityType> readWithOldOwnerConversion(NBTTagCompound nbttagcompound, String s, World world) {
        Optional<UUID> optional = nbttagcompound.read(s, UUIDUtil.CODEC);

        return optional.isPresent() ? new EntityReference((UUID) optional.get()) : (EntityReference) nbttagcompound.getString(s).map((s1) -> {
            return NameReferencingFileConverter.convertMobOwnerIfNecessary(world.getServer(), s1);
        }).map(EntityReference::new).orElse((Object) null);
    }
}
