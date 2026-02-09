package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPosition;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureLakeConfiguration;

public class WorldGenFeatureIceburg extends WorldGenerator<WorldGenFeatureLakeConfiguration> {

    public WorldGenFeatureIceburg(Codec<WorldGenFeatureLakeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<WorldGenFeatureLakeConfiguration> featureplacecontext) {
        BlockPosition blockposition = featureplacecontext.origin();
        GeneratorAccessSeed generatoraccessseed = featureplacecontext.level();

        blockposition = new BlockPosition(blockposition.getX(), featureplacecontext.chunkGenerator().getSeaLevel(), blockposition.getZ());
        RandomSource randomsource = featureplacecontext.random();
        boolean flag = randomsource.nextDouble() > 0.7D;
        IBlockData iblockdata = (featureplacecontext.config()).state;
        double d0 = randomsource.nextDouble() * 2.0D * Math.PI;
        int i = 11 - randomsource.nextInt(5);
        int j = 3 + randomsource.nextInt(3);
        boolean flag1 = randomsource.nextDouble() > 0.7D;
        int k = 11;
        int l = flag1 ? randomsource.nextInt(6) + 6 : randomsource.nextInt(15) + 3;

        if (!flag1 && randomsource.nextDouble() > 0.9D) {
            l += randomsource.nextInt(19) + 7;
        }

        int i1 = Math.min(l + randomsource.nextInt(11), 18);
        int j1 = Math.min(l + randomsource.nextInt(7) - randomsource.nextInt(5), 11);
        int k1 = flag1 ? i : 11;

        for (int l1 = -k1; l1 < k1; ++l1) {
            for (int i2 = -k1; i2 < k1; ++i2) {
                for (int j2 = 0; j2 < l; ++j2) {
                    int k2 = flag1 ? this.heightDependentRadiusEllipse(j2, l, j1) : this.heightDependentRadiusRound(randomsource, j2, l, j1);

                    if (flag1 || l1 < k2) {
                        this.generateIcebergBlock(generatoraccessseed, randomsource, blockposition, l, l1, j2, i2, k2, k1, flag1, j, d0, flag, iblockdata);
                    }
                }
            }
        }

        this.smooth(generatoraccessseed, blockposition, j1, l, flag1, i);

        for (int l2 = -k1; l2 < k1; ++l2) {
            for (int i3 = -k1; i3 < k1; ++i3) {
                for (int j3 = -1; j3 > -i1; --j3) {
                    int k3 = flag1 ? MathHelper.ceil((float) k1 * (1.0F - (float) Math.pow((double) j3, 2.0D) / ((float) i1 * 8.0F))) : k1;
                    int l3 = this.heightDependentRadiusSteep(randomsource, -j3, i1, j1);

                    if (l2 < l3) {
                        this.generateIcebergBlock(generatoraccessseed, randomsource, blockposition, i1, l2, j3, i3, l3, k3, flag1, j, d0, flag, iblockdata);
                    }
                }
            }
        }

        boolean flag2 = flag1 ? randomsource.nextDouble() > 0.1D : randomsource.nextDouble() > 0.7D;

        if (flag2) {
            this.generateCutOut(randomsource, generatoraccessseed, j1, l, blockposition, flag1, i, d0, j);
        }

        return true;
    }

    private void generateCutOut(RandomSource randomsource, GeneratorAccess generatoraccess, int i, int j, BlockPosition blockposition, boolean flag, int k, double d0, int l) {
        int i1 = randomsource.nextBoolean() ? -1 : 1;
        int j1 = randomsource.nextBoolean() ? -1 : 1;
        int k1 = randomsource.nextInt(Math.max(i / 2 - 2, 1));

        if (randomsource.nextBoolean()) {
            k1 = i / 2 + 1 - randomsource.nextInt(Math.max(i - i / 2 - 1, 1));
        }

        int l1 = randomsource.nextInt(Math.max(i / 2 - 2, 1));

        if (randomsource.nextBoolean()) {
            l1 = i / 2 + 1 - randomsource.nextInt(Math.max(i - i / 2 - 1, 1));
        }

        if (flag) {
            k1 = l1 = randomsource.nextInt(Math.max(k - 5, 1));
        }

        BlockPosition blockposition1 = new BlockPosition(i1 * k1, 0, j1 * l1);
        double d1 = flag ? d0 + (Math.PI / 2D) : randomsource.nextDouble() * 2.0D * Math.PI;

        for (int i2 = 0; i2 < j - 3; ++i2) {
            int j2 = this.heightDependentRadiusRound(randomsource, i2, j, i);

            this.carve(j2, i2, blockposition, generatoraccess, false, d1, blockposition1, k, l);
        }

        for (int k2 = -1; k2 > -j + randomsource.nextInt(5); --k2) {
            int l2 = this.heightDependentRadiusSteep(randomsource, -k2, j, i);

            this.carve(l2, k2, blockposition, generatoraccess, true, d1, blockposition1, k, l);
        }

    }

    private void carve(int i, int j, BlockPosition blockposition, GeneratorAccess generatoraccess, boolean flag, double d0, BlockPosition blockposition1, int k, int l) {
        int i1 = i + 1 + k / 3;
        int j1 = Math.min(i - 3, 3) + l / 2 - 1;

        for (int k1 = -i1; k1 < i1; ++k1) {
            for (int l1 = -i1; l1 < i1; ++l1) {
                double d1 = this.signedDistanceEllipse(k1, l1, blockposition1, i1, j1, d0);

                if (d1 < 0.0D) {
                    BlockPosition blockposition2 = blockposition.offset(k1, j, l1);
                    IBlockData iblockdata = generatoraccess.getBlockState(blockposition2);

                    if (isIcebergState(iblockdata) || iblockdata.is(Blocks.SNOW_BLOCK)) {
                        if (flag) {
                            this.setBlock(generatoraccess, blockposition2, Blocks.WATER.defaultBlockState());
                        } else {
                            this.setBlock(generatoraccess, blockposition2, Blocks.AIR.defaultBlockState());
                            this.removeFloatingSnowLayer(generatoraccess, blockposition2);
                        }
                    }
                }
            }
        }

    }

    private void removeFloatingSnowLayer(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        if (generatoraccess.getBlockState(blockposition.above()).is(Blocks.SNOW)) {
            this.setBlock(generatoraccess, blockposition.above(), Blocks.AIR.defaultBlockState());
        }

    }

    private void generateIcebergBlock(GeneratorAccess generatoraccess, RandomSource randomsource, BlockPosition blockposition, int i, int j, int k, int l, int i1, int j1, boolean flag, int k1, double d0, boolean flag1, IBlockData iblockdata) {
        double d1 = flag ? this.signedDistanceEllipse(j, l, BlockPosition.ZERO, j1, this.getEllipseC(k, i, k1), d0) : this.signedDistanceCircle(j, l, BlockPosition.ZERO, i1, randomsource);

        if (d1 < 0.0D) {
            BlockPosition blockposition1 = blockposition.offset(j, k, l);
            double d2 = flag ? -0.5D : (double) (-6 - randomsource.nextInt(3));

            if (d1 > d2 && randomsource.nextDouble() > 0.9D) {
                return;
            }

            this.setIcebergBlock(blockposition1, generatoraccess, randomsource, i - k, i, flag, flag1, iblockdata);
        }

    }

    private void setIcebergBlock(BlockPosition blockposition, GeneratorAccess generatoraccess, RandomSource randomsource, int i, int j, boolean flag, boolean flag1, IBlockData iblockdata) {
        IBlockData iblockdata1 = generatoraccess.getBlockState(blockposition);

        if (iblockdata1.isAir() || iblockdata1.is(Blocks.SNOW_BLOCK) || iblockdata1.is(Blocks.ICE) || iblockdata1.is(Blocks.WATER)) {
            boolean flag2 = !flag || randomsource.nextDouble() > 0.05D;
            int k = flag ? 3 : 2;

            if (flag1 && !iblockdata1.is(Blocks.WATER) && (double) i <= (double) randomsource.nextInt(Math.max(1, j / k)) + (double) j * 0.6D && flag2) {
                this.setBlock(generatoraccess, blockposition, Blocks.SNOW_BLOCK.defaultBlockState());
            } else {
                this.setBlock(generatoraccess, blockposition, iblockdata);
            }
        }

    }

    private int getEllipseC(int i, int j, int k) {
        int l = k;

        if (i > 0 && j - i <= 3) {
            l = k - (4 - (j - i));
        }

        return l;
    }

    private double signedDistanceCircle(int i, int j, BlockPosition blockposition, int k, RandomSource randomsource) {
        float f = 10.0F * MathHelper.clamp(randomsource.nextFloat(), 0.2F, 0.8F) / (float) k;

        return (double) f + Math.pow((double) (i - blockposition.getX()), 2.0D) + Math.pow((double) (j - blockposition.getZ()), 2.0D) - Math.pow((double) k, 2.0D);
    }

    private double signedDistanceEllipse(int i, int j, BlockPosition blockposition, int k, int l, double d0) {
        return Math.pow(((double) (i - blockposition.getX()) * Math.cos(d0) - (double) (j - blockposition.getZ()) * Math.sin(d0)) / (double) k, 2.0D) + Math.pow(((double) (i - blockposition.getX()) * Math.sin(d0) + (double) (j - blockposition.getZ()) * Math.cos(d0)) / (double) l, 2.0D) - 1.0D;
    }

    private int heightDependentRadiusRound(RandomSource randomsource, int i, int j, int k) {
        float f = 3.5F - randomsource.nextFloat();
        float f1 = (1.0F - (float) Math.pow((double) i, 2.0D) / ((float) j * f)) * (float) k;

        if (j > 15 + randomsource.nextInt(5)) {
            int l = i < 3 + randomsource.nextInt(6) ? i / 2 : i;

            f1 = (1.0F - (float) l / ((float) j * f * 0.4F)) * (float) k;
        }

        return MathHelper.ceil(f1 / 2.0F);
    }

    private int heightDependentRadiusEllipse(int i, int j, int k) {
        float f = 1.0F;
        float f1 = (1.0F - (float) Math.pow((double) i, 2.0D) / ((float) j * 1.0F)) * (float) k;

        return MathHelper.ceil(f1 / 2.0F);
    }

    private int heightDependentRadiusSteep(RandomSource randomsource, int i, int j, int k) {
        float f = 1.0F + randomsource.nextFloat() / 2.0F;
        float f1 = (1.0F - (float) i / ((float) j * f)) * (float) k;

        return MathHelper.ceil(f1 / 2.0F);
    }

    private static boolean isIcebergState(IBlockData iblockdata) {
        return iblockdata.is(Blocks.PACKED_ICE) || iblockdata.is(Blocks.SNOW_BLOCK) || iblockdata.is(Blocks.BLUE_ICE);
    }

    private boolean belowIsAir(IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockaccess.getBlockState(blockposition.below()).isAir();
    }

    private void smooth(GeneratorAccess generatoraccess, BlockPosition blockposition, int i, int j, boolean flag, int k) {
        int l = flag ? k : i / 2;

        for (int i1 = -l; i1 <= l; ++i1) {
            for (int j1 = -l; j1 <= l; ++j1) {
                for (int k1 = 0; k1 <= j; ++k1) {
                    BlockPosition blockposition1 = blockposition.offset(i1, k1, j1);
                    IBlockData iblockdata = generatoraccess.getBlockState(blockposition1);

                    if (isIcebergState(iblockdata) || iblockdata.is(Blocks.SNOW)) {
                        if (this.belowIsAir(generatoraccess, blockposition1)) {
                            this.setBlock(generatoraccess, blockposition1, Blocks.AIR.defaultBlockState());
                            this.setBlock(generatoraccess, blockposition1.above(), Blocks.AIR.defaultBlockState());
                        } else if (isIcebergState(iblockdata)) {
                            IBlockData[] aiblockdata = new IBlockData[]{generatoraccess.getBlockState(blockposition1.west()), generatoraccess.getBlockState(blockposition1.east()), generatoraccess.getBlockState(blockposition1.north()), generatoraccess.getBlockState(blockposition1.south())};
                            int l1 = 0;

                            for (IBlockData iblockdata1 : aiblockdata) {
                                if (!isIcebergState(iblockdata1)) {
                                    ++l1;
                                }
                            }

                            if (l1 >= 3) {
                                this.setBlock(generatoraccess, blockposition1, Blocks.AIR.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }

    }
}
