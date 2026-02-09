package net.minecraft.world.waypoints;

public interface WaypointManager<T extends Waypoint> {

    void trackWaypoint(T t0);

    void updateWaypoint(T t0);

    void untrackWaypoint(T t0);
}
