package net.minecraft.gametest.framework;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class BuiltinTestFunctions extends TestFunctionLoader {

    public static final ResourceKey<Consumer<GameTestHarnessHelper>> ALWAYS_PASS = create("always_pass");
    public static final Consumer<GameTestHarnessHelper> ALWAYS_PASS_INSTANCE = GameTestHarnessHelper::succeed;

    public BuiltinTestFunctions() {}

    private static ResourceKey<Consumer<GameTestHarnessHelper>> create(String s) {
        return ResourceKey.create(Registries.TEST_FUNCTION, MinecraftKey.withDefaultNamespace(s));
    }

    public static Consumer<GameTestHarnessHelper> bootstrap(IRegistry<Consumer<GameTestHarnessHelper>> iregistry) {
        registerLoader(new BuiltinTestFunctions());
        runLoaders(iregistry);
        return BuiltinTestFunctions.ALWAYS_PASS_INSTANCE;
    }

    @Override
    public void load(BiConsumer<ResourceKey<Consumer<GameTestHarnessHelper>>, Consumer<GameTestHarnessHelper>> biconsumer) {
        biconsumer.accept(BuiltinTestFunctions.ALWAYS_PASS, BuiltinTestFunctions.ALWAYS_PASS_INSTANCE);
    }
}
