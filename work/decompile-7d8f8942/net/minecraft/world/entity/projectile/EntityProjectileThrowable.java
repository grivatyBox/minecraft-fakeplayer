package net.minecraft.world.entity.projectile;

import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class EntityProjectileThrowable extends EntityProjectile implements ItemSupplier {

    private static final DataWatcherObject<ItemStack> DATA_ITEM_STACK = DataWatcher.<ItemStack>defineId(EntityProjectileThrowable.class, DataWatcherRegistry.ITEM_STACK);

    public EntityProjectileThrowable(EntityTypes<? extends EntityProjectileThrowable> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityProjectileThrowable(EntityTypes<? extends EntityProjectileThrowable> entitytypes, double d0, double d1, double d2, World world, ItemStack itemstack) {
        super(entitytypes, d0, d1, d2, world);
        this.setItem(itemstack);
    }

    public EntityProjectileThrowable(EntityTypes<? extends EntityProjectileThrowable> entitytypes, EntityLiving entityliving, World world, ItemStack itemstack) {
        this(entitytypes, entityliving.getX(), entityliving.getEyeY() - (double) 0.1F, entityliving.getZ(), world, itemstack);
        this.setOwner(entityliving);
    }

    public void setItem(ItemStack itemstack) {
        this.getEntityData().set(EntityProjectileThrowable.DATA_ITEM_STACK, itemstack.copyWithCount(1));
    }

    protected abstract Item getDefaultItem();

    @Override
    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(EntityProjectileThrowable.DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityProjectileThrowable.DATA_ITEM_STACK, new ItemStack(this.getDefaultItem()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {
        super.addAdditionalSaveData(valueoutput);
        valueoutput.store("Item", ItemStack.CODEC, this.getItem());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {
        super.readAdditionalSaveData(valueinput);
        this.setItem((ItemStack) valueinput.read("Item", ItemStack.CODEC).orElseGet(() -> {
            return new ItemStack(this.getDefaultItem());
        }));
    }
}
