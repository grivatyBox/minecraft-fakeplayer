package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.WorldGenFeatureTree;

public class FallenTreeConfiguration implements WorldGenFeatureConfiguration {

    public static final Codec<FallenTreeConfiguration> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(WorldGenFeatureStateProvider.CODEC.fieldOf("trunk_provider").forGetter((fallentreeconfiguration) -> {
            return fallentreeconfiguration.trunkProvider;
        }), IntProvider.codec(0, 16).fieldOf("log_length").forGetter((fallentreeconfiguration) -> {
            return fallentreeconfiguration.logLength;
        }), WorldGenFeatureTree.CODEC.listOf().fieldOf("stump_decorators").forGetter((fallentreeconfiguration) -> {
            return fallentreeconfiguration.stumpDecorators;
        }), WorldGenFeatureTree.CODEC.listOf().fieldOf("log_decorators").forGetter((fallentreeconfiguration) -> {
            return fallentreeconfiguration.logDecorators;
        })).apply(instance, FallenTreeConfiguration::new);
    });
    public final WorldGenFeatureStateProvider trunkProvider;
    public final IntProvider logLength;
    public final List<WorldGenFeatureTree> stumpDecorators;
    public final List<WorldGenFeatureTree> logDecorators;

    protected FallenTreeConfiguration(WorldGenFeatureStateProvider worldgenfeaturestateprovider, IntProvider intprovider, List<WorldGenFeatureTree> list, List<WorldGenFeatureTree> list1) {
        this.trunkProvider = worldgenfeaturestateprovider;
        this.logLength = intprovider;
        this.stumpDecorators = list;
        this.logDecorators = list1;
    }

    public static class a {

        private final WorldGenFeatureStateProvider trunkProvider;
        private final IntProvider logLength;
        private List<WorldGenFeatureTree> stumpDecorators = new ArrayList();
        private List<WorldGenFeatureTree> logDecorators = new ArrayList();

        public a(WorldGenFeatureStateProvider worldgenfeaturestateprovider, IntProvider intprovider) {
            this.trunkProvider = worldgenfeaturestateprovider;
            this.logLength = intprovider;
        }

        public FallenTreeConfiguration.a stumpDecorators(List<WorldGenFeatureTree> list) {
            this.stumpDecorators = list;
            return this;
        }

        public FallenTreeConfiguration.a logDecorators(List<WorldGenFeatureTree> list) {
            this.logDecorators = list;
            return this;
        }

        public FallenTreeConfiguration build() {
            return new FallenTreeConfiguration(this.trunkProvider, this.logLength, this.stumpDecorators, this.logDecorators);
        }
    }
}
