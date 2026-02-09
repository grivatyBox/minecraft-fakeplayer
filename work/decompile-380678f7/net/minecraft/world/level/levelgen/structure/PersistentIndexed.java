package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PersistentIndexed extends PersistentBase {

    private final LongSet all;
    private final LongSet remaining;
    private static final Codec<LongSet> LONG_SET = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);
    public static final Codec<PersistentIndexed> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(PersistentIndexed.LONG_SET.fieldOf("All").forGetter((persistentindexed) -> {
            return persistentindexed.all;
        }), PersistentIndexed.LONG_SET.fieldOf("Remaining").forGetter((persistentindexed) -> {
            return persistentindexed.remaining;
        })).apply(instance, PersistentIndexed::new);
    });

    public static SavedDataType<PersistentIndexed> type(String s) {
        return new SavedDataType<PersistentIndexed>(s, PersistentIndexed::new, PersistentIndexed.CODEC, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
    }

    private PersistentIndexed(LongSet longset, LongSet longset1) {
        this.all = longset;
        this.remaining = longset1;
    }

    public PersistentIndexed() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public void addIndex(long i) {
        this.all.add(i);
        this.remaining.add(i);
        this.setDirty();
    }

    public boolean hasStartIndex(long i) {
        return this.all.contains(i);
    }

    public boolean hasUnhandledIndex(long i) {
        return this.remaining.contains(i);
    }

    public void removeIndex(long i) {
        if (this.remaining.remove(i)) {
            this.setDirty();
        }

    }

    public LongSet getAll() {
        return this.all;
    }
}
