package net.minecraft.gametest.framework;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.EnumBlockRotation;

public record TestData<EnvironmentType>(EnvironmentType environment, MinecraftKey structure, int maxTicks, int setupTicks, boolean required, EnumBlockRotation rotation, boolean manualOnly, int maxAttempts, int requiredSuccesses, boolean skyAccess) {

    public static final MapCodec<TestData<Holder<TestEnvironmentDefinition>>> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(TestEnvironmentDefinition.CODEC.fieldOf("environment").forGetter(TestData::environment), MinecraftKey.CODEC.fieldOf("structure").forGetter(TestData::structure), ExtraCodecs.POSITIVE_INT.fieldOf("max_ticks").forGetter(TestData::maxTicks), ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("setup_ticks", 0).forGetter(TestData::setupTicks), Codec.BOOL.optionalFieldOf("required", true).forGetter(TestData::required), EnumBlockRotation.CODEC.optionalFieldOf("rotation", EnumBlockRotation.NONE).forGetter(TestData::rotation), Codec.BOOL.optionalFieldOf("manual_only", false).forGetter(TestData::manualOnly), ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_attempts", 1).forGetter(TestData::maxAttempts), ExtraCodecs.POSITIVE_INT.optionalFieldOf("required_successes", 1).forGetter(TestData::requiredSuccesses), Codec.BOOL.optionalFieldOf("sky_access", false).forGetter(TestData::skyAccess)).apply(instance, TestData::new);
    });

    public TestData(EnvironmentType environmenttype, MinecraftKey minecraftkey, int i, int j, boolean flag, EnumBlockRotation enumblockrotation) {
        this(environmenttype, minecraftkey, i, j, flag, enumblockrotation, false, 1, 1, false);
    }

    public TestData(EnvironmentType environmenttype, MinecraftKey minecraftkey, int i, int j, boolean flag) {
        this(environmenttype, minecraftkey, i, j, flag, EnumBlockRotation.NONE);
    }

    public <T> TestData<T> map(Function<EnvironmentType, T> function) {
        return new TestData<T>(function.apply(this.environment), this.structure, this.maxTicks, this.setupTicks, this.required, this.rotation, this.manualOnly, this.maxAttempts, this.requiredSuccesses, this.skyAccess);
    }
}
