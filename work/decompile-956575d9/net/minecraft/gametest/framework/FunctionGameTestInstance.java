package net.minecraft.gametest.framework;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.ResourceKey;

public class FunctionGameTestInstance extends GameTestInstance {

    public static final MapCodec<FunctionGameTestInstance> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ResourceKey.codec(Registries.TEST_FUNCTION).fieldOf("function").forGetter(FunctionGameTestInstance::function), TestData.CODEC.forGetter(GameTestInstance::info)).apply(instance, FunctionGameTestInstance::new);
    });
    private final ResourceKey<Consumer<GameTestHarnessHelper>> function;

    public FunctionGameTestInstance(ResourceKey<Consumer<GameTestHarnessHelper>> resourcekey, TestData<Holder<TestEnvironmentDefinition>> testdata) {
        super(testdata);
        this.function = resourcekey;
    }

    @Override
    public void run(GameTestHarnessHelper gametestharnesshelper) {
        ((Consumer) gametestharnesshelper.getLevel().registryAccess().get(this.function).map(Holder.c::value).orElseThrow(() -> {
            return new IllegalStateException("Trying to access missing test function: " + String.valueOf(this.function.location()));
        })).accept(gametestharnesshelper);
    }

    private ResourceKey<Consumer<GameTestHarnessHelper>> function() {
        return this.function;
    }

    @Override
    public MapCodec<FunctionGameTestInstance> codec() {
        return FunctionGameTestInstance.CODEC;
    }

    @Override
    protected IChatMutableComponent typeDescription() {
        return IChatBaseComponent.translatable("test_instance.type.function");
    }

    @Override
    public IChatBaseComponent describe() {
        return this.describeType().append((IChatBaseComponent) this.descriptionRow("test_instance.description.function", this.function.location().toString())).append(this.describeInfo());
    }
}
