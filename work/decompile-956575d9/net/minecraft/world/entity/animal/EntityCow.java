package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;

public class EntityCow extends AbstractCow {

    private static final DataWatcherObject<Holder<CowVariant>> DATA_VARIANT_ID = DataWatcher.<Holder<CowVariant>>defineId(EntityCow.class, DataWatcherRegistry.COW_VARIANT);

    public EntityCow(EntityTypes<? extends EntityCow> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityCow.DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), CowVariants.TEMPERATE));
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        VariantUtils.writeVariant(nbttagcompound, this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        VariantUtils.readVariant(nbttagcompound, this.registryAccess(), Registries.COW_VARIANT).ifPresent(this::setVariant);
    }

    @Nullable
    @Override
    public EntityCow getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        EntityCow entitycow = EntityTypes.COW.create(worldserver, EntitySpawnReason.BREEDING);

        if (entitycow != null && entityageable instanceof EntityCow entitycow1) {
            entitycow.setVariant(this.random.nextBoolean() ? this.getVariant() : entitycow1.getVariant());
        }

        return entitycow;
    }

    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EntitySpawnReason entityspawnreason, @Nullable GroupDataEntity groupdataentity) {
        CowVariants.selectVariantToSpawn(this.random, this.registryAccess(), SpawnContext.create(worldaccess, this.blockPosition())).ifPresent(this::setVariant);
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, entityspawnreason, groupdataentity);
    }

    public void setVariant(Holder<CowVariant> holder) {
        this.entityData.set(EntityCow.DATA_VARIANT_ID, holder);
    }

    public Holder<CowVariant> getVariant() {
        return (Holder) this.entityData.get(EntityCow.DATA_VARIANT_ID);
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> datacomponenttype) {
        return (T) (datacomponenttype == DataComponents.COW_VARIANT ? castComponentValue(datacomponenttype, this.getVariant()) : super.get(datacomponenttype));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        this.applyImplicitComponentIfPresent(datacomponentgetter, DataComponents.COW_VARIANT);
        super.applyImplicitComponents(datacomponentgetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> datacomponenttype, T t0) {
        if (datacomponenttype == DataComponents.COW_VARIANT) {
            this.setVariant((Holder) castComponentValue(DataComponents.COW_VARIANT, t0));
            return true;
        } else {
            return super.applyImplicitComponent(datacomponenttype, t0);
        }
    }
}
