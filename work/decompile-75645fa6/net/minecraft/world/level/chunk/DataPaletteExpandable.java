package net.minecraft.world.level.chunk;

public interface DataPaletteExpandable<T> {

    int onResize(int i, T t0);

    static <T> DataPaletteExpandable<T> noResizeExpected() {
        return (i, object) -> {
            throw new IllegalArgumentException("Unexpected palette resize, bits = " + i + ", added value = " + String.valueOf(object));
        };
    }
}
