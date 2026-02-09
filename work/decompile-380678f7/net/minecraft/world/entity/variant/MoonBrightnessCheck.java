package net.minecraft.world.entity.variant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.CriterionConditionValue;

public record MoonBrightnessCheck(CriterionConditionValue.DoubleRange range) implements SpawnCondition {

    public static final MapCodec<MoonBrightnessCheck> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(CriterionConditionValue.DoubleRange.CODEC.fieldOf("range").forGetter(MoonBrightnessCheck::range)).apply(instance, MoonBrightnessCheck::new);
    });

    public boolean test(SpawnContext spawncontext) {
        return this.range.matches((double) spawncontext.level().getLevel().getMoonBrightness());
    }

    @Override
    public MapCodec<MoonBrightnessCheck> codec() {
        return MoonBrightnessCheck.MAP_CODEC;
    }
}
