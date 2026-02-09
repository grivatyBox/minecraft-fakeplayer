package net.minecraft.world.level.block.sounds;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.HeightMap;

public class AmbientDesertBlockSoundsPlayer {

    private static final int IDLE_SOUND_CHANCE = 2100;
    private static final int DRY_GRASS_SOUND_CHANCE = 200;
    private static final int DEAD_BUSH_SOUND_CHANCE = 130;
    private static final int DEAD_BUSH_SOUND_BADLANDS_DECREASED_CHANCE = 3;
    private static final int SURROUNDING_BLOCKS_PLAY_SOUND_THRESHOLD = 3;
    private static final int SURROUNDING_BLOCKS_DISTANCE_HORIZONTAL_CHECK = 8;
    private static final int SURROUNDING_BLOCKS_DISTANCE_VERTICAL_CHECK = 5;
    private static final int HORIZONTAL_DIRECTIONS = 4;

    public AmbientDesertBlockSoundsPlayer() {}

    public static void playAmbientSandSounds(World world, BlockPosition blockposition, RandomSource randomsource) {
        if (world.getBlockState(blockposition.above()).is(Blocks.AIR)) {
            if (randomsource.nextInt(2100) == 0 && shouldPlayAmbientSandSound(world, blockposition)) {
                world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.SAND_IDLE, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            }

        }
    }

    public static void playAmbientDryGrassSounds(World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(200) == 0 && shouldPlayDesertDryVegetationBlockSounds(world, blockposition.below())) {
            world.playPlayerSound(SoundEffects.DRY_GRASS, SoundCategory.AMBIENT, 1.0F, 1.0F);
        }

    }

    public static void playAmbientDeadBushSounds(World world, BlockPosition blockposition, RandomSource randomsource) {
        if (randomsource.nextInt(130) == 0) {
            IBlockData iblockdata = world.getBlockState(blockposition.below());

            if ((iblockdata.is(Blocks.RED_SAND) || iblockdata.is(TagsBlock.TERRACOTTA)) && randomsource.nextInt(3) != 0) {
                return;
            }

            if (shouldPlayDesertDryVegetationBlockSounds(world, blockposition.below())) {
                world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.DEAD_BUSH_IDLE, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            }
        }

    }

    public static boolean shouldPlayDesertDryVegetationBlockSounds(World world, BlockPosition blockposition) {
        return world.getBlockState(blockposition).is(TagsBlock.TRIGGERS_AMBIENT_DESERT_DRY_VEGETATION_BLOCK_SOUNDS) && world.getBlockState(blockposition.below()).is(TagsBlock.TRIGGERS_AMBIENT_DESERT_DRY_VEGETATION_BLOCK_SOUNDS);
    }

    private static boolean shouldPlayAmbientSandSound(World world, BlockPosition blockposition) {
        int i = 0;
        int j = 0;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            blockposition_mutableblockposition.set(blockposition).move(enumdirection, 8);
            if (columnContainsTriggeringBlock(world, blockposition_mutableblockposition) && i++ >= 3) {
                return true;
            }

            ++j;
            int k = 4 - j;
            int l = k + i;
            boolean flag = l >= 3;

            if (!flag) {
                return false;
            }
        }

        return false;
    }

    private static boolean columnContainsTriggeringBlock(World world, BlockPosition.MutableBlockPosition blockposition_mutableblockposition) {
        int i = world.getHeight(HeightMap.Type.WORLD_SURFACE, blockposition_mutableblockposition) - 1;

        if (Math.abs(i - blockposition_mutableblockposition.getY()) > 5) {
            blockposition_mutableblockposition.move(EnumDirection.UP, 6);
            IBlockData iblockdata = world.getBlockState(blockposition_mutableblockposition);

            blockposition_mutableblockposition.move(EnumDirection.DOWN);

            for (int j = 0; j < 10; ++j) {
                IBlockData iblockdata1 = world.getBlockState(blockposition_mutableblockposition);

                if (iblockdata.isAir() && canTriggerAmbientDesertSandSounds(iblockdata1)) {
                    return true;
                }

                iblockdata = iblockdata1;
                blockposition_mutableblockposition.move(EnumDirection.DOWN);
            }

            return false;
        } else {
            boolean flag = world.getBlockState(blockposition_mutableblockposition.setY(i + 1)).isAir();

            return flag && canTriggerAmbientDesertSandSounds(world.getBlockState(blockposition_mutableblockposition.setY(i)));
        }
    }

    private static boolean canTriggerAmbientDesertSandSounds(IBlockData iblockdata) {
        return iblockdata.is(TagsBlock.TRIGGERS_AMBIENT_DESERT_SAND_BLOCK_SOUNDS);
    }
}
