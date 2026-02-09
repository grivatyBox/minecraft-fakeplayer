package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;

public abstract class TestFunctionLoader {

    private static final List<TestFunctionLoader> loaders = new ArrayList();

    public TestFunctionLoader() {}

    public static void registerLoader(TestFunctionLoader testfunctionloader) {
        TestFunctionLoader.loaders.add(testfunctionloader);
    }

    public static void runLoaders(IRegistry<Consumer<GameTestHarnessHelper>> iregistry) {
        for (TestFunctionLoader testfunctionloader : TestFunctionLoader.loaders) {
            testfunctionloader.load((resourcekey, consumer) -> {
                IRegistry.register(iregistry, resourcekey, consumer);
            });
        }

    }

    public abstract void load(BiConsumer<ResourceKey<Consumer<GameTestHarnessHelper>>, Consumer<GameTestHarnessHelper>> biconsumer);
}
