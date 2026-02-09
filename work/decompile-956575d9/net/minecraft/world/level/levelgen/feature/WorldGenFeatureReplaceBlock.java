package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureOreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureReplaceBlockConfiguration;

public class WorldGenFeatureReplaceBlock extends WorldGenerator<WorldGenFeatureReplaceBlockConfiguration> {

    public WorldGenFeatureReplaceBlock(Codec<WorldGenFeatureReplaceBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureReplaceBlockConfiguration> featureplacecontext) {
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();
        BlockPosition blockposition = featureplacecontext.origin();
        WorldGenFeatureReplaceBlockConfiguration worldgenfeaturereplaceblockconfiguration = featureplacecontext.config();

        for (WorldGenFeatureOreConfiguration.a worldgenfeatureoreconfiguration_a : worldgenfeaturereplaceblockconfiguration.targetStates) {
            if (worldgenfeatureoreconfiguration_a.target.test(generatoraccessseed.getBlockState(blockposition), featureplacecontext.random())) {
                generatoraccessseed.setBlock(blockposition, worldgenfeatureoreconfiguration_a.state, 2);
                break;
            }
        }

        return true;
    }
}
