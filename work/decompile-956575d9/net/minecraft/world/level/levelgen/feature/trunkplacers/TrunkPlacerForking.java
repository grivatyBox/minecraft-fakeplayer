package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.VirtualLevelReadable;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.WorldGenFoilagePlacer;

public class TrunkPlacerForking extends TrunkPlacer {

    public static final MapCodec<TrunkPlacerForking> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return trunkPlacerParts(instance).apply(instance, TrunkPlacerForking::new);
    });

    public TrunkPlacerForking(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected TrunkPlacers<?> type() {
        return TrunkPlacers.FORKING_TRUNK_PLACER;
    }

    @Override
    public List<WorldGenFoilagePlacer.a> placeTrunk(VirtualLevelReadable virtuallevelreadable, BiConsumer<BlockPosition, IBlockData> biconsumer, RandomSource randomsource, int i, BlockPosition blockposition, WorldGenFeatureTreeConfiguration worldgenfeaturetreeconfiguration) {
        setDirtAt(virtuallevelreadable, biconsumer, randomsource, blockposition.below(), worldgenfeaturetreeconfiguration);
        List<WorldGenFoilagePlacer.a> list = Lists.newArrayList();
        EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
        int j = i - randomsource.nextInt(4) - 1;
        int k = 3 - randomsource.nextInt(3);
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        int l = blockposition.getX();
        int i1 = blockposition.getZ();
        OptionalInt optionalint = OptionalInt.empty();

        for (int j1 = 0; j1 < i; ++j1) {
            int k1 = blockposition.getY() + j1;

            if (j1 >= j && k > 0) {
                l += enumdirection.getStepX();
                i1 += enumdirection.getStepZ();
                --k;
            }

            if (this.placeLog(virtuallevelreadable, biconsumer, randomsource, blockposition_mutableblockposition.set(l, k1, i1), worldgenfeaturetreeconfiguration)) {
                optionalint = OptionalInt.of(k1 + 1);
            }
        }

        if (optionalint.isPresent()) {
            list.add(new WorldGenFoilagePlacer.a(new BlockPosition(l, optionalint.getAsInt(), i1), 1, false));
        }

        l = blockposition.getX();
        i1 = blockposition.getZ();
        EnumDirection enumdirection1 = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);

        if (enumdirection1 != enumdirection) {
            int l1 = j - randomsource.nextInt(2) - 1;
            int i2 = 1 + randomsource.nextInt(3);

            optionalint = OptionalInt.empty();

            for (int j2 = l1; j2 < i && i2 > 0; --i2) {
                if (j2 >= 1) {
                    int k2 = blockposition.getY() + j2;

                    l += enumdirection1.getStepX();
                    i1 += enumdirection1.getStepZ();
                    if (this.placeLog(virtuallevelreadable, biconsumer, randomsource, blockposition_mutableblockposition.set(l, k2, i1), worldgenfeaturetreeconfiguration)) {
                        optionalint = OptionalInt.of(k2 + 1);
                    }
                }

                ++j2;
            }

            if (optionalint.isPresent()) {
                list.add(new WorldGenFoilagePlacer.a(new BlockPosition(l, optionalint.getAsInt(), i1), 0, false));
            }
        }

        return list;
    }
}
