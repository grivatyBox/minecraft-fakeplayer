package net.minecraft.world.entity.animal;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;

public class EntitySalmon extends EntityFishSchool {

    private static final String TAG_TYPE = "type";
    private static final DataWatcherObject<Integer> DATA_TYPE = DataWatcher.<Integer>defineId(EntitySalmon.class, DataWatcherRegistry.INT);

    public EntitySalmon(EntityTypes<? extends EntitySalmon> entitytypes, World world) {
        super(entitytypes, world);
        this.refreshDimensions();
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.SALMON_AMBIENT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.SALMON_DEATH;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.SALMON_HURT;
    }

    @Override
    protected SoundEffect getFlopSound() {
        return SoundEffects.SALMON_FLOP;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntitySalmon.DATA_TYPE, EntitySalmon.Variant.DEFAULT.id());
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        super.onSyncedDataUpdated(datawatcherobject);
        if (EntitySalmon.DATA_TYPE.equals(datawatcherobject)) {
            this.refreshDimensions();
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.store("type", EntitySalmon.Variant.CODEC, this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setVariant((EntitySalmon.Variant) nbttagcompound.read("type", EntitySalmon.Variant.CODEC).orElse(EntitySalmon.Variant.DEFAULT));
    }

    @Override
    public void saveToBucketTag(ItemStack itemstack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemstack);
        itemstack.copyFrom(DataComponents.SALMON_SIZE, this);
    }

    public void setVariant(EntitySalmon.Variant entitysalmon_variant) {
        this.entityData.set(EntitySalmon.DATA_TYPE, entitysalmon_variant.id);
    }

    public EntitySalmon.Variant getVariant() {
        return (EntitySalmon.Variant) EntitySalmon.Variant.BY_ID.apply((Integer) this.entityData.get(EntitySalmon.DATA_TYPE));
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> datacomponenttype) {
        return (T) (datacomponenttype == DataComponents.SALMON_SIZE ? castComponentValue(datacomponenttype, this.getVariant()) : super.get(datacomponenttype));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        this.applyImplicitComponentIfPresent(datacomponentgetter, DataComponents.SALMON_SIZE);
        super.applyImplicitComponents(datacomponentgetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> datacomponenttype, T t0) {
        if (datacomponenttype == DataComponents.SALMON_SIZE) {
            this.setVariant((EntitySalmon.Variant) castComponentValue(DataComponents.SALMON_SIZE, t0));
            return true;
        } else {
            return super.applyImplicitComponent(datacomponenttype, t0);
        }
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        WeightedList.a<EntitySalmon.Variant> weightedlist_a = WeightedList.<EntitySalmon.Variant>builder();

        weightedlist_a.add(EntitySalmon.Variant.SMALL, 30);
        weightedlist_a.add(EntitySalmon.Variant.MEDIUM, 50);
        weightedlist_a.add(EntitySalmon.Variant.LARGE, 15);
        weightedlist_a.build().getRandom(this.random).ifPresent(this::setVariant);
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public float getSalmonScale() {
        return this.getVariant().boundingBoxScale;
    }

    @Override
    protected EntitySize getDefaultDimensions(EntityPose entitypose) {
        return super.getDefaultDimensions(entitypose).scale(this.getSalmonScale());
    }

    public static enum Variant implements INamable {

        SMALL("small", 0, 0.5F), MEDIUM("medium", 1, 1.0F), LARGE("large", 2, 1.5F);

        public static final EntitySalmon.Variant DEFAULT = EntitySalmon.Variant.MEDIUM;
        public static final INamable.a<EntitySalmon.Variant> CODEC = INamable.<EntitySalmon.Variant>fromEnum(EntitySalmon.Variant::values);
        static final IntFunction<EntitySalmon.Variant> BY_ID = ByIdMap.<EntitySalmon.Variant>continuous(EntitySalmon.Variant::id, values(), ByIdMap.a.CLAMP);
        public static final StreamCodec<ByteBuf, EntitySalmon.Variant> STREAM_CODEC = ByteBufCodecs.idMapper(EntitySalmon.Variant.BY_ID, EntitySalmon.Variant::id);
        private final String name;
        final int id;
        final float boundingBoxScale;

        private Variant(final String s, final int i, final float f) {
            this.name = s;
            this.id = i;
            this.boundingBoxScale = f;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        int id() {
            return this.id;
        }
    }
}
