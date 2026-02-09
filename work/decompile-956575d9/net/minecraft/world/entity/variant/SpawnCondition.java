package net.minecraft.world.entity.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;

public interface SpawnCondition extends PriorityProvider.b<SpawnContext> {

    Codec<SpawnCondition> CODEC = BuiltInRegistries.SPAWN_CONDITION_TYPE.byNameCodec().dispatch(SpawnCondition::codec, (mapcodec) -> {
        return mapcodec;
    });

    MapCodec<? extends SpawnCondition> codec();
}
