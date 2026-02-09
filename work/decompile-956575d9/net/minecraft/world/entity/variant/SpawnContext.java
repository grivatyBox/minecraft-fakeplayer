package net.minecraft.world.entity.variant;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.biome.BiomeBase;

public record SpawnContext(BlockPosition pos, WorldAccess level, Holder<BiomeBase> biome) {

    public static SpawnContext create(WorldAccess worldaccess, BlockPosition blockposition) {
        Holder<BiomeBase> holder = worldaccess.getBiome(blockposition);

        return new SpawnContext(blockposition, worldaccess, holder);
    }
}
