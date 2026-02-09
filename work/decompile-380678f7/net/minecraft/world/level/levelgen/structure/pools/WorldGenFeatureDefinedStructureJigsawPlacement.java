package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.WorldGenFeaturePieces;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.BlockJigsaw;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SeededRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.WorldGenFeaturePillagerOutpostPoolPiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.OperatorBoolean;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapes;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class WorldGenFeatureDefinedStructureJigsawPlacement {

    static final Logger LOGGER = LogUtils.getLogger();
    private static final int UNSET_HEIGHT = Integer.MIN_VALUE;

    public WorldGenFeatureDefinedStructureJigsawPlacement() {}

    public static Optional<Structure.b> addPieces(Structure.a structure_a, Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder, Optional<MinecraftKey> optional, int i, BlockPosition blockposition, boolean flag, Optional<HeightMap.Type> optional1, int j, PoolAliasLookup poolaliaslookup, DimensionPadding dimensionpadding, LiquidSettings liquidsettings) {
        IRegistryCustom iregistrycustom = structure_a.registryAccess();
        ChunkGenerator chunkgenerator = structure_a.chunkGenerator();
        StructureTemplateManager structuretemplatemanager = structure_a.structureTemplateManager();
        LevelHeightAccessor levelheightaccessor = structure_a.heightAccessor();
        SeededRandom seededrandom = structure_a.random();
        IRegistry<WorldGenFeatureDefinedStructurePoolTemplate> iregistry = iregistrycustom.lookupOrThrow(Registries.TEMPLATE_POOL);
        EnumBlockRotation enumblockrotation = EnumBlockRotation.getRandom(seededrandom);
        WorldGenFeatureDefinedStructurePoolTemplate worldgenfeaturedefinedstructurepooltemplate = (WorldGenFeatureDefinedStructurePoolTemplate) holder.unwrapKey().flatMap((resourcekey) -> {
            return iregistry.getOptional(poolaliaslookup.lookup(resourcekey));
        }).orElse(holder.value());
        WorldGenFeatureDefinedStructurePoolStructure worldgenfeaturedefinedstructurepoolstructure = worldgenfeaturedefinedstructurepooltemplate.getRandomTemplate(seededrandom);

        if (worldgenfeaturedefinedstructurepoolstructure == WorldGenFeatureDefinedStructurePoolEmpty.INSTANCE) {
            return Optional.empty();
        } else {
            BlockPosition blockposition1;

            if (optional.isPresent()) {
                MinecraftKey minecraftkey = (MinecraftKey) optional.get();
                Optional<BlockPosition> optional2 = getRandomNamedJigsaw(worldgenfeaturedefinedstructurepoolstructure, minecraftkey, blockposition, enumblockrotation, structuretemplatemanager, seededrandom);

                if (optional2.isEmpty()) {
                    WorldGenFeatureDefinedStructureJigsawPlacement.LOGGER.error("No starting jigsaw {} found in start pool {}", minecraftkey, holder.unwrapKey().map((resourcekey) -> {
                        return resourcekey.location().toString();
                    }).orElse("<unregistered>"));
                    return Optional.empty();
                }

                blockposition1 = (BlockPosition) optional2.get();
            } else {
                blockposition1 = blockposition;
            }

            BaseBlockPosition baseblockposition = blockposition1.subtract(blockposition);
            BlockPosition blockposition2 = blockposition.subtract(baseblockposition);
            WorldGenFeaturePillagerOutpostPoolPiece worldgenfeaturepillageroutpostpoolpiece = new WorldGenFeaturePillagerOutpostPoolPiece(structuretemplatemanager, worldgenfeaturedefinedstructurepoolstructure, blockposition2, worldgenfeaturedefinedstructurepoolstructure.getGroundLevelDelta(), enumblockrotation, worldgenfeaturedefinedstructurepoolstructure.getBoundingBox(structuretemplatemanager, blockposition2, enumblockrotation), liquidsettings);
            StructureBoundingBox structureboundingbox = worldgenfeaturepillageroutpostpoolpiece.getBoundingBox();
            int k = (structureboundingbox.maxX() + structureboundingbox.minX()) / 2;
            int l = (structureboundingbox.maxZ() + structureboundingbox.minZ()) / 2;
            int i1 = optional1.isEmpty() ? blockposition2.getY() : blockposition.getY() + chunkgenerator.getFirstFreeHeight(k, l, (HeightMap.Type) optional1.get(), levelheightaccessor, structure_a.randomState());
            int j1 = structureboundingbox.minY() + worldgenfeaturepillageroutpostpoolpiece.getGroundLevelDelta();

            worldgenfeaturepillageroutpostpoolpiece.move(0, i1 - j1, 0);
            if (isStartTooCloseToWorldHeightLimits(levelheightaccessor, dimensionpadding, worldgenfeaturepillageroutpostpoolpiece.getBoundingBox())) {
                WorldGenFeatureDefinedStructureJigsawPlacement.LOGGER.debug("Center piece {} with bounding box {} does not fit dimension padding {}", new Object[]{worldgenfeaturedefinedstructurepoolstructure, worldgenfeaturepillageroutpostpoolpiece.getBoundingBox(), dimensionpadding});
                return Optional.empty();
            } else {
                int k1 = i1 + baseblockposition.getY();

                return Optional.of(new Structure.b(new BlockPosition(k, k1, l), (structurepiecesbuilder) -> {
                    List<WorldGenFeaturePillagerOutpostPoolPiece> list = Lists.newArrayList();

                    list.add(worldgenfeaturepillageroutpostpoolpiece);
                    if (i > 0) {
                        AxisAlignedBB axisalignedbb = new AxisAlignedBB((double) (k - j), (double) Math.max(k1 - j, levelheightaccessor.getMinY() + dimensionpadding.bottom()), (double) (l - j), (double) (k + j + 1), (double) Math.min(k1 + j + 1, levelheightaccessor.getMaxY() + 1 - dimensionpadding.top()), (double) (l + j + 1));
                        VoxelShape voxelshape = VoxelShapes.join(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB.of(structureboundingbox)), OperatorBoolean.ONLY_FIRST);

                        addPieces(structure_a.randomState(), i, flag, chunkgenerator, structuretemplatemanager, levelheightaccessor, seededrandom, iregistry, worldgenfeaturepillageroutpostpoolpiece, list, voxelshape, poolaliaslookup, liquidsettings);
                        Objects.requireNonNull(structurepiecesbuilder);
                        list.forEach(structurepiecesbuilder::addPiece);
                    }
                }));
            }
        }
    }

    private static boolean isStartTooCloseToWorldHeightLimits(LevelHeightAccessor levelheightaccessor, DimensionPadding dimensionpadding, StructureBoundingBox structureboundingbox) {
        if (dimensionpadding == DimensionPadding.ZERO) {
            return false;
        } else {
            int i = levelheightaccessor.getMinY() + dimensionpadding.bottom();
            int j = levelheightaccessor.getMaxY() - dimensionpadding.top();

            return structureboundingbox.minY() < i || structureboundingbox.maxY() > j;
        }
    }

    private static Optional<BlockPosition> getRandomNamedJigsaw(WorldGenFeatureDefinedStructurePoolStructure worldgenfeaturedefinedstructurepoolstructure, MinecraftKey minecraftkey, BlockPosition blockposition, EnumBlockRotation enumblockrotation, StructureTemplateManager structuretemplatemanager, SeededRandom seededrandom) {
        for (DefinedStructure.a definedstructure_a : worldgenfeaturedefinedstructurepoolstructure.getShuffledJigsawBlocks(structuretemplatemanager, blockposition, enumblockrotation, seededrandom)) {
            if (minecraftkey.equals(definedstructure_a.name())) {
                return Optional.of(definedstructure_a.info().pos());
            }
        }

        return Optional.empty();
    }

    private static void addPieces(RandomState randomstate, int i, boolean flag, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, LevelHeightAccessor levelheightaccessor, RandomSource randomsource, IRegistry<WorldGenFeatureDefinedStructurePoolTemplate> iregistry, WorldGenFeaturePillagerOutpostPoolPiece worldgenfeaturepillageroutpostpoolpiece, List<WorldGenFeaturePillagerOutpostPoolPiece> list, VoxelShape voxelshape, PoolAliasLookup poolaliaslookup, LiquidSettings liquidsettings) {
        WorldGenFeatureDefinedStructureJigsawPlacement.b worldgenfeaturedefinedstructurejigsawplacement_b = new WorldGenFeatureDefinedStructureJigsawPlacement.b(iregistry, i, chunkgenerator, structuretemplatemanager, list, randomsource);

        worldgenfeaturedefinedstructurejigsawplacement_b.tryPlacingChildren(worldgenfeaturepillageroutpostpoolpiece, new MutableObject(voxelshape), 0, flag, levelheightaccessor, randomstate, poolaliaslookup, liquidsettings);

        while (worldgenfeaturedefinedstructurejigsawplacement_b.placing.hasNext()) {
            WorldGenFeatureDefinedStructureJigsawPlacement.a worldgenfeaturedefinedstructurejigsawplacement_a = (WorldGenFeatureDefinedStructureJigsawPlacement.a) worldgenfeaturedefinedstructurejigsawplacement_b.placing.next();

            worldgenfeaturedefinedstructurejigsawplacement_b.tryPlacingChildren(worldgenfeaturedefinedstructurejigsawplacement_a.piece, worldgenfeaturedefinedstructurejigsawplacement_a.free, worldgenfeaturedefinedstructurejigsawplacement_a.depth, flag, levelheightaccessor, randomstate, poolaliaslookup, liquidsettings);
        }

    }

    public static boolean generateJigsaw(WorldServer worldserver, Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder, MinecraftKey minecraftkey, int i, BlockPosition blockposition, boolean flag) {
        ChunkGenerator chunkgenerator = worldserver.getChunkSource().getGenerator();
        StructureTemplateManager structuretemplatemanager = worldserver.getStructureManager();
        StructureManager structuremanager = worldserver.structureManager();
        RandomSource randomsource = worldserver.getRandom();
        Structure.a structure_a = new Structure.a(worldserver.registryAccess(), chunkgenerator, chunkgenerator.getBiomeSource(), worldserver.getChunkSource().randomState(), structuretemplatemanager, worldserver.getSeed(), new ChunkCoordIntPair(blockposition), worldserver, (holder1) -> {
            return true;
        });
        Optional<Structure.b> optional = addPieces(structure_a, holder, Optional.of(minecraftkey), i, blockposition, false, Optional.empty(), 128, PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);

        if (optional.isPresent()) {
            StructurePiecesBuilder structurepiecesbuilder = ((Structure.b) optional.get()).getPiecesBuilder();

            for (StructurePiece structurepiece : structurepiecesbuilder.build().pieces()) {
                if (structurepiece instanceof WorldGenFeaturePillagerOutpostPoolPiece) {
                    WorldGenFeaturePillagerOutpostPoolPiece worldgenfeaturepillageroutpostpoolpiece = (WorldGenFeaturePillagerOutpostPoolPiece) structurepiece;

                    worldgenfeaturepillageroutpostpoolpiece.place(worldserver, structuremanager, chunkgenerator, randomsource, StructureBoundingBox.infinite(), blockposition, flag);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private static record a(WorldGenFeaturePillagerOutpostPoolPiece piece, MutableObject<VoxelShape> free, int depth) {

    }

    private static final class b {

        private final IRegistry<WorldGenFeatureDefinedStructurePoolTemplate> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super WorldGenFeaturePillagerOutpostPoolPiece> pieces;
        private final RandomSource random;
        final SequencedPriorityIterator<WorldGenFeatureDefinedStructureJigsawPlacement.a> placing = new SequencedPriorityIterator<WorldGenFeatureDefinedStructureJigsawPlacement.a>();

        b(IRegistry<WorldGenFeatureDefinedStructurePoolTemplate> iregistry, int i, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, List<? super WorldGenFeaturePillagerOutpostPoolPiece> list, RandomSource randomsource) {
            this.pools = iregistry;
            this.maxDepth = i;
            this.chunkGenerator = chunkgenerator;
            this.structureTemplateManager = structuretemplatemanager;
            this.pieces = list;
            this.random = randomsource;
        }

        void tryPlacingChildren(WorldGenFeaturePillagerOutpostPoolPiece worldgenfeaturepillageroutpostpoolpiece, MutableObject<VoxelShape> mutableobject, int i, boolean flag, LevelHeightAccessor levelheightaccessor, RandomState randomstate, PoolAliasLookup poolaliaslookup, LiquidSettings liquidsettings) {
            WorldGenFeatureDefinedStructurePoolStructure worldgenfeaturedefinedstructurepoolstructure = worldgenfeaturepillageroutpostpoolpiece.getElement();
            BlockPosition blockposition = worldgenfeaturepillageroutpostpoolpiece.getPosition();
            EnumBlockRotation enumblockrotation = worldgenfeaturepillageroutpostpoolpiece.getRotation();
            WorldGenFeatureDefinedStructurePoolTemplate.Matching worldgenfeaturedefinedstructurepooltemplate_matching = worldgenfeaturedefinedstructurepoolstructure.getProjection();
            boolean flag1 = worldgenfeaturedefinedstructurepooltemplate_matching == WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID;
            MutableObject<VoxelShape> mutableobject1 = new MutableObject();
            StructureBoundingBox structureboundingbox = worldgenfeaturepillageroutpostpoolpiece.getBoundingBox();
            int j = structureboundingbox.minY();

            label129:
            for (DefinedStructure.a definedstructure_a : worldgenfeaturedefinedstructurepoolstructure.getShuffledJigsawBlocks(this.structureTemplateManager, blockposition, enumblockrotation, this.random)) {
                DefinedStructure.BlockInfo definedstructure_blockinfo = definedstructure_a.info();
                EnumDirection enumdirection = BlockJigsaw.getFrontFacing(definedstructure_blockinfo.state());
                BlockPosition blockposition1 = definedstructure_blockinfo.pos();
                BlockPosition blockposition2 = blockposition1.relative(enumdirection);
                int k = blockposition1.getY() - j;
                int l = Integer.MIN_VALUE;
                ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey = poolaliaslookup.lookup(definedstructure_a.pool());
                Optional<? extends Holder<WorldGenFeatureDefinedStructurePoolTemplate>> optional = this.pools.get(resourcekey);

                if (optional.isEmpty()) {
                    WorldGenFeatureDefinedStructureJigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", resourcekey.location());
                } else {
                    Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder = (Holder) optional.get();

                    if (((WorldGenFeatureDefinedStructurePoolTemplate) holder.value()).size() == 0 && !holder.is(WorldGenFeaturePieces.EMPTY)) {
                        WorldGenFeatureDefinedStructureJigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", resourcekey.location());
                    } else {
                        Holder<WorldGenFeatureDefinedStructurePoolTemplate> holder1 = ((WorldGenFeatureDefinedStructurePoolTemplate) holder.value()).getFallback();

                        if (((WorldGenFeatureDefinedStructurePoolTemplate) holder1.value()).size() == 0 && !holder1.is(WorldGenFeaturePieces.EMPTY)) {
                            WorldGenFeatureDefinedStructureJigsawPlacement.LOGGER.warn("Empty or non-existent fallback pool: {}", holder1.unwrapKey().map((resourcekey1) -> {
                                return resourcekey1.location().toString();
                            }).orElse("<unregistered>"));
                        } else {
                            boolean flag2 = structureboundingbox.isInside(blockposition2);
                            MutableObject<VoxelShape> mutableobject2;

                            if (flag2) {
                                mutableobject2 = mutableobject1;
                                if (mutableobject1.getValue() == null) {
                                    mutableobject1.setValue(VoxelShapes.create(AxisAlignedBB.of(structureboundingbox)));
                                }
                            } else {
                                mutableobject2 = mutableobject;
                            }

                            List<WorldGenFeatureDefinedStructurePoolStructure> list = Lists.newArrayList();

                            if (i != this.maxDepth) {
                                list.addAll((holder.value()).getShuffledTemplates(this.random));
                            }

                            list.addAll((holder1.value()).getShuffledTemplates(this.random));
                            int i1 = definedstructure_a.placementPriority();

                            for (WorldGenFeatureDefinedStructurePoolStructure worldgenfeaturedefinedstructurepoolstructure1 : list) {
                                if (worldgenfeaturedefinedstructurepoolstructure1 == WorldGenFeatureDefinedStructurePoolEmpty.INSTANCE) {
                                    break;
                                }

                                for (EnumBlockRotation enumblockrotation1 : EnumBlockRotation.getShuffled(this.random)) {
                                    List<DefinedStructure.a> list1 = worldgenfeaturedefinedstructurepoolstructure1.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPosition.ZERO, enumblockrotation1, this.random);
                                    StructureBoundingBox structureboundingbox1 = worldgenfeaturedefinedstructurepoolstructure1.getBoundingBox(this.structureTemplateManager, BlockPosition.ZERO, enumblockrotation1);
                                    int j1;

                                    if (flag && structureboundingbox1.getYSpan() <= 16) {
                                        j1 = list1.stream().mapToInt((definedstructure_a1) -> {
                                            DefinedStructure.BlockInfo definedstructure_blockinfo1 = definedstructure_a1.info();

                                            if (!structureboundingbox1.isInside(definedstructure_blockinfo1.pos().relative(BlockJigsaw.getFrontFacing(definedstructure_blockinfo1.state())))) {
                                                return 0;
                                            } else {
                                                ResourceKey<WorldGenFeatureDefinedStructurePoolTemplate> resourcekey1 = poolaliaslookup.lookup(definedstructure_a1.pool());
                                                Optional<? extends Holder<WorldGenFeatureDefinedStructurePoolTemplate>> optional1 = this.pools.get(resourcekey1);
                                                Optional<Holder<WorldGenFeatureDefinedStructurePoolTemplate>> optional2 = optional1.map((holder2) -> {
                                                    return ((WorldGenFeatureDefinedStructurePoolTemplate) holder2.value()).getFallback();
                                                });
                                                int k1 = (Integer) optional1.map((holder2) -> {
                                                    return ((WorldGenFeatureDefinedStructurePoolTemplate) holder2.value()).getMaxSize(this.structureTemplateManager);
                                                }).orElse(0);
                                                int l1 = (Integer) optional2.map((holder2) -> {
                                                    return ((WorldGenFeatureDefinedStructurePoolTemplate) holder2.value()).getMaxSize(this.structureTemplateManager);
                                                }).orElse(0);

                                                return Math.max(k1, l1);
                                            }
                                        }).max().orElse(0);
                                    } else {
                                        j1 = 0;
                                    }

                                    for (DefinedStructure.a definedstructure_a1 : list1) {
                                        if (BlockJigsaw.canAttach(definedstructure_a, definedstructure_a1)) {
                                            BlockPosition blockposition3 = definedstructure_a1.info().pos();
                                            BlockPosition blockposition4 = blockposition2.subtract(blockposition3);
                                            StructureBoundingBox structureboundingbox2 = worldgenfeaturedefinedstructurepoolstructure1.getBoundingBox(this.structureTemplateManager, blockposition4, enumblockrotation1);
                                            int k1 = structureboundingbox2.minY();
                                            WorldGenFeatureDefinedStructurePoolTemplate.Matching worldgenfeaturedefinedstructurepooltemplate_matching1 = worldgenfeaturedefinedstructurepoolstructure1.getProjection();
                                            boolean flag3 = worldgenfeaturedefinedstructurepooltemplate_matching1 == WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID;
                                            int l1 = blockposition3.getY();
                                            int i2 = k - l1 + BlockJigsaw.getFrontFacing(definedstructure_blockinfo.state()).getStepY();
                                            int j2;

                                            if (flag1 && flag3) {
                                                j2 = j + i2;
                                            } else {
                                                if (l == Integer.MIN_VALUE) {
                                                    l = this.chunkGenerator.getFirstFreeHeight(blockposition1.getX(), blockposition1.getZ(), HeightMap.Type.WORLD_SURFACE_WG, levelheightaccessor, randomstate);
                                                }

                                                j2 = l - l1;
                                            }

                                            int k2 = j2 - k1;
                                            StructureBoundingBox structureboundingbox3 = structureboundingbox2.moved(0, k2, 0);
                                            BlockPosition blockposition5 = blockposition4.offset(0, k2, 0);

                                            if (j1 > 0) {
                                                int l2 = Math.max(j1 + 1, structureboundingbox3.maxY() - structureboundingbox3.minY());

                                                structureboundingbox3.encapsulate(new BlockPosition(structureboundingbox3.minX(), structureboundingbox3.minY() + l2, structureboundingbox3.minZ()));
                                            }

                                            if (!VoxelShapes.joinIsNotEmpty((VoxelShape) mutableobject2.getValue(), VoxelShapes.create(AxisAlignedBB.of(structureboundingbox3).deflate(0.25D)), OperatorBoolean.ONLY_SECOND)) {
                                                mutableobject2.setValue(VoxelShapes.joinUnoptimized((VoxelShape) mutableobject2.getValue(), VoxelShapes.create(AxisAlignedBB.of(structureboundingbox3)), OperatorBoolean.ONLY_FIRST));
                                                int i3 = worldgenfeaturepillageroutpostpoolpiece.getGroundLevelDelta();
                                                int j3;

                                                if (flag3) {
                                                    j3 = i3 - i2;
                                                } else {
                                                    j3 = worldgenfeaturedefinedstructurepoolstructure1.getGroundLevelDelta();
                                                }

                                                WorldGenFeaturePillagerOutpostPoolPiece worldgenfeaturepillageroutpostpoolpiece1 = new WorldGenFeaturePillagerOutpostPoolPiece(this.structureTemplateManager, worldgenfeaturedefinedstructurepoolstructure1, blockposition5, j3, enumblockrotation1, structureboundingbox3, liquidsettings);
                                                int k3;

                                                if (flag1) {
                                                    k3 = j + k;
                                                } else if (flag3) {
                                                    k3 = j2 + l1;
                                                } else {
                                                    if (l == Integer.MIN_VALUE) {
                                                        l = this.chunkGenerator.getFirstFreeHeight(blockposition1.getX(), blockposition1.getZ(), HeightMap.Type.WORLD_SURFACE_WG, levelheightaccessor, randomstate);
                                                    }

                                                    k3 = l + i2 / 2;
                                                }

                                                worldgenfeaturepillageroutpostpoolpiece.addJunction(new WorldGenFeatureDefinedStructureJigsawJunction(blockposition2.getX(), k3 - k + i3, blockposition2.getZ(), i2, worldgenfeaturedefinedstructurepooltemplate_matching1));
                                                worldgenfeaturepillageroutpostpoolpiece1.addJunction(new WorldGenFeatureDefinedStructureJigsawJunction(blockposition1.getX(), k3 - l1 + j3, blockposition1.getZ(), -i2, worldgenfeaturedefinedstructurepooltemplate_matching));
                                                this.pieces.add(worldgenfeaturepillageroutpostpoolpiece1);
                                                if (i + 1 <= this.maxDepth) {
                                                    WorldGenFeatureDefinedStructureJigsawPlacement.a worldgenfeaturedefinedstructurejigsawplacement_a = new WorldGenFeatureDefinedStructureJigsawPlacement.a(worldgenfeaturepillageroutpostpoolpiece1, mutableobject2, i + 1);

                                                    this.placing.add(worldgenfeaturedefinedstructurejigsawplacement_a, i1);
                                                }
                                                continue label129;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
