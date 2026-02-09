package net.minecraft.world.waypoints;

import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public interface WaypointStyleAssets {

    ResourceKey<? extends IRegistry<WaypointStyleAsset>> ROOT_ID = ResourceKey.createRegistryKey(MinecraftKey.withDefaultNamespace("waypoint_style_asset"));
    ResourceKey<WaypointStyleAsset> DEFAULT = createId("default");
    ResourceKey<WaypointStyleAsset> BOWTIE = createId("bowtie");

    static ResourceKey<WaypointStyleAsset> createId(String s) {
        return ResourceKey.create(WaypointStyleAssets.ROOT_ID, MinecraftKey.withDefaultNamespace(s));
    }
}
