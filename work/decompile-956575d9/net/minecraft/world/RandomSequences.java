package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.level.saveddata.SavedDataType;

public class RandomSequences extends PersistentBase {

    public static final SavedDataType<RandomSequences> TYPE = new SavedDataType<RandomSequences>("random_sequences", (persistentbase_a) -> {
        return new RandomSequences(persistentbase_a.worldSeed());
    }, (persistentbase_a) -> {
        return codec(persistentbase_a.worldSeed());
    }, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    private final long worldSeed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<MinecraftKey, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public RandomSequences(long i) {
        this.worldSeed = i;
    }

    private RandomSequences(long i, int j, boolean flag, boolean flag1, Map<MinecraftKey, RandomSequence> map) {
        this.worldSeed = i;
        this.salt = j;
        this.includeWorldSeed = flag;
        this.includeSequenceId = flag1;
        this.sequences.putAll(map);
    }

    public static Codec<RandomSequences> codec(long i) {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(RecordCodecBuilder.point(i), Codec.INT.fieldOf("salt").forGetter((randomsequences) -> {
                return randomsequences.salt;
            }), Codec.BOOL.optionalFieldOf("include_world_seed", true).forGetter((randomsequences) -> {
                return randomsequences.includeWorldSeed;
            }), Codec.BOOL.optionalFieldOf("include_sequence_id", true).forGetter((randomsequences) -> {
                return randomsequences.includeSequenceId;
            }), Codec.unboundedMap(MinecraftKey.CODEC, RandomSequence.CODEC).fieldOf("sequences").forGetter((randomsequences) -> {
                return randomsequences.sequences;
            })).apply(instance, RandomSequences::new);
        });
    }

    public RandomSource get(MinecraftKey minecraftkey) {
        RandomSource randomsource = ((RandomSequence) this.sequences.computeIfAbsent(minecraftkey, this::createSequence)).random();

        return new RandomSequences.a(randomsource);
    }

    private RandomSequence createSequence(MinecraftKey minecraftkey) {
        return this.createSequence(minecraftkey, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(MinecraftKey minecraftkey, int i, boolean flag, boolean flag1) {
        long j = (flag ? this.worldSeed : 0L) ^ (long) i;

        return new RandomSequence(j, flag1 ? Optional.of(minecraftkey) : Optional.empty());
    }

    public void forAllSequences(BiConsumer<MinecraftKey, RandomSequence> biconsumer) {
        this.sequences.forEach(biconsumer);
    }

    public void setSeedDefaults(int i, boolean flag, boolean flag1) {
        this.salt = i;
        this.includeWorldSeed = flag;
        this.includeSequenceId = flag1;
    }

    public int clear() {
        int i = this.sequences.size();

        this.sequences.clear();
        return i;
    }

    public void reset(MinecraftKey minecraftkey) {
        this.sequences.put(minecraftkey, this.createSequence(minecraftkey));
    }

    public void reset(MinecraftKey minecraftkey, int i, boolean flag, boolean flag1) {
        this.sequences.put(minecraftkey, this.createSequence(minecraftkey, i, flag, flag1));
    }

    private class a implements RandomSource {

        private final RandomSource random;

        a(final RandomSource randomsource) {
            this.random = randomsource;
        }

        @Override
        public RandomSource fork() {
            RandomSequences.this.setDirty();
            return this.random.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return this.random.forkPositional();
        }

        @Override
        public void setSeed(long i) {
            RandomSequences.this.setDirty();
            this.random.setSeed(i);
        }

        @Override
        public int nextInt() {
            RandomSequences.this.setDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int i) {
            RandomSequences.this.setDirty();
            return this.random.nextInt(i);
        }

        @Override
        public long nextLong() {
            RandomSequences.this.setDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequences.this.setDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequences.this.setDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequences.this.setDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof RandomSequences.a) {
                RandomSequences.a randomsequences_a = (RandomSequences.a) object;

                return this.random.equals(randomsequences_a.random);
            } else {
                return false;
            }
        }
    }
}
