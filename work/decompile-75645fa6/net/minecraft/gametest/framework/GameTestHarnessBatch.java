package net.minecraft.gametest.framework;

import java.util.Collection;
import net.minecraft.core.Holder;

public record GameTestHarnessBatch(int index, Collection<GameTestHarnessInfo> gameTestInfos, Holder<TestEnvironmentDefinition> environment) {

    public GameTestHarnessBatch(int i, Collection<GameTestHarnessInfo> collection, Holder<TestEnvironmentDefinition> holder) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("A GameTestBatch must include at least one GameTestInfo!");
        } else {
            this.index = i;
            this.gameTestInfos = collection;
            this.environment = holder;
        }
    }
}
