package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPosition;
import net.minecraft.tags.TagsFluid;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.level.pathfinder.PathfinderNormal;

public class PathfinderGoalUtil {

    public PathfinderGoalUtil() {}

    public static boolean hasGroundPathNavigation(EntityInsentient entityinsentient) {
        return entityinsentient.getNavigation().canNavigateGround();
    }

    public static boolean mobRestricted(EntityCreature entitycreature, int i) {
        return entitycreature.hasHome() && entitycreature.getHomePosition().closerToCenterThan(entitycreature.position(), (double) (entitycreature.getHomeRadius() + i + 1));
    }

    public static boolean isOutsideLimits(BlockPosition blockposition, EntityCreature entitycreature) {
        return entitycreature.level().isOutsideBuildHeight(blockposition.getY());
    }

    public static boolean isRestricted(boolean flag, EntityCreature entitycreature, BlockPosition blockposition) {
        return flag && !entitycreature.isWithinHome(blockposition);
    }

    public static boolean isNotStable(NavigationAbstract navigationabstract, BlockPosition blockposition) {
        return !navigationabstract.isStableDestination(blockposition);
    }

    public static boolean isWater(EntityCreature entitycreature, BlockPosition blockposition) {
        return entitycreature.level().getFluidState(blockposition).is(TagsFluid.WATER);
    }

    public static boolean hasMalus(EntityCreature entitycreature, BlockPosition blockposition) {
        return entitycreature.getPathfindingMalus(PathfinderNormal.getPathTypeStatic((EntityInsentient) entitycreature, blockposition)) != 0.0F;
    }

    public static boolean isSolid(EntityCreature entitycreature, BlockPosition blockposition) {
        return entitycreature.level().getBlockState(blockposition).isSolid();
    }
}
