package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public record PalettedContainerFactory(Strategy<IBlockData> blockStatesStrategy, IBlockData defaultBlockState, Codec<DataPaletteBlock<IBlockData>> blockStatesContainerCodec, Strategy<Holder<BiomeBase>> biomeStrategy, Holder<BiomeBase> defaultBiome, Codec<PalettedContainerRO<Holder<BiomeBase>>> biomeContainerCodec) {

    public static PalettedContainerFactory create(IRegistryCustom iregistrycustom) {
        Strategy<IBlockData> strategy = Strategy.<IBlockData>createForBlockStates(Block.BLOCK_STATE_REGISTRY);
        IBlockData iblockdata = Blocks.AIR.defaultBlockState();
        IRegistry<BiomeBase> iregistry = iregistrycustom.lookupOrThrow(Registries.BIOME);
        Strategy<Holder<BiomeBase>> strategy1 = Strategy.<Holder<BiomeBase>>createForBiomes(iregistry.asHolderIdMap());
        Holder.c<BiomeBase> holder_c = iregistry.getOrThrow(Biomes.PLAINS);

        return new PalettedContainerFactory(strategy, iblockdata, DataPaletteBlock.codecRW(IBlockData.CODEC, strategy, iblockdata), strategy1, holder_c, DataPaletteBlock.codecRO(iregistry.holderByNameCodec(), strategy1, holder_c));
    }

    public DataPaletteBlock<IBlockData> createForBlockStates() {
        return new DataPaletteBlock<IBlockData>(this.defaultBlockState, this.blockStatesStrategy);
    }

    public DataPaletteBlock<Holder<BiomeBase>> createForBiomes() {
        return new DataPaletteBlock<Holder<BiomeBase>>(this.defaultBiome, this.biomeStrategy);
    }
}
