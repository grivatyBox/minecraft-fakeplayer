package net.minecraft.world.level.border;

public interface IWorldBorderListener {

    void onSetSize(WorldBorder worldborder, double d0);

    void onLerpSize(WorldBorder worldborder, double d0, double d1, long i);

    void onSetCenter(WorldBorder worldborder, double d0, double d1);

    void onSetWarningTime(WorldBorder worldborder, int i);

    void onSetWarningBlocks(WorldBorder worldborder, int i);

    void onSetDamagePerBlock(WorldBorder worldborder, double d0);

    void onSetSafeZone(WorldBorder worldborder, double d0);
}
