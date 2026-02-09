package net.minecraft.world.phys.shapes;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.vehicle.EntityMinecartAbstract;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ICollisionAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;

public interface VoxelShapeCollision {

    static VoxelShapeCollision empty() {
        return VoxelShapeCollisionEntity.EMPTY;
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

    static VoxelShapeCollision placementContext(@Nullable Entity entity) {
        VoxelShapeCollisionEntity voxelshapecollisionentity = new VoxelShapeCollisionEntity;
        boolean flag = entity != null ? entity.isDescending() : false;
        double d0 = entity != null ? entity.getY() : -Double.MAX_VALUE;
        ItemStack itemstack;

        if (entity instanceof EntityLiving entityliving) {
            itemstack = entityliving.getMainHandItem();
        } else {
            itemstack = ItemStack.EMPTY;
        }

        Predicate predicate;

        if (entity instanceof EntityLiving entityliving1) {
            predicate = (fluid) -> {
                return entityliving1.canStandOnFluid(fluid);
            };
        } else {
            predicate = (fluid) -> {
                return false;
            };
        }

        voxelshapecollisionentity.<init>(flag, true, d0, itemstack, predicate, entity);
        return voxelshapecollisionentity;
    }

    boolean isDescending();

    boolean isAbove(VoxelShape voxelshape, BlockPosition blockposition, boolean flag);

    boolean isHoldingItem(Item item);

    boolean canStandOnFluid(Fluid fluid, Fluid fluid1);

    VoxelShape getCollisionShape(IBlockData iblockdata, ICollisionAccess icollisionaccess, BlockPosition blockposition);

    default boolean isPlacement() {
        return false;
    }
}
