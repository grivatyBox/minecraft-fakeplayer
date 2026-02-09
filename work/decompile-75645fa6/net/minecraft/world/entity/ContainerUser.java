package net.minecraft.world.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;

public interface ContainerUser {

    boolean hasContainerOpen(ContainerOpenersCounter containeropenerscounter, BlockPosition blockposition);

    double getContainerInteractionRange();

    default EntityLiving getLivingEntity() {
        if (this instanceof EntityLiving) {
            return (EntityLiving) this;
        } else {
            throw new IllegalStateException("A container user must be a LivingEntity");
        }
    }
}
