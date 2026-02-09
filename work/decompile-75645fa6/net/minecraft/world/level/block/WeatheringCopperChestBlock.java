package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityChest;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertyChestType;

public class WeatheringCopperChestBlock extends CopperChestBlock implements WeatheringCopper {

    public static final MapCodec<WeatheringCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(WeatheringCopper.a.CODEC.fieldOf("weathering_state").forGetter(CopperChestBlock::getState), BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(BlockChest::getOpenChestSound), BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(BlockChest::getCloseChestSound), propertiesCodec()).apply(instance, WeatheringCopperChestBlock::new);
    });

    @Override
    public MapCodec<WeatheringCopperChestBlock> codec() {
        return WeatheringCopperChestBlock.CODEC;
    }

    public WeatheringCopperChestBlock(WeatheringCopper.a weatheringcopper_a, SoundEffect soundeffect, SoundEffect soundeffect1, BlockBase.Info blockbase_info) {
        super(weatheringcopper_a, soundeffect, soundeffect1, blockbase_info);
    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return WeatheringCopper.getNext(iblockdata.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if (!((BlockPropertyChestType) iblockdata.getValue(BlockChest.TYPE)).equals(BlockPropertyChestType.RIGHT)) {
            TileEntity tileentity = worldserver.getBlockEntity(blockposition);

            if (tileentity instanceof TileEntityChest) {
                TileEntityChest tileentitychest = (TileEntityChest) tileentity;

                if (tileentitychest.getEntitiesWithContainerOpen().isEmpty()) {
                    this.changeOverTime(iblockdata, worldserver, blockposition, randomsource);
                }
            }
        }

    }

    @Override
    public WeatheringCopper.a getAge() {
        return this.getState();
    }

    @Override
    public boolean isWaxed() {
        return false;
    }
}
