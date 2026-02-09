package net.minecraft.world.level.block.sounds;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.HeightMap;

public class AmbientDesertBlockSoundsPlayer {

    private static final int IDLE_SOUND_CHANCE = 1600;
    private static final int WIND_SOUND_CHANCE = 10000;
    private static final int SURROUNDING_BLOCKS_PLAY_SOUND_THRESHOLD = 3;
    private static final int SURROUNDING_BLOCKS_DISTANCE_CHECK = 8;

    public AmbientDesertBlockSoundsPlayer() {}

    public static void playAmbientBlockSounds(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if (iblockdata.is(TagsBlock.PLAYS_AMBIENT_DESERT_BLOCK_SOUNDS) && world.canSeeSky(blockposition.above())) {
            if (randomsource.nextInt(1600) == 0 && shouldPlayAmbientSound(world, blockposition)) {
                world.playLocalSound((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), SoundEffects.SAND_IDLE, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            }

            if (randomsource.nextInt(10000) == 0 && isInAmbientSoundBiome(world.getBiome(blockposition)) && shouldPlayAmbientSound(world, blockposition)) {
                world.playPlayerSound(SoundEffects.SAND_WIND, SoundCategory.AMBIENT, 1.0F, 1.0F);
            }

        }
    }

    private static boolean isInAmbientSoundBiome(Holder<BiomeBase> holder) {
        return holder.is(Biomes.DESERT) || holder.is(BiomeTags.IS_BADLANDS);
    }

    private static boolean shouldPlayAmbientSound(World world, BlockPosition blockposition) {
        int i = 0;

        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            BlockPosition blockposition1 = blockposition.relative(enumdirection, 8);
            IBlockData iblockdata = world.getBlockState(blockposition1.atY(world.getHeight(HeightMap.Type.WORLD_SURFACE, blockposition1) - 1));

            if (iblockdata.is(TagsBlock.PLAYS_AMBIENT_DESERT_BLOCK_SOUNDS)) {
                ++i;
                if (i >= 3) {
                    return true;
                }
            }
        }

        return false;
    }
}
