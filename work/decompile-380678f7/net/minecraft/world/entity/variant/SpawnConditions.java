package net.minecraft.world.entity.variant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.IRegistry;

public class SpawnConditions {

    public SpawnConditions() {}

    public static MapCodec<? extends SpawnCondition> bootstrap(IRegistry<MapCodec<? extends SpawnCondition>> iregistry) {
        IRegistry.register(iregistry, "structure", StructureCheck.MAP_CODEC);
        IRegistry.register(iregistry, "moon_brightness", MoonBrightnessCheck.MAP_CODEC);
        return (MapCodec) IRegistry.register(iregistry, "biome", BiomeCheck.MAP_CODEC);
    }
}
