package net.minecraft.world.entity.animal.coppergolem;

import java.util.Map;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.level.block.WeatheringCopper;

public class CopperGolemOxidationLevels {

    private static final CopperGolemOxidationLevel UNAFFECTED = new CopperGolemOxidationLevel(SoundEffects.COPPER_GOLEM_SPIN, SoundEffects.COPPER_GOLEM_HURT, SoundEffects.COPPER_GOLEM_DEATH, SoundEffects.COPPER_GOLEM_STEP, MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/copper_golem.png"), MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/copper_golem_eyes.png"));
    private static final CopperGolemOxidationLevel EXPOSED = new CopperGolemOxidationLevel(SoundEffects.COPPER_GOLEM_SPIN, SoundEffects.COPPER_GOLEM_HURT, SoundEffects.COPPER_GOLEM_DEATH, SoundEffects.COPPER_GOLEM_STEP, MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/exposed_copper_golem.png"), MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/exposed_copper_golem_eyes.png"));
    private static final CopperGolemOxidationLevel WEATHERED = new CopperGolemOxidationLevel(SoundEffects.COPPER_GOLEM_WEATHERED_SPIN, SoundEffects.COPPER_GOLEM_WEATHERED_HURT, SoundEffects.COPPER_GOLEM_WEATHERED_DEATH, SoundEffects.COPPER_GOLEM_WEATHERED_STEP, MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/weathered_copper_golem.png"), MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/weathered_copper_golem_eyes.png"));
    private static final CopperGolemOxidationLevel OXIDIZED = new CopperGolemOxidationLevel(SoundEffects.COPPER_GOLEM_OXIDIZED_SPIN, SoundEffects.COPPER_GOLEM_OXIDIZED_HURT, SoundEffects.COPPER_GOLEM_OXIDIZED_DEATH, SoundEffects.COPPER_GOLEM_OXIDIZED_STEP, MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/oxidized_copper_golem.png"), MinecraftKey.withDefaultNamespace("textures/entity/copper_golem/oxidized_copper_golem_eyes.png"));
    private static final Map<WeatheringCopper.a, CopperGolemOxidationLevel> WEATHERED_STATES = Map.of(WeatheringCopper.a.UNAFFECTED, CopperGolemOxidationLevels.UNAFFECTED, WeatheringCopper.a.EXPOSED, CopperGolemOxidationLevels.EXPOSED, WeatheringCopper.a.WEATHERED, CopperGolemOxidationLevels.WEATHERED, WeatheringCopper.a.OXIDIZED, CopperGolemOxidationLevels.OXIDIZED);

    public CopperGolemOxidationLevels() {}

    public static CopperGolemOxidationLevel getOxidationLevel(WeatheringCopper.a weatheringcopper_a) {
        return (CopperGolemOxidationLevel) CopperGolemOxidationLevels.WEATHERED_STATES.get(weatheringcopper_a);
    }
}
