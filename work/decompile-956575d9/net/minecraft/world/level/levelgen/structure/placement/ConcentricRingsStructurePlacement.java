package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.Products.P9;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

public class ConcentricRingsStructurePlacement extends StructurePlacement {

    public static final MapCodec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return codec(instance).apply(instance, ConcentricRingsStructurePlacement::new);
    });
    private final int distance;
    private final int spread;
    private final int count;
    private final HolderSet<BiomeBase> preferredBiomes;

    private static Products.P9<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, BaseBlockPosition, StructurePlacement.c, Float, Integer, Optional<StructurePlacement.a>, Integer, Integer, Integer, HolderSet<BiomeBase>> codec(RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement> recordcodecbuilder_instance) {
        Products.P5<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, BaseBlockPosition, StructurePlacement.c, Float, Integer, Optional<StructurePlacement.a>> products_p5 = placementCodec(recordcodecbuilder_instance);
        Products.P4<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Integer, Integer, Integer, HolderSet<BiomeBase>> products_p4 = recordcodecbuilder_instance.group(Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance), Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread), Codec.intRange(1, 4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count), RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(ConcentricRingsStructurePlacement::preferredBiomes));

        return new P9(products_p5.t1(), products_p5.t2(), products_p5.t3(), products_p5.t4(), products_p5.t5(), products_p4.t1(), products_p4.t2(), products_p4.t3(), products_p4.t4());
    }

    public ConcentricRingsStructurePlacement(BaseBlockPosition baseblockposition, StructurePlacement.c structureplacement_c, float f, int i, Optional<StructurePlacement.a> optional, int j, int k, int l, HolderSet<BiomeBase> holderset) {
        super(baseblockposition, structureplacement_c, f, i, optional);
        this.distance = j;
        this.spread = k;
        this.count = l;
        this.preferredBiomes = holderset;
    }

    public ConcentricRingsStructurePlacement(int i, int j, int k, HolderSet<BiomeBase> holderset) {
        this(BaseBlockPosition.ZERO, StructurePlacement.c.DEFAULT, 1.0F, 0, Optional.empty(), i, j, k, holderset);
    }

    public int distance() {
        return this.distance;
    }

    public int spread() {
        return this.spread;
    }

    public int count() {
        return this.count;
    }

    public HolderSet<BiomeBase> preferredBiomes() {
        return this.preferredBiomes;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j) {
        List<ChunkCoordIntPair> list = chunkgeneratorstructurestate.getRingPositionsFor(this);

        return list == null ? false : list.contains(new ChunkCoordIntPair(i, j));
    }

    @Override
    public StructurePlacementType<?> type() {
        return StructurePlacementType.CONCENTRIC_RINGS;
    }
}
