package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.DefinedStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.WorldGenFeatureStructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockIgnore;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherFossilPieces {

    private static final MinecraftKey[] FOSSILS = new MinecraftKey[]{MinecraftKey.withDefaultNamespace("nether_fossils/fossil_1"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_2"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_3"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_4"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_5"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_6"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_7"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_8"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_9"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_10"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_11"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_12"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_13"), MinecraftKey.withDefaultNamespace("nether_fossils/fossil_14")};

    public NetherFossilPieces() {}

    public static void addPieces(StructureTemplateManager structuretemplatemanager, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, BlockPosition blockposition) {
        EnumBlockRotation enumblockrotation = EnumBlockRotation.getRandom(randomsource);

        structurepieceaccessor.addPiece(new NetherFossilPieces.a(structuretemplatemanager, (MinecraftKey) SystemUtils.getRandom(NetherFossilPieces.FOSSILS, randomsource), blockposition, enumblockrotation));
    }

    public static class a extends DefinedStructurePiece {

        public a(StructureTemplateManager structuretemplatemanager, MinecraftKey minecraftkey, BlockPosition blockposition, EnumBlockRotation enumblockrotation) {
            super(WorldGenFeatureStructurePieceType.NETHER_FOSSIL, 0, structuretemplatemanager, minecraftkey, minecraftkey.toString(), makeSettings(enumblockrotation), blockposition);
        }

        public a(StructureTemplateManager structuretemplatemanager, NBTTagCompound nbttagcompound) {
            super(WorldGenFeatureStructurePieceType.NETHER_FOSSIL, nbttagcompound, structuretemplatemanager, (minecraftkey) -> {
                return makeSettings((EnumBlockRotation) nbttagcompound.read("Rot", EnumBlockRotation.LEGACY_CODEC).orElseThrow());
            });
        }

        private static DefinedStructureInfo makeSettings(EnumBlockRotation enumblockrotation) {
            return (new DefinedStructureInfo()).setRotation(enumblockrotation).setMirror(EnumBlockMirror.NONE).addProcessor(DefinedStructureProcessorBlockIgnore.STRUCTURE_AND_AIR);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
            super.addAdditionalSaveData(structurepieceserializationcontext, nbttagcompound);
            nbttagcompound.store("Rot", EnumBlockRotation.LEGACY_CODEC, this.placeSettings.getRotation());
        }

        @Override
        protected void handleDataMarker(String s, BlockPosition blockposition, WorldAccess worldaccess, RandomSource randomsource, StructureBoundingBox structureboundingbox) {}

        @Override
        public void postProcess(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, ChunkCoordIntPair chunkcoordintpair, BlockPosition blockposition) {
            StructureBoundingBox structureboundingbox1 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);

            structureboundingbox.encapsulate(structureboundingbox1);
            super.postProcess(generatoraccessseed, structuremanager, chunkgenerator, randomsource, structureboundingbox, chunkcoordintpair, blockposition);
            this.placeDriedGhast(generatoraccessseed, randomsource, structureboundingbox1, structureboundingbox);
        }

        private void placeDriedGhast(GeneratorAccessSeed generatoraccessseed, RandomSource randomsource, StructureBoundingBox structureboundingbox, StructureBoundingBox structureboundingbox1) {
            RandomSource randomsource1 = RandomSource.create(generatoraccessseed.getSeed()).forkPositional().at(structureboundingbox.getCenter());

            if (randomsource1.nextFloat() < 0.5F) {
                int i = structureboundingbox.minX() + randomsource1.nextInt(structureboundingbox.getXSpan());
                int j = structureboundingbox.minY();
                int k = structureboundingbox.minZ() + randomsource1.nextInt(structureboundingbox.getZSpan());
                BlockPosition blockposition = new BlockPosition(i, j, k);

                if (generatoraccessseed.getBlockState(blockposition).isAir() && structureboundingbox1.isInside(blockposition)) {
                    generatoraccessseed.setBlock(blockposition, Blocks.DRIED_GHAST.defaultBlockState().rotate(EnumBlockRotation.getRandom(randomsource1)), 2);
                }
            }

        }
    }
}
