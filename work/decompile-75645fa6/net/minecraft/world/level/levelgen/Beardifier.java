package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.WorldGenFeaturePillagerOutpostPoolPiece;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructureJigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.WorldGenFeatureDefinedStructurePoolTemplate;

public class Beardifier implements DensityFunctions.c {

    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = (float[]) SystemUtils.make(new float[13824], (afloat) -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    afloat[i * 24 * 24 + j * 24 + k] = (float) computeBeardContribution(j - 12, k - 12, i - 12);
                }
            }
        }

    });
    public static final Beardifier EMPTY = new Beardifier(List.of(), List.of(), (StructureBoundingBox) null);
    private final List<Beardifier.a> pieces;
    private final List<WorldGenFeatureDefinedStructureJigsawJunction> junctions;
    @Nullable
    private final StructureBoundingBox affectedBox;

    public static Beardifier forStructuresInChunk(StructureManager structuremanager, ChunkCoordIntPair chunkcoordintpair) {
        List<StructureStart> list = structuremanager.startsForStructure(chunkcoordintpair, (structure) -> {
            return structure.terrainAdaptation() != TerrainAdjustment.NONE;
        });

        if (list.isEmpty()) {
            return Beardifier.EMPTY;
        } else {
            int i = chunkcoordintpair.getMinBlockX();
            int j = chunkcoordintpair.getMinBlockZ();
            List<Beardifier.a> list1 = new ArrayList();
            List<WorldGenFeatureDefinedStructureJigsawJunction> list2 = new ArrayList();
            StructureBoundingBox structureboundingbox = null;

            for (StructureStart structurestart : list) {
                TerrainAdjustment terrainadjustment = structurestart.getStructure().terrainAdaptation();

                for (StructurePiece structurepiece : structurestart.getPieces()) {
                    if (structurepiece.isCloseToChunk(chunkcoordintpair, 12)) {
                        if (structurepiece instanceof WorldGenFeaturePillagerOutpostPoolPiece) {
                            WorldGenFeaturePillagerOutpostPoolPiece worldgenfeaturepillageroutpostpoolpiece = (WorldGenFeaturePillagerOutpostPoolPiece) structurepiece;
                            WorldGenFeatureDefinedStructurePoolTemplate.Matching worldgenfeaturedefinedstructurepooltemplate_matching = worldgenfeaturepillageroutpostpoolpiece.getElement().getProjection();

                            if (worldgenfeaturedefinedstructurepooltemplate_matching == WorldGenFeatureDefinedStructurePoolTemplate.Matching.RIGID) {
                                list1.add(new Beardifier.a(worldgenfeaturepillageroutpostpoolpiece.getBoundingBox(), terrainadjustment, worldgenfeaturepillageroutpostpoolpiece.getGroundLevelDelta()));
                                structureboundingbox = includeBoundingBox(structureboundingbox, structurepiece.getBoundingBox());
                            }

                            for (WorldGenFeatureDefinedStructureJigsawJunction worldgenfeaturedefinedstructurejigsawjunction : worldgenfeaturepillageroutpostpoolpiece.getJunctions()) {
                                int k = worldgenfeaturedefinedstructurejigsawjunction.getSourceX();
                                int l = worldgenfeaturedefinedstructurejigsawjunction.getSourceZ();

                                if (k > i - 12 && l > j - 12 && k < i + 15 + 12 && l < j + 15 + 12) {
                                    list2.add(worldgenfeaturedefinedstructurejigsawjunction);
                                    StructureBoundingBox structureboundingbox1 = new StructureBoundingBox(new BlockPosition(k, worldgenfeaturedefinedstructurejigsawjunction.getSourceGroundY(), l));

                                    structureboundingbox = includeBoundingBox(structureboundingbox, structureboundingbox1);
                                }
                            }
                        } else {
                            list1.add(new Beardifier.a(structurepiece.getBoundingBox(), terrainadjustment, 0));
                            structureboundingbox = includeBoundingBox(structureboundingbox, structurepiece.getBoundingBox());
                        }
                    }
                }
            }

            if (structureboundingbox == null) {
                return Beardifier.EMPTY;
            } else {
                StructureBoundingBox structureboundingbox2 = structureboundingbox.inflatedBy(24);

                return new Beardifier(List.copyOf(list1), List.copyOf(list2), structureboundingbox2);
            }
        }
    }

    private static StructureBoundingBox includeBoundingBox(@Nullable StructureBoundingBox structureboundingbox, StructureBoundingBox structureboundingbox1) {
        return structureboundingbox == null ? structureboundingbox1 : StructureBoundingBox.encapsulating(structureboundingbox, structureboundingbox1);
    }

    @VisibleForTesting
    public Beardifier(List<Beardifier.a> list, List<WorldGenFeatureDefinedStructureJigsawJunction> list1, @Nullable StructureBoundingBox structureboundingbox) {
        this.pieces = list;
        this.junctions = list1;
        this.affectedBox = structureboundingbox;
    }

    @Override
    public void fillArray(double[] adouble, DensityFunction.a densityfunction_a) {
        if (this.affectedBox == null) {
            Arrays.fill(adouble, 0.0D);
        } else {
            DensityFunctions.c.super.fillArray(adouble, densityfunction_a);
        }

    }

    @Override
    public double compute(DensityFunction.b densityfunction_b) {
        if (this.affectedBox == null) {
            return 0.0D;
        } else {
            int i = densityfunction_b.blockX();
            int j = densityfunction_b.blockY();
            int k = densityfunction_b.blockZ();

            if (!this.affectedBox.isInside(i, j, k)) {
                return 0.0D;
            } else {
                double d0 = 0.0D;

                for (Beardifier.a beardifier_a : this.pieces) {
                    StructureBoundingBox structureboundingbox = beardifier_a.box();
                    int l = beardifier_a.groundLevelDelta();
                    int i1 = Math.max(0, Math.max(structureboundingbox.minX() - i, i - structureboundingbox.maxX()));
                    int j1 = Math.max(0, Math.max(structureboundingbox.minZ() - k, k - structureboundingbox.maxZ()));
                    int k1 = structureboundingbox.minY() + l;
                    int l1 = j - k1;
                    int i2;

                    switch (beardifier_a.terrainAdjustment()) {
                        case NONE:
                            i2 = 0;
                            break;
                        case BURY:
                        case BEARD_THIN:
                            i2 = l1;
                            break;
                        case BEARD_BOX:
                            i2 = Math.max(0, Math.max(k1 - j, j - structureboundingbox.maxY()));
                            break;
                        case ENCAPSULATE:
                            i2 = Math.max(0, Math.max(structureboundingbox.minY() - j, j - structureboundingbox.maxY()));
                            break;
                        default:
                            throw new MatchException((String) null, (Throwable) null);
                    }

                    int j2 = i2;
                    double d1;

                    switch (beardifier_a.terrainAdjustment()) {
                        case NONE:
                            d1 = 0.0D;
                            break;
                        case BURY:
                            d1 = getBuryContribution((double) i1, (double) j2 / 2.0D, (double) j1);
                            break;
                        case BEARD_THIN:
                        case BEARD_BOX:
                            d1 = getBeardContribution(i1, j2, j1, l1) * 0.8D;
                            break;
                        case ENCAPSULATE:
                            d1 = getBuryContribution((double) i1 / 2.0D, (double) j2 / 2.0D, (double) j1 / 2.0D) * 0.8D;
                            break;
                        default:
                            throw new MatchException((String) null, (Throwable) null);
                    }

                    d0 += d1;
                }

                for (WorldGenFeatureDefinedStructureJigsawJunction worldgenfeaturedefinedstructurejigsawjunction : this.junctions) {
                    int k2 = i - worldgenfeaturedefinedstructurejigsawjunction.getSourceX();
                    int l2 = j - worldgenfeaturedefinedstructurejigsawjunction.getSourceGroundY();
                    int i3 = k - worldgenfeaturedefinedstructurejigsawjunction.getSourceZ();

                    d0 += getBeardContribution(k2, l2, i3, l2) * 0.4D;
                }

                return d0;
            }
        }
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(double d0, double d1, double d2) {
        double d3 = MathHelper.length(d0, d1, d2);

        return MathHelper.clampedMap(d3, 0.0D, 6.0D, 1.0D, 0.0D);
    }

    private static double getBeardContribution(int i, int j, int k, int l) {
        int i1 = i + 12;
        int j1 = j + 12;
        int k1 = k + 12;

        if (isInKernelRange(i1) && isInKernelRange(j1) && isInKernelRange(k1)) {
            double d0 = (double) l + 0.5D;
            double d1 = MathHelper.lengthSquared((double) i, d0, (double) k);
            double d2 = -d0 * MathHelper.fastInvSqrt(d1 / 2.0D) / 2.0D;

            return d2 * (double) Beardifier.BEARD_KERNEL[k1 * 24 * 24 + i1 * 24 + j1];
        } else {
            return 0.0D;
        }
    }

    private static boolean isInKernelRange(int i) {
        return i >= 0 && i < 24;
    }

    private static double computeBeardContribution(int i, int j, int k) {
        return computeBeardContribution(i, (double) j + 0.5D, k);
    }

    private static double computeBeardContribution(int i, double d0, int j) {
        double d1 = MathHelper.lengthSquared((double) i, d0, (double) j);
        double d2 = Math.pow(Math.E, -d1 / 16.0D);

        return d2;
    }

    @VisibleForTesting
    public static record a(StructureBoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {

    }
}
