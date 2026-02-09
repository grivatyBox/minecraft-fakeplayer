package net.minecraft.world.level;

public interface ColorMapColorUtil {

    static int get(double d0, double d1, int[] aint, int i) {
        d1 *= d0;
        int j = (int) ((1.0D - d0) * 255.0D);
        int k = (int) ((1.0D - d1) * 255.0D);
        int l = k << 8 | j;

        return l >= aint.length ? i : aint[l];
    }
}
