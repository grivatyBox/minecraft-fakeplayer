package net.minecraft.world.level;

public class DryFoliageColor {

    public static final int FOLIAGE_DRY_DEFAULT = -10732494;
    private static int[] pixels = new int[65536];

    public DryFoliageColor() {}

    public static void init(int[] aint) {
        DryFoliageColor.pixels = aint;
    }

    public static int get(double d0, double d1) {
        return ColorMapColorUtil.get(d0, d1, DryFoliageColor.pixels, -10732494);
    }
}
