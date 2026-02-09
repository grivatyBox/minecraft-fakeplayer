package net.minecraft.world.level;

import net.minecraft.server.level.WorldServer;

public interface MobSpawner {

    void tick(WorldServer worldserver, boolean flag, boolean flag1);
}
