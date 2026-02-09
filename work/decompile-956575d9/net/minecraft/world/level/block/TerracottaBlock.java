package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.sounds.AmbientDesertBlockSoundsPlayer;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class TerracottaBlock extends Block {

    public static final MapCodec<TerracottaBlock> CODEC = simpleCodec(TerracottaBlock::new);

    @Override
    public MapCodec<TerracottaBlock> codec() {
        return TerracottaBlock.CODEC;
    }

    public TerracottaBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        AmbientDesertBlockSoundsPlayer.playAmbientBlockSounds(iblockdata, world, blockposition, randomsource);
    }
}
