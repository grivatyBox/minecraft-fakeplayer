package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureEmptyConfiguration;

public class WorldGenFeatureCoralClaw extends WorldGenFeatureCoral {

    public WorldGenFeatureCoralClaw(Codec<WorldGenFeatureEmptyConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean placeFeature(GeneratorAccess generatoraccess, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        if (!this.placeCoralBlock(generatoraccess, randomsource, blockposition, iblockdata)) {
            return false;
        } else {
            EnumDirection enumdirection = EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource);
            int i = randomsource.nextInt(2) + 2;
            List<EnumDirection> list = SystemUtils.toShuffledList(Stream.of(enumdirection, enumdirection.getClockWise(), enumdirection.getCounterClockWise()), randomsource);

            for (EnumDirection enumdirection1 : list.subList(0, i)) {
                BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();
                int j = randomsource.nextInt(2) + 1;

                blockposition_mutableblockposition.move(enumdirection1);
                int k;
                EnumDirection enumdirection2;

                if (enumdirection1 == enumdirection) {
                    enumdirection2 = enumdirection;
                    k = randomsource.nextInt(3) + 2;
                } else {
                    blockposition_mutableblockposition.move(EnumDirection.UP);
                    EnumDirection[] aenumdirection = new EnumDirection[]{enumdirection1, EnumDirection.UP};

                    enumdirection2 = (EnumDirection) SystemUtils.getRandom(aenumdirection, randomsource);
                    k = randomsource.nextInt(3) + 3;
                }

                for (int l = 0; l < j && this.placeCoralBlock(generatoraccess, randomsource, blockposition_mutableblockposition, iblockdata); ++l) {
                    blockposition_mutableblockposition.move(enumdirection2);
                }

                blockposition_mutableblockposition.move(enumdirection2.getOpposite());
                blockposition_mutableblockposition.move(EnumDirection.UP);

                for (int i1 = 0; i1 < k; ++i1) {
                    blockposition_mutableblockposition.move(enumdirection);
                    if (!this.placeCoralBlock(generatoraccess, randomsource, blockposition_mutableblockposition, iblockdata)) {
                        break;
                    }

                    if (randomsource.nextFloat() < 0.25F) {
                        blockposition_mutableblockposition.move(EnumDirection.UP);
                    }
                }
            }

            return true;
        }
    }
}
