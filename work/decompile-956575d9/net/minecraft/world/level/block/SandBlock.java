package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.sounds.AmbientDesertBlockSoundsPlayer;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class SandBlock extends ColoredFallingBlock {

    public static final MapCodec<SandBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ColorRGBA.CODEC.fieldOf("falling_dust_color").forGetter((sandblock) -> {
            return sandblock.dustColor;
        }), propertiesCodec()).apply(instance, SandBlock::new);
    });

    @Override
    public MapCodec<SandBlock> codec() {
        return SandBlock.CODEC;
    }

    public SandBlock(ColorRGBA colorrgba, BlockBase.Info blockbase_info) {
        super(colorrgba, blockbase_info);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        AmbientDesertBlockSoundsPlayer.playAmbientBlockSounds(iblockdata, world, blockposition, randomsource);
    }
}
