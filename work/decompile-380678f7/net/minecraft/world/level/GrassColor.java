package net.minecraft.world.level;

public class GrassColor {

    private static int[] pixels = new int[65536];

    public GrassColor() {}

    public static void init(int[] aint) {
        GrassColor.pixels = aint;
    }

    public static int get(double d0, double d1) {
        return ColorMapColorUtil.get(d0, d1, GrassColor.pixels, -65281);
    }

    public static int getDefaultColor() {
        return get(0.5D, 1.0D);
    }
}
