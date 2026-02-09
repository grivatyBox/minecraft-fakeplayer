package net.minecraft.world.level.chunk;

import net.minecraft.core.Registry;
import net.minecraft.util.MathHelper;

public abstract class Strategy<T> {

    private static final DataPalette.a SINGLE_VALUE_PALETTE_FACTORY = SingleValuePalette::create;
    private static final DataPalette.a LINEAR_PALETTE_FACTORY = DataPaletteLinear::create;
    private static final DataPalette.a HASHMAP_PALETTE_FACTORY = DataPaletteHash::create;
    static final Configuration ZERO_BITS = new Configuration.b(Strategy.SINGLE_VALUE_PALETTE_FACTORY, 0);
    static final Configuration ONE_BIT_LINEAR = new Configuration.b(Strategy.LINEAR_PALETTE_FACTORY, 1);
    static final Configuration TWO_BITS_LINEAR = new Configuration.b(Strategy.LINEAR_PALETTE_FACTORY, 2);
    static final Configuration THREE_BITS_LINEAR = new Configuration.b(Strategy.LINEAR_PALETTE_FACTORY, 3);
    static final Configuration FOUR_BITS_LINEAR = new Configuration.b(Strategy.LINEAR_PALETTE_FACTORY, 4);
    static final Configuration FIVE_BITS_HASHMAP = new Configuration.b(Strategy.HASHMAP_PALETTE_FACTORY, 5);
    static final Configuration SIX_BITS_HASHMAP = new Configuration.b(Strategy.HASHMAP_PALETTE_FACTORY, 6);
    static final Configuration SEVEN_BITS_HASHMAP = new Configuration.b(Strategy.HASHMAP_PALETTE_FACTORY, 7);
    static final Configuration EIGHT_BITS_HASHMAP = new Configuration.b(Strategy.HASHMAP_PALETTE_FACTORY, 8);
    private final Registry<T> globalMap;
    private final DataPaletteGlobal<T> globalPalette;
    protected final int globalPaletteBitsInMemory;
    private final int bitsPerAxis;
    private final int entryCount;

    Strategy(Registry<T> registry, int i) {
        this.globalMap = registry;
        this.globalPalette = new DataPaletteGlobal<T>(registry);
        this.globalPaletteBitsInMemory = minimumBitsRequiredForDistinctValues(registry.size());
        this.bitsPerAxis = i;
        this.entryCount = 1 << i * 3;
    }

    public static <T> Strategy<T> createForBlockStates(Registry<T> registry) {
        return new Strategy<T>(registry, 4) {
            @Override
            public Configuration getConfigurationForBitCount(int i) {
                Object object;

                switch (i) {
                    case 0:
                        object = Strategy.ZERO_BITS;
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        object = Strategy.FOUR_BITS_LINEAR;
                        break;
                    case 5:
                        object = Strategy.FIVE_BITS_HASHMAP;
                        break;
                    case 6:
                        object = Strategy.SIX_BITS_HASHMAP;
                        break;
                    case 7:
                        object = Strategy.SEVEN_BITS_HASHMAP;
                        break;
                    case 8:
                        object = Strategy.EIGHT_BITS_HASHMAP;
                        break;
                    default:
                        object = new Configuration.a(this.globalPaletteBitsInMemory, i);
                }

                return (Configuration) object;
            }
        };
    }

    public static <T> Strategy<T> createForBiomes(Registry<T> registry) {
        return new Strategy<T>(registry, 2) {
            @Override
            public Configuration getConfigurationForBitCount(int i) {
                Object object;

                switch (i) {
                    case 0:
                        object = Strategy.ZERO_BITS;
                        break;
                    case 1:
                        object = Strategy.ONE_BIT_LINEAR;
                        break;
                    case 2:
                        object = Strategy.TWO_BITS_LINEAR;
                        break;
                    case 3:
                        object = Strategy.THREE_BITS_LINEAR;
                        break;
                    default:
                        object = new Configuration.a(this.globalPaletteBitsInMemory, i);
                }

                return (Configuration) object;
            }
        };
    }

    public int entryCount() {
        return this.entryCount;
    }

    public int getIndex(int i, int j, int k) {
        return (j << this.bitsPerAxis | k) << this.bitsPerAxis | i;
    }

    public Registry<T> globalMap() {
        return this.globalMap;
    }

    public DataPaletteGlobal<T> globalPalette() {
        return this.globalPalette;
    }

    protected abstract Configuration getConfigurationForBitCount(int i);

    protected Configuration getConfigurationForPaletteSize(int i) {
        int j = minimumBitsRequiredForDistinctValues(i);

        return this.getConfigurationForBitCount(j);
    }

    private static int minimumBitsRequiredForDistinctValues(int i) {
        return MathHelper.ceillog2(i);
    }
}
