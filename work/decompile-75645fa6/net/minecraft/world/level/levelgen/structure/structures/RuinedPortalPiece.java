package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.INamable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.BlockVine;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.DefinedStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.WorldGenFeatureStructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlackstoneReplace;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockAge;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorBlockIgnore;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorLavaSubmergedBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorPredicates;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureTestBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureTestRandomBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureTestTrue;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class RuinedPortalPiece extends DefinedStructurePiece {

    private static final float PROBABILITY_OF_GOLD_GONE = 0.3F;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_NETHERRACK = 0.07F;
    private static final float PROBABILITY_OF_MAGMA_INSTEAD_OF_LAVA = 0.2F;
    private final RuinedPortalPiece.b verticalPlacement;
    private final RuinedPortalPiece.a properties;

    public RuinedPortalPiece(StructureTemplateManager structuretemplatemanager, BlockPosition blockposition, RuinedPortalPiece.b ruinedportalpiece_b, RuinedPortalPiece.a ruinedportalpiece_a, MinecraftKey minecraftkey, DefinedStructure definedstructure, EnumBlockRotation enumblockrotation, EnumBlockMirror enumblockmirror, BlockPosition blockposition1) {
        super(WorldGenFeatureStructurePieceType.RUINED_PORTAL, 0, structuretemplatemanager, minecraftkey, minecraftkey.toString(), makeSettings(enumblockmirror, enumblockrotation, ruinedportalpiece_b, blockposition1, ruinedportalpiece_a), blockposition);
        this.verticalPlacement = ruinedportalpiece_b;
        this.properties = ruinedportalpiece_a;
    }

    public RuinedPortalPiece(StructureTemplateManager structuretemplatemanager, NBTTagCompound nbttagcompound) {
        super(WorldGenFeatureStructurePieceType.RUINED_PORTAL, nbttagcompound, structuretemplatemanager, (minecraftkey) -> {
            return makeSettings(structuretemplatemanager, nbttagcompound, minecraftkey);
        });
        this.verticalPlacement = (RuinedPortalPiece.b) nbttagcompound.read("VerticalPlacement", RuinedPortalPiece.b.CODEC).orElseThrow();
        this.properties = (RuinedPortalPiece.a) nbttagcompound.read("Properties", RuinedPortalPiece.a.CODEC).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(structurepieceserializationcontext, nbttagcompound);
        nbttagcompound.store("Rotation", EnumBlockRotation.LEGACY_CODEC, this.placeSettings.getRotation());
        nbttagcompound.store("Mirror", EnumBlockMirror.LEGACY_CODEC, this.placeSettings.getMirror());
        nbttagcompound.store("VerticalPlacement", RuinedPortalPiece.b.CODEC, this.verticalPlacement);
        nbttagcompound.store("Properties", RuinedPortalPiece.a.CODEC, this.properties);
    }

    private static DefinedStructureInfo makeSettings(StructureTemplateManager structuretemplatemanager, NBTTagCompound nbttagcompound, MinecraftKey minecraftkey) {
        DefinedStructure definedstructure = structuretemplatemanager.getOrCreate(minecraftkey);
        BlockPosition blockposition = new BlockPosition(definedstructure.getSize().getX() / 2, 0, definedstructure.getSize().getZ() / 2);

        return makeSettings((EnumBlockMirror) nbttagcompound.read("Mirror", EnumBlockMirror.LEGACY_CODEC).orElseThrow(), (EnumBlockRotation) nbttagcompound.read("Rotation", EnumBlockRotation.LEGACY_CODEC).orElseThrow(), (RuinedPortalPiece.b) nbttagcompound.read("VerticalPlacement", RuinedPortalPiece.b.CODEC).orElseThrow(), blockposition, (RuinedPortalPiece.a) RuinedPortalPiece.a.CODEC.parse(new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound.get("Properties"))).getPartialOrThrow());
    }

    private static DefinedStructureInfo makeSettings(EnumBlockMirror enumblockmirror, EnumBlockRotation enumblockrotation, RuinedPortalPiece.b ruinedportalpiece_b, BlockPosition blockposition, RuinedPortalPiece.a ruinedportalpiece_a) {
        DefinedStructureProcessorBlockIgnore definedstructureprocessorblockignore = ruinedportalpiece_a.airPocket ? DefinedStructureProcessorBlockIgnore.STRUCTURE_BLOCK : DefinedStructureProcessorBlockIgnore.STRUCTURE_AND_AIR;
        List<DefinedStructureProcessorPredicates> list = Lists.newArrayList();

        list.add(getBlockReplaceRule(Blocks.GOLD_BLOCK, 0.3F, Blocks.AIR));
        list.add(getLavaProcessorRule(ruinedportalpiece_b, ruinedportalpiece_a));
        if (!ruinedportalpiece_a.cold) {
            list.add(getBlockReplaceRule(Blocks.NETHERRACK, 0.07F, Blocks.MAGMA_BLOCK));
        }

        DefinedStructureInfo definedstructureinfo = (new DefinedStructureInfo()).setRotation(enumblockrotation).setMirror(enumblockmirror).setRotationPivot(blockposition).addProcessor(definedstructureprocessorblockignore).addProcessor(new DefinedStructureProcessorRule(list)).addProcessor(new DefinedStructureProcessorBlockAge(ruinedportalpiece_a.mossiness)).addProcessor(new ProtectedBlockProcessor(TagsBlock.FEATURES_CANNOT_REPLACE)).addProcessor(new DefinedStructureProcessorLavaSubmergedBlock());

        if (ruinedportalpiece_a.replaceWithBlackstone) {
            definedstructureinfo.addProcessor(DefinedStructureProcessorBlackstoneReplace.INSTANCE);
        }

        return definedstructureinfo;
    }

    private static DefinedStructureProcessorPredicates getLavaProcessorRule(RuinedPortalPiece.b ruinedportalpiece_b, RuinedPortalPiece.a ruinedportalpiece_a) {
        return ruinedportalpiece_b == RuinedPortalPiece.b.ON_OCEAN_FLOOR ? getBlockReplaceRule(Blocks.LAVA, Blocks.MAGMA_BLOCK) : (ruinedportalpiece_a.cold ? getBlockReplaceRule(Blocks.LAVA, Blocks.NETHERRACK) : getBlockReplaceRule(Blocks.LAVA, 0.2F, Blocks.MAGMA_BLOCK));
    }

    @Override
    public void postProcess(GeneratorAccessSeed generatoraccessseed, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, StructureBoundingBox structureboundingbox, ChunkCoordIntPair chunkcoordintpair, BlockPosition blockposition) {
        StructureBoundingBox structureboundingbox1 = this.template.getBoundingBox(this.placeSettings, this.templatePosition);

        if (structureboundingbox.isInside(structureboundingbox1.getCenter())) {
            structureboundingbox.encapsulate(structureboundingbox1);
            super.postProcess(generatoraccessseed, structuremanager, chunkgenerator, randomsource, structureboundingbox, chunkcoordintpair, blockposition);
            this.spreadNetherrack(randomsource, generatoraccessseed);
            this.addNetherrackDripColumnsBelowPortal(randomsource, generatoraccessseed);
            if (this.properties.vines || this.properties.overgrown) {
                BlockPosition.betweenClosedStream(this.getBoundingBox()).forEach((blockposition1) -> {
                    if (this.properties.vines) {
                        this.maybeAddVines(randomsource, generatoraccessseed, blockposition1);
                    }

                    if (this.properties.overgrown) {
                        this.maybeAddLeavesAbove(randomsource, generatoraccessseed, blockposition1);
                    }

                });
            }

        }
    }

    @Override
    protected void handleDataMarker(String s, BlockPosition blockposition, WorldAccess worldaccess, RandomSource randomsource, StructureBoundingBox structureboundingbox) {}

    private void maybeAddVines(RandomSource randomsource, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);

        if (!iblockdata.isAir() && !iblockdata.is(Blocks.VINE)) {
            EnumDirection enumdirection = getRandomHorizontalDirection(randomsource);
            BlockPosition blockposition1 = blockposition.relative(enumdirection);
            IBlockData iblockdata1 = generatoraccess.getBlockState(blockposition1);

            if (iblockdata1.isAir()) {
                if (Block.isFaceFull(iblockdata.getCollisionShape(generatoraccess, blockposition), enumdirection)) {
                    BlockStateBoolean blockstateboolean = BlockVine.getPropertyForFace(enumdirection.getOpposite());

                    generatoraccess.setBlock(blockposition1, (IBlockData) Blocks.VINE.defaultBlockState().setValue(blockstateboolean, true), 3);
                }
            }
        }
    }

    private void maybeAddLeavesAbove(RandomSource randomsource, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        if (randomsource.nextFloat() < 0.5F && generatoraccess.getBlockState(blockposition).is(Blocks.NETHERRACK) && generatoraccess.getBlockState(blockposition.above()).isAir()) {
            generatoraccess.setBlock(blockposition.above(), (IBlockData) Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(BlockLeaves.PERSISTENT, true), 3);
        }

    }

    private void addNetherrackDripColumnsBelowPortal(RandomSource randomsource, GeneratorAccess generatoraccess) {
        for (int i = this.boundingBox.minX() + 1; i < this.boundingBox.maxX(); ++i) {
            for (int j = this.boundingBox.minZ() + 1; j < this.boundingBox.maxZ(); ++j) {
                BlockPosition blockposition = new BlockPosition(i, this.boundingBox.minY(), j);

                if (generatoraccess.getBlockState(blockposition).is(Blocks.NETHERRACK)) {
                    this.addNetherrackDripColumn(randomsource, generatoraccess, blockposition.below());
                }
            }
        }

    }

    private void addNetherrackDripColumn(RandomSource randomsource, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = blockposition.mutable();

        this.placeNetherrackOrMagma(randomsource, generatoraccess, blockposition_mutableblockposition);
        int i = 8;

        while (i > 0 && randomsource.nextFloat() < 0.5F) {
            blockposition_mutableblockposition.move(EnumDirection.DOWN);
            --i;
            this.placeNetherrackOrMagma(randomsource, generatoraccess, blockposition_mutableblockposition);
        }

    }

    private void spreadNetherrack(RandomSource randomsource, GeneratorAccess generatoraccess) {
        boolean flag = this.verticalPlacement == RuinedPortalPiece.b.ON_LAND_SURFACE || this.verticalPlacement == RuinedPortalPiece.b.ON_OCEAN_FLOOR;
        BlockPosition blockposition = this.boundingBox.getCenter();
        int i = blockposition.getX();
        int j = blockposition.getZ();
        float[] afloat = new float[]{1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F, 0.9F, 0.8F, 0.7F, 0.6F, 0.4F, 0.2F};
        int k = afloat.length;
        int l = (this.boundingBox.getXSpan() + this.boundingBox.getZSpan()) / 2;
        int i1 = randomsource.nextInt(Math.max(1, 8 - l / 2));
        int j1 = 3;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = BlockPosition.ZERO.mutable();

        for (int k1 = i - k; k1 <= i + k; ++k1) {
            for (int l1 = j - k; l1 <= j + k; ++l1) {
                int i2 = Math.abs(k1 - i) + Math.abs(l1 - j);
                int j2 = Math.max(0, i2 + i1);

                if (j2 < k) {
                    float f = afloat[j2];

                    if (randomsource.nextDouble() < (double) f) {
                        int k2 = getSurfaceY(generatoraccess, k1, l1, this.verticalPlacement);
                        int l2 = flag ? k2 : Math.min(this.boundingBox.minY(), k2);

                        blockposition_mutableblockposition.set(k1, l2, l1);
                        if (Math.abs(l2 - this.boundingBox.minY()) <= 3 && this.canBlockBeReplacedByNetherrackOrMagma(generatoraccess, blockposition_mutableblockposition)) {
                            this.placeNetherrackOrMagma(randomsource, generatoraccess, blockposition_mutableblockposition);
                            if (this.properties.overgrown) {
                                this.maybeAddLeavesAbove(randomsource, generatoraccess, blockposition_mutableblockposition);
                            }

                            this.addNetherrackDripColumn(randomsource, generatoraccess, blockposition_mutableblockposition.below());
                        }
                    }
                }
            }
        }

    }

    private boolean canBlockBeReplacedByNetherrackOrMagma(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);

        return !iblockdata.is(Blocks.AIR) && !iblockdata.is(Blocks.OBSIDIAN) && !iblockdata.is(TagsBlock.FEATURES_CANNOT_REPLACE) && (this.verticalPlacement == RuinedPortalPiece.b.IN_NETHER || !iblockdata.is(Blocks.LAVA));
    }

    private void placeNetherrackOrMagma(RandomSource randomsource, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        if (!this.properties.cold && randomsource.nextFloat() < 0.07F) {
            generatoraccess.setBlock(blockposition, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        } else {
            generatoraccess.setBlock(blockposition, Blocks.NETHERRACK.defaultBlockState(), 3);
        }

    }

    private static int getSurfaceY(GeneratorAccess generatoraccess, int i, int j, RuinedPortalPiece.b ruinedportalpiece_b) {
        return generatoraccess.getHeight(getHeightMapType(ruinedportalpiece_b), i, j) - 1;
    }

    public static HeightMap.Type getHeightMapType(RuinedPortalPiece.b ruinedportalpiece_b) {
        return ruinedportalpiece_b == RuinedPortalPiece.b.ON_OCEAN_FLOOR ? HeightMap.Type.OCEAN_FLOOR_WG : HeightMap.Type.WORLD_SURFACE_WG;
    }

    private static DefinedStructureProcessorPredicates getBlockReplaceRule(Block block, float f, Block block1) {
        return new DefinedStructureProcessorPredicates(new DefinedStructureTestRandomBlock(block, f), DefinedStructureTestTrue.INSTANCE, block1.defaultBlockState());
    }

    private static DefinedStructureProcessorPredicates getBlockReplaceRule(Block block, Block block1) {
        return new DefinedStructureProcessorPredicates(new DefinedStructureTestBlock(block), DefinedStructureTestTrue.INSTANCE, block1.defaultBlockState());
    }

    public static class a {

        public static final Codec<RuinedPortalPiece.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.BOOL.fieldOf("cold").forGetter((ruinedportalpiece_a) -> {
                return ruinedportalpiece_a.cold;
            }), Codec.FLOAT.fieldOf("mossiness").forGetter((ruinedportalpiece_a) -> {
                return ruinedportalpiece_a.mossiness;
            }), Codec.BOOL.fieldOf("air_pocket").forGetter((ruinedportalpiece_a) -> {
                return ruinedportalpiece_a.airPocket;
            }), Codec.BOOL.fieldOf("overgrown").forGetter((ruinedportalpiece_a) -> {
                return ruinedportalpiece_a.overgrown;
            }), Codec.BOOL.fieldOf("vines").forGetter((ruinedportalpiece_a) -> {
                return ruinedportalpiece_a.vines;
            }), Codec.BOOL.fieldOf("replace_with_blackstone").forGetter((ruinedportalpiece_a) -> {
                return ruinedportalpiece_a.replaceWithBlackstone;
            })).apply(instance, RuinedPortalPiece.a::new);
        });
        public boolean cold;
        public float mossiness;
        public boolean airPocket;
        public boolean overgrown;
        public boolean vines;
        public boolean replaceWithBlackstone;

        public a() {}

        public a(boolean flag, float f, boolean flag1, boolean flag2, boolean flag3, boolean flag4) {
            this.cold = flag;
            this.mossiness = f;
            this.airPocket = flag1;
            this.overgrown = flag2;
            this.vines = flag3;
            this.replaceWithBlackstone = flag4;
        }
    }

    public static enum b implements INamable {

        ON_LAND_SURFACE("on_land_surface"), PARTLY_BURIED("partly_buried"), ON_OCEAN_FLOOR("on_ocean_floor"), IN_MOUNTAIN("in_mountain"), UNDERGROUND("underground"), IN_NETHER("in_nether");

        public static final Codec<RuinedPortalPiece.b> CODEC = INamable.<RuinedPortalPiece.b>fromEnum(RuinedPortalPiece.b::values);
        private final String name;

        private b(final String s) {
            this.name = s;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
