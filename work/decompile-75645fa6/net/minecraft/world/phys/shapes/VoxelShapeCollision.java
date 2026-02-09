package net.minecraft.world.phys.shapes;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public interface VoxelShapeCollision {

    static VoxelShapeCollision empty() {
        return VoxelShapeCollisionEntity.a.WITHOUT_FLUID_COLLISIONS;
    }

    static VoxelShapeCollision emptyWithFluidCollisions() {
        return VoxelShapeCollisionEntity.a.WITH_FLUID_COLLISIONS;
    }

    static VoxelShapeCollision of(Entity entity) {
        Objects.requireNonNull(entity);
        byte b0 = 0;
        Object object;

        //$FF: b0->value
        //0->net/minecraft/world/entity/vehicle/EntityMinecartAbstract
        switch (entity.typeSwitch<invokedynamic>(entity, b0)) {
            case 0:
                EntityMinecartAbstract entityminecartabstract = (EntityMinecartAbstract)entity;

                object = EntityMinecartAbstract.useExperimentalMovement(entityminecartabstract.level()) ? new MinecartCollisionContext(entityminecartabstract, false) : new VoxelShapeCollisionEntity(entity, false, false);
                break;
            default:
                object = new VoxelShapeCollisionEntity(entity, false, false);
        }

        return (VoxelShapeCollision)object;
    }

    static VoxelShapeCollision of(Entity entity, boolean flag) {
        return new VoxelShapeCollisionEntity(entity, flag, false);
    }

    static VoxelShapeCollision placementContext(@Nullable EntityHuman entityhuman) {
        return new VoxelShapeCollisionEntity(entityhuman != null ? entityhuman.isDescending() : false, true, entityhuman != null ? entityhuman.getY() : -Double.MAX_VALUE, entityhuman instanceof EntityLiving ? ((EntityLiving) entityhuman).getMainHandItem() : ItemStack.EMPTY, false, entityhuman);
    }

    static VoxelShapeCollision withPosition(@Nullable Entity entity, double d0) {
        VoxelShapeCollisionEntity voxelshapecollisionentity = new VoxelShapeCollisionEntity;
        boolean flag = entity != null ? entity.isDescending() : false;
        double d1 = entity != null ? d0 : -Double.MAX_VALUE;
        ItemStack itemstack;

        if (entity instanceof EntityLiving entityliving) {
            itemstack = entityliving.getMainHandItem();
        } else {
            itemstack = ItemStack.EMPTY;
        }

        voxelshapecollisionentity.<init>(flag, true, d1, itemstack, false, entity);
        return voxelshapecollisionentity;
    }

    boolean isDescending();

    boolean isAbove(VoxelShape voxelshape, BlockPosition blockposition, boolean flag);

    boolean isHoldingItem(Item item);

    boolean alwaysCollideWithFluid();

    boolean canStandOnFluid(Fluid fluid, Fluid fluid1);

    VoxelShape getCollisionShape(IBlockData iblockdata, ICollisionAccess icollisionaccess, BlockPosition blockposition);

    default boolean isPlacement() {
        return false;
    }
}
