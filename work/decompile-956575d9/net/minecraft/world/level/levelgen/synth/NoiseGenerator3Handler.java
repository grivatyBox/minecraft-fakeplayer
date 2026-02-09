package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;

public class NoiseGenerator3Handler {

    protected static final int[][] GRADIENT = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}, {1, 1, 0}, {0, -1, 1}, {-1, 1, 0}, {0, -1, -1}};
    private static final double SQRT_3 = Math.sqrt(3.0D);
    private static final double F2 = 0.5D * (NoiseGenerator3Handler.SQRT_3 - 1.0D);
    private static final double G2 = (3.0D - NoiseGenerator3Handler.SQRT_3) / 6.0D;
    private final int[] p = new int[512];
    public final double xo;
    public final double yo;
    public final double zo;

    public NoiseGenerator3Handler(RandomSource randomsource) {
        this.xo = randomsource.nextDouble() * 256.0D;
        this.yo = randomsource.nextDouble() * 256.0D;
        this.zo = randomsource.nextDouble() * 256.0D;

        for (int i = 0; i < 256; this.p[i] = i++) {
            ;
        }

        for (int j = 0; j < 256; ++j) {
            int k = randomsource.nextInt(256 - j);
            int l = this.p[j];

            this.p[j] = this.p[k + j];
            this.p[k + j] = l;
        }

    }

    private int p(int i) {
        return this.p[i & 255];
    }

    protected static double dot(int[] aint, double d0, double d1, double d2) {
        return (double) aint[0] * d0 + (double) aint[1] * d1 + (double) aint[2] * d2;
    }

    private double getCornerNoise3D(int i, double d0, double d1, double d2, double d3) {
        double d4 = d3 - d0 * d0 - d1 * d1 - d2 * d2;
        double d5;

        if (d4 < 0.0D) {
            d5 = 0.0D;
        } else {
            d4 *= d4;
            d5 = d4 * d4 * dot(NoiseGenerator3Handler.GRADIENT[i], d0, d1, d2);
        }

        return d5;
    }

    public double getValue(double d0, double d1) {
        double d2 = (d0 + d1) * NoiseGenerator3Handler.F2;
        int i = MathHelper.floor(d0 + d2);
        int j = MathHelper.floor(d1 + d2);
        double d3 = (double) (i + j) * NoiseGenerator3Handler.G2;
        double d4 = (double) i - d3;
        double d5 = (double) j - d3;
        double d6 = d0 - d4;
        double d7 = d1 - d5;
        int k;
        int l;

        if (d6 > d7) {
            k = 1;
            l = 0;
        } else {
            k = 0;
            l = 1;
        }

        double d8 = d6 - (double) k + NoiseGenerator3Handler.G2;
        double d9 = d7 - (double) l + NoiseGenerator3Handler.G2;
        double d10 = d6 - 1.0D + 2.0D * NoiseGenerator3Handler.G2;
        double d11 = d7 - 1.0D + 2.0D * NoiseGenerator3Handler.G2;
        int i1 = i & 255;
        int j1 = j & 255;
        int k1 = this.p(i1 + this.p(j1)) % 12;
        int l1 = this.p(i1 + k + this.p(j1 + l)) % 12;
        int i2 = this.p(i1 + 1 + this.p(j1 + 1)) % 12;
        double d12 = this.getCornerNoise3D(k1, d6, d7, 0.0D, 0.5D);
        double d13 = this.getCornerNoise3D(l1, d8, d9, 0.0D, 0.5D);
        double d14 = this.getCornerNoise3D(i2, d10, d11, 0.0D, 0.5D);

        return 70.0D * (d12 + d13 + d14);
    }

    public double getValue(double d0, double d1, double d2) {
        double d3 = 0.3333333333333333D;
        double d4 = (d0 + d1 + d2) * 0.3333333333333333D;
        int i = MathHelper.floor(d0 + d4);
        int j = MathHelper.floor(d1 + d4);
        int k = MathHelper.floor(d2 + d4);
        double d5 = 0.16666666666666666D;
        double d6 = (double) (i + j + k) * 0.16666666666666666D;
        double d7 = (double) i - d6;
        double d8 = (double) j - d6;
        double d9 = (double) k - d6;
        double d10 = d0 - d7;
        double d11 = d1 - d8;
        double d12 = d2 - d9;
        int l;
        int i1;
        int j1;
        int k1;
        int l1;
        int i2;

        if (d10 >= d11) {
            if (d11 >= d12) {
                l = 1;
                i1 = 0;
                j1 = 0;
                k1 = 1;
                l1 = 1;
                i2 = 0;
            } else if (d10 >= d12) {
                l = 1;
                i1 = 0;
                j1 = 0;
                k1 = 1;
                l1 = 0;
                i2 = 1;
            } else {
                l = 0;
                i1 = 0;
                j1 = 1;
                k1 = 1;
                l1 = 0;
                i2 = 1;
            }
        } else if (d11 < d12) {
            l = 0;
            i1 = 0;
            j1 = 1;
            k1 = 0;
            l1 = 1;
            i2 = 1;
        } else if (d10 < d12) {
            l = 0;
            i1 = 1;
            j1 = 0;
            k1 = 0;
            l1 = 1;
            i2 = 1;
        } else {
            l = 0;
            i1 = 1;
            j1 = 0;
            k1 = 1;
            l1 = 1;
            i2 = 0;
        }

        double d13 = d10 - (double) l + 0.16666666666666666D;
        double d14 = d11 - (double) i1 + 0.16666666666666666D;
        double d15 = d12 - (double) j1 + 0.16666666666666666D;
        double d16 = d10 - (double) k1 + 0.3333333333333333D;
        double d17 = d11 - (double) l1 + 0.3333333333333333D;
        double d18 = d12 - (double) i2 + 0.3333333333333333D;
        double d19 = d10 - 1.0D + 0.5D;
        double d20 = d11 - 1.0D + 0.5D;
        double d21 = d12 - 1.0D + 0.5D;
        int j2 = i & 255;
        int k2 = j & 255;
        int l2 = k & 255;
        int i3 = this.p(j2 + this.p(k2 + this.p(l2))) % 12;
        int j3 = this.p(j2 + l + this.p(k2 + i1 + this.p(l2 + j1))) % 12;
        int k3 = this.p(j2 + k1 + this.p(k2 + l1 + this.p(l2 + i2))) % 12;
        int l3 = this.p(j2 + 1 + this.p(k2 + 1 + this.p(l2 + 1))) % 12;
        double d22 = this.getCornerNoise3D(i3, d10, d11, d12, 0.6D);
        double d23 = this.getCornerNoise3D(j3, d13, d14, d15, 0.6D);
        double d24 = this.getCornerNoise3D(k3, d16, d17, d18, 0.6D);
        double d25 = this.getCornerNoise3D(l3, d19, d20, d21, 0.6D);

        return 32.0D * (d22 + d23 + d24 + d25);
    }
}
