package net.minecraft.world.level.chunk;

import java.util.List;

public interface Configuration {

    boolean alwaysRepack();

    int bitsInMemory();

    int bitsInStorage();

    <T> DataPalette<T> createPalette(Strategy<T> strategy, List<T> list);

    public static record b(DataPalette.a factory, int bits) implements Configuration {

        @Override
        public boolean alwaysRepack() {
            return false;
        }

        @Override
        public <T> DataPalette<T> createPalette(Strategy<T> strategy, List<T> list) {
            return this.factory.<T>create(this.bits, list);
        }

        @Override
        public int bitsInMemory() {
            return this.bits;
        }

        @Override
        public int bitsInStorage() {
            return this.bits;
        }
    }

    public static record a(int bitsInMemory, int bitsInStorage) implements Configuration {

        @Override
        public boolean alwaysRepack() {
            return true;
        }

        @Override
        public <T> DataPalette<T> createPalette(Strategy<T> strategy, List<T> list) {
            return strategy.globalPalette();
        }
    }
}
