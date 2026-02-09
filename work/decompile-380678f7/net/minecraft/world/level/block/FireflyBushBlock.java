package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.HeightMap;

public class FireflyBushBlock extends VegetationBlock implements IBlockFragilePlantElement {

    private static final double FIREFLY_CHANCE_PER_TICK = 0.7D;
    private static final double FIREFLY_HORIZONTAL_RANGE = 10.0D;
    private static final double FIREFLY_VERTICAL_RANGE = 5.0D;
    private static final int FIREFLY_SPAWN_MAX_BRIGHTNESS_LEVEL = 13;
    private static final int FIREFLY_AMBIENT_SOUND_CHANCE_ONE_IN = 30;
    public static final MapCodec<FireflyBushBlock> CODEC = simpleCodec(FireflyBushBlock::new);

    public FireflyBushBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    protected MapCodec<? extends FireflyBushBlock> codec() {
        return FireflyBushBlock.CODEC;
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(30) == 0 && world.isMoonVisible() && world.getHeight(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, blockposition) <= blockposition.getY()) {
            world.playLocalSound(blockposition, SoundEffects.FIREFLY_BUSH_IDLE, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
        }

        if (world.getMaxLocalRawBrightness(blockposition) <= 13 && randomsource.nextDouble() <= 0.7D) {
            double d0 = (double) blockposition.getX() + randomsource.nextDouble() * 10.0D - 5.0D;
            double d1 = (double) blockposition.getY() + randomsource.nextDouble() * 5.0D;
            double d2 = (double) blockposition.getZ() + randomsource.nextDouble() * 10.0D - 5.0D;

            world.addParticle(Particles.FIREFLY, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }

    }

    @Override
    public boolean isValidBonemealTarget(IWorldReader iworldreader, BlockPosition blockposition, IBlockData iblockdata) {
        return IBlockFragilePlantElement.hasSpreadableNeighbourPos(iworldreader, blockposition, iblockdata);
    }

    @Override
    public boolean isBonemealSuccess(World world, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        return true;
    }

    @Override
    public void performBonemeal(WorldServer worldserver, RandomSource randomsource, BlockPosition blockposition, IBlockData iblockdata) {
        IBlockFragilePlantElement.findSpreadableNeighbourPos(worldserver, blockposition, iblockdata).ifPresent((blockposition1) -> {
            worldserver.setBlockAndUpdate(blockposition1, this.defaultBlockState());
        });
    }
}
