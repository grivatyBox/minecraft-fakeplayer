package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.dimension.DimensionManager;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {

    static Aquifer create(NoiseChunk noisechunk, ChunkCoordIntPair chunkcoordintpair, NoiseRouter noiserouter, PositionalRandomFactory positionalrandomfactory, int i, int j, Aquifer.a aquifer_a) {
        return new Aquifer.c(noisechunk, chunkcoordintpair, noiserouter, positionalrandomfactory, i, j, aquifer_a);
    }

    static Aquifer createDisabled(final Aquifer.a aquifer_a) {
        return new Aquifer() {
            @Nullable
            @Override
            public IBlockData computeSubstance(DensityFunction.b densityfunction_b, double d0) {
                return d0 > 0.0D ? null : aquifer_a.computeFluid(densityfunction_b.blockX(), densityfunction_b.blockY(), densityfunction_b.blockZ()).at(densityfunction_b.blockY());
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Nullable
    IBlockData computeSubstance(DensityFunction.b densityfunction_b, double d0);

    boolean shouldScheduleFluidUpdate();

    public static class c implements Aquifer {

        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final int X_SEPARATION = 6;
        private static final int Y_SEPARATION = 3;
        private static final int Z_SEPARATION = 6;
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;
        private static final int X_SPACING_SHIFT = 4;
        private static final int Z_SPACING_SHIFT = 4;
        private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
        private static final double FLOWING_UPDATE_SIMULARITY = similarity(MathHelper.square(10), MathHelper.square(12));
        private static final int SAMPLE_OFFSET_X = -5;
        private static final int SAMPLE_OFFSET_Y = 1;
        private static final int SAMPLE_OFFSET_Z = -5;
        private static final int MIN_CELL_SAMPLE_X = 0;
        private static final int MIN_CELL_SAMPLE_Y = -1;
        private static final int MIN_CELL_SAMPLE_Z = 0;
        private static final int MAX_CELL_SAMPLE_X = 1;
        private static final int MAX_CELL_SAMPLE_Y = 1;
        private static final int MAX_CELL_SAMPLE_Z = 1;
        private final NoiseChunk noiseChunk;
        private final DensityFunction barrierNoise;
        private final DensityFunction fluidLevelFloodednessNoise;
        private final DensityFunction fluidLevelSpreadNoise;
        private final DensityFunction lavaNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        private final Aquifer.b[] aquiferCache;
        private final long[] aquiferLocationCache;
        private final Aquifer.a globalFluidPicker;
        private final DensityFunction erosion;
        private final DensityFunction depth;
        private boolean shouldScheduleFluidUpdate;
        private final int skipSamplingAboveY;
        private final int minGridX;
        private final int minGridY;
        private final int minGridZ;
        private final int gridSizeX;
        private final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};

        c(NoiseChunk noisechunk, ChunkCoordIntPair chunkcoordintpair, NoiseRouter noiserouter, PositionalRandomFactory positionalrandomfactory, int i, int j, Aquifer.a aquifer_a) {
            this.noiseChunk = noisechunk;
            this.barrierNoise = noiserouter.barrierNoise();
            this.fluidLevelFloodednessNoise = noiserouter.fluidLevelFloodednessNoise();
            this.fluidLevelSpreadNoise = noiserouter.fluidLevelSpreadNoise();
            this.lavaNoise = noiserouter.lavaNoise();
            this.erosion = noiserouter.erosion();
            this.depth = noiserouter.depth();
            this.positionalRandomFactory = positionalrandomfactory;
            this.minGridX = gridX(chunkcoordintpair.getMinBlockX() + -5) + 0;
            this.globalFluidPicker = aquifer_a;
            int k = gridX(chunkcoordintpair.getMaxBlockX() + -5) + 1;

            this.gridSizeX = k - this.minGridX + 1;
            this.minGridY = gridY(i + 1) + -1;
            int l = gridY(i + j + 1) + 1;
            int i1 = l - this.minGridY + 1;

            this.minGridZ = gridZ(chunkcoordintpair.getMinBlockZ() + -5) + 0;
            int j1 = gridZ(chunkcoordintpair.getMaxBlockZ() + -5) + 1;

            this.gridSizeZ = j1 - this.minGridZ + 1;
            int k1 = this.gridSizeX * i1 * this.gridSizeZ;

            this.aquiferCache = new Aquifer.b[k1];
            this.aquiferLocationCache = new long[k1];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
            int l1 = this.adjustSurfaceLevel(noisechunk.maxPreliminarySurfaceLevel(fromGridX(this.minGridX, 0), fromGridZ(this.minGridZ, 0), fromGridX(k, 9), fromGridZ(j1, 9)));
            int i2 = gridY(l1 + 12) - -1;

            this.skipSamplingAboveY = fromGridY(i2, 11) - 1;
        }

        private int getIndex(int i, int j, int k) {
            int l = i - this.minGridX;
            int i1 = j - this.minGridY;
            int j1 = k - this.minGridZ;

            return (i1 * this.gridSizeZ + j1) * this.gridSizeX + l;
        }

        @Nullable
        @Override
        public IBlockData computeSubstance(DensityFunction.b densityfunction_b, double d0) {
            if (d0 > 0.0D) {
                this.shouldScheduleFluidUpdate = false;
                return null;
            } else {
                int i = densityfunction_b.blockX();
                int j = densityfunction_b.blockY();
                int k = densityfunction_b.blockZ();
                Aquifer.b aquifer_b = this.globalFluidPicker.computeFluid(i, j, k);

                if (j > this.skipSamplingAboveY) {
                    this.shouldScheduleFluidUpdate = false;
                    return aquifer_b.at(j);
                } else if (aquifer_b.at(j).is(Blocks.LAVA)) {
                    this.shouldScheduleFluidUpdate = false;
                    return SharedConstants.DEBUG_DISABLE_FLUID_GENERATION ? Blocks.AIR.defaultBlockState() : Blocks.LAVA.defaultBlockState();
                } else {
                    int l = gridX(i + -5);
                    int i1 = gridY(j + 1);
                    int j1 = gridZ(k + -5);
                    int k1 = Integer.MAX_VALUE;
                    int l1 = Integer.MAX_VALUE;
                    int i2 = Integer.MAX_VALUE;
                    int j2 = Integer.MAX_VALUE;
                    int k2 = 0;
                    int l2 = 0;
                    int i3 = 0;
                    int j3 = 0;

                    for (int k3 = 0; k3 <= 1; ++k3) {
                        for (int l3 = -1; l3 <= 1; ++l3) {
                            for (int i4 = 0; i4 <= 1; ++i4) {
                                int j4 = l + k3;
                                int k4 = i1 + l3;
                                int l4 = j1 + i4;
                                int i5 = this.getIndex(j4, k4, l4);
                                long j5 = this.aquiferLocationCache[i5];
                                long k5;

                                if (j5 != Long.MAX_VALUE) {
                                    k5 = j5;
                                } else {
                                    RandomSource randomsource = this.positionalRandomFactory.at(j4, k4, l4);

                                    k5 = BlockPosition.asLong(fromGridX(j4, randomsource.nextInt(10)), fromGridY(k4, randomsource.nextInt(9)), fromGridZ(l4, randomsource.nextInt(10)));
                                    this.aquiferLocationCache[i5] = k5;
                                }

                                int l5 = BlockPosition.getX(k5) - i;
                                int i6 = BlockPosition.getY(k5) - j;
                                int j6 = BlockPosition.getZ(k5) - k;
                                int k6 = l5 * l5 + i6 * i6 + j6 * j6;

                                if (k1 >= k6) {
                                    j3 = i3;
                                    i3 = l2;
                                    l2 = k2;
                                    k2 = i5;
                                    j2 = i2;
                                    i2 = l1;
                                    l1 = k1;
                                    k1 = k6;
                                } else if (l1 >= k6) {
                                    j3 = i3;
                                    i3 = l2;
                                    l2 = i5;
                                    j2 = i2;
                                    i2 = l1;
                                    l1 = k6;
                                } else if (i2 >= k6) {
                                    j3 = i3;
                                    i3 = i5;
                                    j2 = i2;
                                    i2 = k6;
                                } else if (j2 >= k6) {
                                    j3 = i5;
                                    j2 = k6;
                                }
                            }
                        }
                    }

                    Aquifer.b aquifer_b1 = this.getAquiferStatus(k2);
                    double d1 = similarity(k1, l1);
                    IBlockData iblockdata = aquifer_b1.at(j);
                    IBlockData iblockdata1 = SharedConstants.DEBUG_DISABLE_FLUID_GENERATION ? Blocks.AIR.defaultBlockState() : iblockdata;

                    if (d1 <= 0.0D) {
                        if (d1 >= Aquifer.c.FLOWING_UPDATE_SIMULARITY) {
                            Aquifer.b aquifer_b2 = this.getAquiferStatus(l2);

                            this.shouldScheduleFluidUpdate = !aquifer_b1.equals(aquifer_b2);
                        } else {
                            this.shouldScheduleFluidUpdate = false;
                        }

                        return iblockdata1;
                    } else if (iblockdata.is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
                        this.shouldScheduleFluidUpdate = true;
                        return iblockdata1;
                    } else {
                        MutableDouble mutabledouble = new MutableDouble(Double.NaN);
                        Aquifer.b aquifer_b3 = this.getAquiferStatus(l2);
                        double d2 = d1 * this.calculatePressure(densityfunction_b, mutabledouble, aquifer_b1, aquifer_b3);

                        if (d0 + d2 > 0.0D) {
                            this.shouldScheduleFluidUpdate = false;
                            return null;
                        } else {
                            Aquifer.b aquifer_b4 = this.getAquiferStatus(i3);
                            double d3 = similarity(k1, i2);

                            if (d3 > 0.0D) {
                                double d4 = d1 * d3 * this.calculatePressure(densityfunction_b, mutabledouble, aquifer_b1, aquifer_b4);

                                if (d0 + d4 > 0.0D) {
                                    this.shouldScheduleFluidUpdate = false;
                                    return null;
                                }
                            }

                            double d5 = similarity(l1, i2);

                            if (d5 > 0.0D) {
                                double d6 = d1 * d5 * this.calculatePressure(densityfunction_b, mutabledouble, aquifer_b3, aquifer_b4);

                                if (d0 + d6 > 0.0D) {
                                    this.shouldScheduleFluidUpdate = false;
                                    return null;
                                }
                            }

                            boolean flag = !aquifer_b1.equals(aquifer_b3);
                            boolean flag1 = d5 >= Aquifer.c.FLOWING_UPDATE_SIMULARITY && !aquifer_b3.equals(aquifer_b4);
                            boolean flag2 = d3 >= Aquifer.c.FLOWING_UPDATE_SIMULARITY && !aquifer_b1.equals(aquifer_b4);

                            if (!flag && !flag1 && !flag2) {
                                this.shouldScheduleFluidUpdate = d3 >= Aquifer.c.FLOWING_UPDATE_SIMULARITY && similarity(k1, j2) >= Aquifer.c.FLOWING_UPDATE_SIMULARITY && !aquifer_b1.equals(this.getAquiferStatus(j3));
                            } else {
                                this.shouldScheduleFluidUpdate = true;
                            }

                            return iblockdata1;
                        }
                    }
                }
            }
        }

        @Override
        public boolean shouldScheduleFluidUpdate() {
            return this.shouldScheduleFluidUpdate;
        }

        private static double similarity(int i, int j) {
            double d0 = 25.0D;

            return 1.0D - (double) (j - i) / 25.0D;
        }

        private double calculatePressure(DensityFunction.b densityfunction_b, MutableDouble mutabledouble, Aquifer.b aquifer_b, Aquifer.b aquifer_b1) {
            int i = densityfunction_b.blockY();
            IBlockData iblockdata = aquifer_b.at(i);
            IBlockData iblockdata1 = aquifer_b1.at(i);

            if ((!iblockdata.is(Blocks.LAVA) || !iblockdata1.is(Blocks.WATER)) && (!iblockdata.is(Blocks.WATER) || !iblockdata1.is(Blocks.LAVA))) {
                int j = Math.abs(aquifer_b.fluidLevel - aquifer_b1.fluidLevel);

                if (j == 0) {
                    return 0.0D;
                } else {
                    double d0 = 0.5D * (double) (aquifer_b.fluidLevel + aquifer_b1.fluidLevel);
                    double d1 = (double) i + 0.5D - d0;
                    double d2 = (double) j / 2.0D;
                    double d3 = 0.0D;
                    double d4 = 2.5D;
                    double d5 = 1.5D;
                    double d6 = 3.0D;
                    double d7 = 10.0D;
                    double d8 = 3.0D;
                    double d9 = d2 - Math.abs(d1);
                    double d10;

                    if (d1 > 0.0D) {
                        double d11 = 0.0D + d9;

                        if (d11 > 0.0D) {
                            d10 = d11 / 1.5D;
                        } else {
                            d10 = d11 / 2.5D;
                        }
                    } else {
                        double d12 = 3.0D + d9;

                        if (d12 > 0.0D) {
                            d10 = d12 / 3.0D;
                        } else {
                            d10 = d12 / 10.0D;
                        }
                    }

                    double d13 = 2.0D;
                    double d14;

                    if (d10 >= -2.0D && d10 <= 2.0D) {
                        double d15 = mutabledouble.getValue();

                        if (Double.isNaN(d15)) {
                            double d16 = this.barrierNoise.compute(densityfunction_b);

                            mutabledouble.setValue(d16);
                            d14 = d16;
                        } else {
                            d14 = d15;
                        }
                    } else {
                        d14 = 0.0D;
                    }

                    return 2.0D * (d14 + d10);
                }
            } else {
                return 2.0D;
            }
        }

        private static int gridX(int i) {
            return i >> 4;
        }

        private static int fromGridX(int i, int j) {
            return (i << 4) + j;
        }

        private static int gridY(int i) {
            return Math.floorDiv(i, 12);
        }

        private static int fromGridY(int i, int j) {
            return i * 12 + j;
        }

        private static int gridZ(int i) {
            return i >> 4;
        }

        private static int fromGridZ(int i, int j) {
            return (i << 4) + j;
        }

        private Aquifer.b getAquiferStatus(int i) {
            Aquifer.b aquifer_b = this.aquiferCache[i];

            if (aquifer_b != null) {
                return aquifer_b;
            } else {
                long j = this.aquiferLocationCache[i];
                Aquifer.b aquifer_b1 = this.computeFluid(BlockPosition.getX(j), BlockPosition.getY(j), BlockPosition.getZ(j));

                this.aquiferCache[i] = aquifer_b1;
                return aquifer_b1;
            }
        }

        private Aquifer.b computeFluid(int i, int j, int k) {
            Aquifer.b aquifer_b = this.globalFluidPicker.computeFluid(i, j, k);
            int l = Integer.MAX_VALUE;
            int i1 = j + 12;
            int j1 = j - 12;
            boolean flag = false;

            for (int[] aint : Aquifer.c.SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                int k1 = i + SectionPosition.sectionToBlockCoord(aint[0]);
                int l1 = k + SectionPosition.sectionToBlockCoord(aint[1]);
                int i2 = this.noiseChunk.preliminarySurfaceLevel(k1, l1);
                int j2 = this.adjustSurfaceLevel(i2);
                boolean flag1 = aint[0] == 0 && aint[1] == 0;

                if (flag1 && j1 > j2) {
                    return aquifer_b;
                }

                boolean flag2 = i1 > j2;

                if (flag2 || flag1) {
                    Aquifer.b aquifer_b1 = this.globalFluidPicker.computeFluid(k1, j2, l1);

                    if (!aquifer_b1.at(j2).isAir()) {
                        if (flag1) {
                            flag = true;
                        }

                        if (flag2) {
                            return aquifer_b1;
                        }
                    }
                }

                l = Math.min(l, i2);
            }

            int k2 = this.computeSurfaceLevel(i, j, k, aquifer_b, l, flag);

            return new Aquifer.b(k2, this.computeFluidType(i, j, k, aquifer_b, k2));
        }

        private int adjustSurfaceLevel(int i) {
            return i + 8;
        }

        private int computeSurfaceLevel(int i, int j, int k, Aquifer.b aquifer_b, int l, boolean flag) {
            DensityFunction.e densityfunction_e = new DensityFunction.e(i, j, k);
            double d0;
            double d1;

            if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, densityfunction_e)) {
                d0 = -1.0D;
                d1 = -1.0D;
            } else {
                int i1 = l + 8 - j;
                int j1 = 64;
                double d2 = flag ? MathHelper.clampedMap((double) i1, 0.0D, 64.0D, 1.0D, 0.0D) : 0.0D;
                double d3 = MathHelper.clamp(this.fluidLevelFloodednessNoise.compute(densityfunction_e), -1.0D, 1.0D);
                double d4 = MathHelper.map(d2, 1.0D, 0.0D, -0.3D, 0.8D);
                double d5 = MathHelper.map(d2, 1.0D, 0.0D, -0.8D, 0.4D);

                d0 = d3 - d5;
                d1 = d3 - d4;
            }

            int k1;

            if (d1 > 0.0D) {
                k1 = aquifer_b.fluidLevel;
            } else if (d0 > 0.0D) {
                k1 = this.computeRandomizedFluidSurfaceLevel(i, j, k, l);
            } else {
                k1 = DimensionManager.WAY_BELOW_MIN_Y;
            }

            return k1;
        }

        private int computeRandomizedFluidSurfaceLevel(int i, int j, int k, int l) {
            int i1 = 16;
            int j1 = 40;
            int k1 = Math.floorDiv(i, 16);
            int l1 = Math.floorDiv(j, 40);
            int i2 = Math.floorDiv(k, 16);
            int j2 = l1 * 40 + 20;
            int k2 = 10;
            double d0 = this.fluidLevelSpreadNoise.compute(new DensityFunction.e(k1, l1, i2)) * 10.0D;
            int l2 = MathHelper.quantize(d0, 3);
            int i3 = j2 + l2;

            return Math.min(l, i3);
        }

        private IBlockData computeFluidType(int i, int j, int k, Aquifer.b aquifer_b, int l) {
            IBlockData iblockdata = aquifer_b.fluidType;

            if (l <= -10 && l != DimensionManager.WAY_BELOW_MIN_Y && aquifer_b.fluidType != Blocks.LAVA.defaultBlockState()) {
                int i1 = 64;
                int j1 = 40;
                int k1 = Math.floorDiv(i, 64);
                int l1 = Math.floorDiv(j, 40);
                int i2 = Math.floorDiv(k, 64);
                double d0 = this.lavaNoise.compute(new DensityFunction.e(k1, l1, i2));

                if (Math.abs(d0) > 0.3D) {
                    iblockdata = Blocks.LAVA.defaultBlockState();
                }
            }

            return iblockdata;
        }
    }

    public static record b(int fluidLevel, IBlockData fluidType) {

        public IBlockData at(int i) {
            return i < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
        }
    }

    public interface a {

        Aquifer.b computeFluid(int i, int j, int k);
    }
}
