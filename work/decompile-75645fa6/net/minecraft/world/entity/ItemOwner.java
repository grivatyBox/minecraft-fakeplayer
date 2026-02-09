package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public interface ItemOwner {

    World level();

    Vec3D position();

    float getVisualRotationYInDegrees();

    @Nullable
    default EntityLiving asLivingEntity() {
        return null;
    }

    static ItemOwner offsetFromOwner(ItemOwner itemowner, Vec3D vec3d) {
        return new ItemOwner.a(itemowner, vec3d);
    }

    public static record a(ItemOwner owner, Vec3D offset) implements ItemOwner {

        @Override
        public World level() {
            return this.owner.level();
        }

        @Override
        public Vec3D position() {
            return this.owner.position().add(this.offset);
        }

        @Override
        public float getVisualRotationYInDegrees() {
            return this.owner.getVisualRotationYInDegrees();
        }

        @Nullable
        @Override
        public EntityLiving asLivingEntity() {
            return this.owner.asLivingEntity();
        }
    }
}
