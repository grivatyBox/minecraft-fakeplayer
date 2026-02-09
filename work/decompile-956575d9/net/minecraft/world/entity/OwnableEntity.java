package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.world.level.World;

public interface OwnableEntity {

    @Nullable
    EntityReference<EntityLiving> getOwnerReference();

    World level();

    @Nullable
    default EntityLiving getOwner() {
        return (EntityLiving) EntityReference.get(this.getOwnerReference(), this.level(), EntityLiving.class);
    }

    @Nullable
    default EntityLiving getRootOwner() {
        Set<Object> set = new ObjectArraySet();
        EntityLiving entityliving = this.getOwner();

        set.add(this);

        while (entityliving instanceof OwnableEntity) {
            OwnableEntity ownableentity = (OwnableEntity) entityliving;
            EntityLiving entityliving1 = ownableentity.getOwner();

            if (set.contains(entityliving1)) {
                return null;
            }

            set.add(entityliving);
            entityliving = ownableentity.getOwner();
        }

        return entityliving;
    }
}
