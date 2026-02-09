package net.minecraft.world.level.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.DataBits;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;

public class DataPaletteBlock<T> implements DataPaletteExpandable<T>, PalettedContainerRO<T> {

    private static final int MIN_PALETTE_BITS = 0;
    private volatile DataPaletteBlock.b<T> data;
    private final Strategy<T> strategy;
    private final ThreadingDetector threadingDetector = new ThreadingDetector("PalettedContainer");

    public void acquire() {
        this.threadingDetector.checkAndLock();
    }

    public void release() {
        this.threadingDetector.checkAndUnlock();
    }

    public static <T> Codec<DataPaletteBlock<T>> codecRW(Codec<T> codec, Strategy<T> strategy, T t0) {
        PalettedContainerRO.b<T, DataPaletteBlock<T>> palettedcontainerro_b = DataPaletteBlock::unpack;

        return codec(codec, strategy, t0, palettedcontainerro_b);
    }

    public static <T> Codec<PalettedContainerRO<T>> codecRO(Codec<T> codec, Strategy<T> strategy, T t0) {
        PalettedContainerRO.b<T, PalettedContainerRO<T>> palettedcontainerro_b = (strategy1, palettedcontainerro_a) -> {
            return unpack(strategy1, palettedcontainerro_a).map((datapaletteblock) -> {
                return datapaletteblock;
            });
        };

        return codec(codec, strategy, t0, palettedcontainerro_b);
    }

    private static <T, C extends PalettedContainerRO<T>> Codec<C> codec(Codec<T> codec, Strategy<T> strategy, T t0, PalettedContainerRO.b<T, C> palettedcontainerro_b) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(codec.mapResult(ExtraCodecs.orElsePartial(t0)).listOf().fieldOf("palette").forGetter(PalettedContainerRO.a::paletteEntries), Codec.LONG_STREAM.lenientOptionalFieldOf("data").forGetter(PalettedContainerRO.a::storage)).apply(instance, PalettedContainerRO.a::new);
        }).comapFlatMap((palettedcontainerro_a) -> {
            return palettedcontainerro_b.read(strategy, palettedcontainerro_a);
        }, (palettedcontainerro) -> {
            return palettedcontainerro.pack(strategy);
        });
    }

    private DataPaletteBlock(Strategy<T> strategy, Configuration configuration, DataBits databits, DataPalette<T> datapalette) {
        this.strategy = strategy;
        this.data = new DataPaletteBlock.b<T>(configuration, databits, datapalette);
    }

    private DataPaletteBlock(DataPaletteBlock<T> datapaletteblock) {
        this.strategy = datapaletteblock.strategy;
        this.data = datapaletteblock.data.copy();
    }

    public DataPaletteBlock(T t0, Strategy<T> strategy) {
        this.strategy = strategy;
        this.data = this.createOrReuseData((DataPaletteBlock.b) null, 0);
        this.data.palette.idFor(t0, this);
    }

    private DataPaletteBlock.b<T> createOrReuseData(@Nullable DataPaletteBlock.b<T> datapaletteblock_b, int i) {
        Configuration configuration = this.strategy.getConfigurationForBitCount(i);

        if (datapaletteblock_b != null && configuration.equals(datapaletteblock_b.configuration())) {
            return datapaletteblock_b;
        } else {
            DataBits databits = (DataBits) (configuration.bitsInMemory() == 0 ? new ZeroBitStorage(this.strategy.entryCount()) : new SimpleBitStorage(configuration.bitsInMemory(), this.strategy.entryCount()));
            DataPalette<T> datapalette = configuration.<T>createPalette(this.strategy, List.of());

            return new DataPaletteBlock.b<T>(configuration, databits, datapalette);
        }
    }

    @Override
    public int onResize(int i, T t0) {
        DataPaletteBlock.b<T> datapaletteblock_b = this.data;
        DataPaletteBlock.b<T> datapaletteblock_b1 = this.createOrReuseData(datapaletteblock_b, i);

        datapaletteblock_b1.copyFrom(datapaletteblock_b.palette, datapaletteblock_b.storage);
        this.data = datapaletteblock_b1;
        return datapaletteblock_b1.palette.idFor(t0, DataPaletteExpandable.noResizeExpected());
    }

    public T getAndSet(int i, int j, int k, T t0) {
        this.acquire();

        Object object;

        try {
            object = this.getAndSet(this.strategy.getIndex(i, j, k), t0);
        } finally {
            this.release();
        }

        return (T) object;
    }

    public T getAndSetUnchecked(int i, int j, int k, T t0) {
        return (T) this.getAndSet(this.strategy.getIndex(i, j, k), t0);
    }

    private T getAndSet(int i, T t0) {
        int j = this.data.palette.idFor(t0, this);
        int k = this.data.storage.getAndSet(i, j);

        return this.data.palette.valueFor(k);
    }

    public void set(int i, int j, int k, T t0) {
        this.acquire();

        try {
            this.set(this.strategy.getIndex(i, j, k), t0);
        } finally {
            this.release();
        }

    }

    private void set(int i, T t0) {
        int j = this.data.palette.idFor(t0, this);

        this.data.storage.set(i, j);
    }

    @Override
    public T get(int i, int j, int k) {
        return (T) this.get(this.strategy.getIndex(i, j, k));
    }

    protected T get(int i) {
        DataPaletteBlock.b<T> datapaletteblock_b = this.data;

        return datapaletteblock_b.palette.valueFor(datapaletteblock_b.storage.get(i));
    }

    @Override
    public void getAll(Consumer<T> consumer) {
        DataPalette<T> datapalette = this.data.palette();
        IntSet intset = new IntArraySet();
        DataBits databits = this.data.storage;

        Objects.requireNonNull(intset);
        databits.getAll(intset::add);
        intset.forEach((i) -> {
            consumer.accept(datapalette.valueFor(i));
        });
    }

    public void read(PacketDataSerializer packetdataserializer) {
        this.acquire();

        try {
            int i = packetdataserializer.readByte();
            DataPaletteBlock.b<T> datapaletteblock_b = this.createOrReuseData(this.data, i);

            datapaletteblock_b.palette.read(packetdataserializer, this.strategy.globalMap());
            packetdataserializer.readFixedSizeLongArray(datapaletteblock_b.storage.getRaw());
            this.data = datapaletteblock_b;
        } finally {
            this.release();
        }

    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        this.acquire();

        try {
            this.data.write(packetdataserializer, this.strategy.globalMap());
        } finally {
            this.release();
        }

    }

    @VisibleForTesting
    public static <T> DataResult<DataPaletteBlock<T>> unpack(Strategy<T> strategy, PalettedContainerRO.a<T> palettedcontainerro_a) {
        List<T> list = palettedcontainerro_a.paletteEntries();
        int i = strategy.entryCount();
        Configuration configuration = strategy.getConfigurationForPaletteSize(list.size());
        int j = configuration.bitsInStorage();

        if (palettedcontainerro_a.bitsPerEntry() != -1 && j != palettedcontainerro_a.bitsPerEntry()) {
            return DataResult.error(() -> {
                return "Invalid bit count, calculated " + j + ", but container declared " + palettedcontainerro_a.bitsPerEntry();
            });
        } else {
            DataBits databits;
            DataPalette<T> datapalette;

            if (configuration.bitsInMemory() == 0) {
                datapalette = configuration.<T>createPalette(strategy, list);
                databits = new ZeroBitStorage(i);
            } else {
                Optional<LongStream> optional = palettedcontainerro_a.storage();

                if (optional.isEmpty()) {
                    return DataResult.error(() -> {
                        return "Missing values for non-zero storage";
                    });
                }

                long[] along = ((LongStream) optional.get()).toArray();

                try {
                    if (!configuration.alwaysRepack() && configuration.bitsInMemory() == j) {
                        datapalette = configuration.<T>createPalette(strategy, list);
                        databits = new SimpleBitStorage(configuration.bitsInMemory(), i, along);
                    } else {
                        DataPalette<T> datapalette1 = new DataPaletteHash<T>(j, list);
                        SimpleBitStorage simplebitstorage = new SimpleBitStorage(j, i, along);
                        DataPalette<T> datapalette2 = configuration.<T>createPalette(strategy, list);
                        int[] aint = reencodeContents(simplebitstorage, datapalette1, datapalette2);

                        datapalette = datapalette2;
                        databits = new SimpleBitStorage(configuration.bitsInMemory(), i, aint);
                    }
                } catch (SimpleBitStorage.a simplebitstorage_a) {
                    return DataResult.error(() -> {
                        return "Failed to read PalettedContainer: " + simplebitstorage_a.getMessage();
                    });
                }
            }

            return DataResult.success(new DataPaletteBlock(strategy, configuration, databits, datapalette));
        }
    }

    @Override
    public PalettedContainerRO.a<T> pack(Strategy<T> strategy) {
        this.acquire();

        PalettedContainerRO.a palettedcontainerro_a;

        try {
            DataBits databits = this.data.storage;
            DataPalette<T> datapalette = this.data.palette;
            DataPaletteHash<T> datapalettehash = new DataPaletteHash<T>(databits.getBits());
            int i = strategy.entryCount();
            int[] aint = reencodeContents(databits, datapalette, datapalettehash);
            Configuration configuration = strategy.getConfigurationForPaletteSize(datapalettehash.getSize());
            int j = configuration.bitsInStorage();
            Optional<LongStream> optional;

            if (j != 0) {
                SimpleBitStorage simplebitstorage = new SimpleBitStorage(j, i, aint);

                optional = Optional.of(Arrays.stream(simplebitstorage.getRaw()));
            } else {
                optional = Optional.empty();
            }

            palettedcontainerro_a = new PalettedContainerRO.a(datapalettehash.getEntries(), optional, j);
        } finally {
            this.release();
        }

        return palettedcontainerro_a;
    }

    private static <T> int[] reencodeContents(DataBits databits, DataPalette<T> datapalette, DataPalette<T> datapalette1) {
        int[] aint = new int[databits.getSize()];

        databits.unpack(aint);
        DataPaletteExpandable<T> datapaletteexpandable = DataPaletteExpandable.<T>noResizeExpected();
        int i = -1;
        int j = -1;

        for (int k = 0; k < aint.length; ++k) {
            int l = aint[k];

            if (l != i) {
                i = l;
                j = datapalette1.idFor(datapalette.valueFor(l), datapaletteexpandable);
            }

            aint[k] = j;
        }

        return aint;
    }

    @Override
    public int getSerializedSize() {
        return this.data.getSerializedSize(this.strategy.globalMap());
    }

    @Override
    public int bitsPerEntry() {
        return this.data.storage().getBits();
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        return this.data.palette.maybeHas(predicate);
    }

    @Override
    public DataPaletteBlock<T> copy() {
        return new DataPaletteBlock<T>(this);
    }

    @Override
    public DataPaletteBlock<T> recreate() {
        return new DataPaletteBlock<T>(this.data.palette.valueFor(0), this.strategy);
    }

    @Override
    public void count(DataPaletteBlock.a<T> datapaletteblock_a) {
        if (this.data.palette.getSize() == 1) {
            datapaletteblock_a.accept(this.data.palette.valueFor(0), this.data.storage.getSize());
        } else {
            Int2IntOpenHashMap int2intopenhashmap = new Int2IntOpenHashMap();

            this.data.storage.getAll((i) -> {
                int2intopenhashmap.addTo(i, 1);
            });
            int2intopenhashmap.int2IntEntrySet().forEach((entry) -> {
                datapaletteblock_a.accept(this.data.palette.valueFor(entry.getIntKey()), entry.getIntValue());
            });
        }
    }

    private static record b<T>(Configuration configuration, DataBits storage, DataPalette<T> palette) {

        public void copyFrom(DataPalette<T> datapalette, DataBits databits) {
            DataPaletteExpandable<T> datapaletteexpandable = DataPaletteExpandable.<T>noResizeExpected();

            for (int i = 0; i < databits.getSize(); ++i) {
                T t0 = datapalette.valueFor(databits.get(i));

                this.storage.set(i, this.palette.idFor(t0, datapaletteexpandable));
            }

        }

        public int getSerializedSize(Registry<T> registry) {
            return 1 + this.palette.getSerializedSize(registry) + this.storage.getRaw().length * 8;
        }

        public void write(PacketDataSerializer packetdataserializer, Registry<T> registry) {
            packetdataserializer.writeByte(this.storage.getBits());
            this.palette.write(packetdataserializer, registry);
            packetdataserializer.writeFixedSizeLongArray(this.storage.getRaw());
        }

        public DataPaletteBlock.b<T> copy() {
            return new DataPaletteBlock.b<T>(this.configuration, this.storage.copy(), this.palette.copy());
        }
    }

    @FunctionalInterface
    public interface a<T> {

        void accept(T t0, int i);
    }
}
