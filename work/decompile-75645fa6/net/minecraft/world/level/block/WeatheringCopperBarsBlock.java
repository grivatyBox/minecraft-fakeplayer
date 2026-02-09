package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class WeatheringCopperBarsBlock extends BlockIronBars implements WeatheringCopper {

    public static final MapCodec<WeatheringCopperBarsBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperBarsBlock::getAge), propertiesCodec()).apply(instance, WeatheringCopperBarsBlock::new);
    });
    private final WeatheringCopper.a weatherState;

    @Override
    public MapCodec<WeatheringCopperBarsBlock> codec() {
        return WeatheringCopperBarsBlock.CODEC;
    }

    protected WeatheringCopperBarsBlock(WeatheringCopper.a weatheringcopper_a, BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.weatherState = weatheringcopper_a;
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        this.changeOverTime(iblockdata, worldserver, blockposition, randomsource);
    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return WeatheringCopper.getNext(iblockdata.getBlock()).isPresent();
    }

    @Override
    public WeatheringCopper.a getAge() {
        return this.weatherState;
    }
}
