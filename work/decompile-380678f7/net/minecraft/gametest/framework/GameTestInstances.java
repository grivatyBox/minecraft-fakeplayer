package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public interface GameTestInstances {

    ResourceKey<GameTestInstance> ALWAYS_PASS = create("always_pass");

    static void bootstrap(BootstrapContext<GameTestInstance> bootstrapcontext) {
        HolderGetter<Consumer<GameTestHarnessHelper>> holdergetter = bootstrapcontext.<Consumer<GameTestHarnessHelper>>lookup(Registries.TEST_FUNCTION);
        HolderGetter<TestEnvironmentDefinition> holdergetter1 = bootstrapcontext.<TestEnvironmentDefinition>lookup(Registries.TEST_ENVIRONMENT);

        bootstrapcontext.register(GameTestInstances.ALWAYS_PASS, new FunctionGameTestInstance(BuiltinTestFunctions.ALWAYS_PASS, new TestData(holdergetter1.getOrThrow(GameTestEnvironments.DEFAULT_KEY), MinecraftKey.withDefaultNamespace("empty"), 1, 1, false)));
    }

    private static ResourceKey<GameTestInstance> create(String s) {
        return ResourceKey.create(Registries.TEST_INSTANCE, MinecraftKey.withDefaultNamespace(s));
    }
}
