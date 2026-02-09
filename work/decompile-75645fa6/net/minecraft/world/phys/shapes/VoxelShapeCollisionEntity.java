package net.minecraft.world.phys.shapes;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public class VoxelShapeCollisionEntity implements VoxelShapeCollision {

    private final boolean descending;
    private final double entityBottom;
    private final boolean placement;
    private final ItemStack heldItem;
    private final boolean alwaysCollideWithFluid;
    @Nullable
    private final Entity entity;

    protected VoxelShapeCollisionEntity(boolean flag, boolean flag1, double d0, ItemStack itemstack, boolean flag2, @Nullable Entity entity) {
        this.descending = flag;
        this.placement = flag1;
        this.entityBottom = d0;
        this.heldItem = itemstack;
        this.alwaysCollideWithFluid = flag2;
        this.entity = entity;
    }

    /** @deprecated */
    @Deprecated
    protected VoxelShapeCollisionEntity(Entity entity, boolean flag, boolean flag1) {
        boolean flag2 = entity.isDescending();
        double d0 = entity.getY();
        ItemStack itemstack;

        if (entity instanceof EntityLiving entityliving) {
            itemstack = entityliving.getMainHandItem();
        } else {
            itemstack = ItemStack.EMPTY;
        }

        this(flag2, flag1, d0, itemstack, flag, entity);
    }

    @Override
    public boolean isHoldingItem(Item item) {
        return this.heldItem.is(item);
    }

    @Override
    public boolean alwaysCollideWithFluid() {
        return this.alwaysCollideWithFluid;
    }

    @Override
    public boolean canStandOnFluid(Fluid fluid, Fluid fluid1) {
        Entity entity = this.entity;

        if (!(entity instanceof EntityLiving entityliving)) {
            return false;
        } else {
            return entityliving.canStandOnFluid(fluid1) && !fluid.getType().isSame(fluid1.getType());
        }
    }

    @Override
    public VoxelShape getCollisionShape(IBlockData iblockdata, ICollisionAccess icollisionaccess, BlockPosition blockposition) {
        return iblockdata.getCollisionShape(icollisionaccess, blockposition, this);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape voxelshape, BlockPosition blockposition, boolean flag) {
        return this.entityBottom > (double) blockposition.getY() + voxelshape.max(EnumDirection.EnumAxis.Y) - (double) 1.0E-5F;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public boolean isPlacement() {
        return this.placement;
    }

    protected static class a extends VoxelShapeCollisionEntity {

        protected static final VoxelShapeCollision WITHOUT_FLUID_COLLISIONS = new VoxelShapeCollisionEntity.a(false);
        protected static final VoxelShapeCollision WITH_FLUID_COLLISIONS = new VoxelShapeCollisionEntity.a(true);

        public a(boolean flag) {
            super(false, false, -Double.MAX_VALUE, ItemStack.EMPTY, flag, (Entity) null);
        }

        @Override
        public boolean isAbove(VoxelShape voxelshape, BlockPosition blockposition, boolean flag) {
            return flag;
        }
    }
}
