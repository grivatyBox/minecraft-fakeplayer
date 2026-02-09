package net.minecraft.gametest.framework;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public interface GameTestEnvironments {

    String DEFAULT = "default";
    ResourceKey<TestEnvironmentDefinition> DEFAULT_KEY = create("default");

    private static ResourceKey<TestEnvironmentDefinition> create(String s) {
        return ResourceKey.create(Registries.TEST_ENVIRONMENT, MinecraftKey.withDefaultNamespace(s));
    }

    static void bootstrap(BootstrapContext<TestEnvironmentDefinition> bootstrapcontext) {
        bootstrapcontext.register(GameTestEnvironments.DEFAULT_KEY, new TestEnvironmentDefinition.a(List.of()));
    }
}
