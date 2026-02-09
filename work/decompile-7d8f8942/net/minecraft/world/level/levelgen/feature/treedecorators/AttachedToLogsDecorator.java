package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProvider;

public class AttachedToLogsDecorator extends WorldGenFeatureTree {

    public static final MapCodec<AttachedToLogsDecorator> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((attachedtologsdecorator) -> {
            return attachedtologsdecorator.probability;
        }), WorldGenFeatureStateProvider.CODEC.fieldOf("block_provider").forGetter((attachedtologsdecorator) -> {
            return attachedtologsdecorator.blockProvider;
        }), ExtraCodecs.nonEmptyList(EnumDirection.CODEC.listOf()).fieldOf("directions").forGetter((attachedtologsdecorator) -> {
            return attachedtologsdecorator.directions;
        })).apply(instance, AttachedToLogsDecorator::new);
    });
    private final float probability;
    private final WorldGenFeatureStateProvider blockProvider;
    private final List<EnumDirection> directions;

    public AttachedToLogsDecorator(float f, WorldGenFeatureStateProvider worldgenfeaturestateprovider, List<EnumDirection> list) {
        this.probability = f;
        this.blockProvider = worldgenfeaturestateprovider;
        this.directions = list;
    }

    @Override
    public void place(WorldGenFeatureTree.a worldgenfeaturetree_a) {
        RandomSource randomsource = worldgenfeaturetree_a.random();

        for (BlockPosition blockposition : SystemUtils.shuffledCopy(worldgenfeaturetree_a.logs(), randomsource)) {
            EnumDirection enumdirection = (EnumDirection) SystemUtils.getRandom(this.directions, randomsource);
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (randomsource.nextFloat() <= this.probability && worldgenfeaturetree_a.isAir(blockposition1)) {
                worldgenfeaturetree_a.setBlock(blockposition1, this.blockProvider.getState(randomsource, blockposition1));
            }
        }

    }

    @Override
    protected WorldGenFeatureTrees<?> type() {
        return WorldGenFeatureTrees.ATTACHED_TO_LOGS;
    }
}
