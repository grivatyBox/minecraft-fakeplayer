package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;

public interface IBlockFragilePlantElement {

    boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata);

    boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata);

    void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata);

    static boolean hasSpreadableNeighbourPos(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return getSpreadableNeighbourPos(EnumDirection.EnumDirectionLimit.HORIZONTAL.stream().toList(), iworldreader, blockposition, iblockdata).isPresent();
    }

    static Optional<BlockPosition> findSpreadableNeighbourPos(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return getSpreadableNeighbourPos(EnumDirection.EnumDirectionLimit.HORIZONTAL.shuffledCopy(world.random), world, blockposition, iblockdata);
    }

    private static Optional<BlockPosition> getSpreadableNeighbourPos(List<EnumDirection> list, IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        for (EnumDirection enumdirection : list) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (iworldreader.isEmptyBlock(blockposition1) && iblockdata.canSurvive(iworldreader, blockposition1)) {
                return Optional.of(blockposition1);
            }
        }

        return Optional.empty();
    }

    default BlockPosition getParticlePos(BlockPosition blockposition) {
        BlockPosition blockposition1;

        switch (this.getType().ordinal()) {
            case 0:
                blockposition1 = blockposition.above();
                break;
            case 1:
                blockposition1 = blockposition;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return blockposition1;
    }

    default IBlockFragilePlantElement.a getType() {
        return IBlockFragilePlantElement.a.GROWER;
    }

    public static enum a {

        NEIGHBOR_SPREADER, GROWER;

        private a() {}
    }
}
