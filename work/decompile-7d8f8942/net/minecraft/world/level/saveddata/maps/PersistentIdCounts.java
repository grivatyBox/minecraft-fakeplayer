package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PersistentIdCounts extends PersistentBase {

    private static final int NO_MAP_ID = -1;
    public static final Codec<PersistentIdCounts> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.INT.optionalFieldOf("map", -1).forGetter((persistentidcounts) -> {
            return persistentidcounts.lastMapId;
        })).apply(instance, PersistentIdCounts::new);
    });
    public static final SavedDataType<PersistentIdCounts> TYPE = new SavedDataType<PersistentIdCounts>("idcounts", PersistentIdCounts::new, PersistentIdCounts.CODEC, DataFixTypes.SAVED_DATA_MAP_INDEX);
    private int lastMapId;

    public PersistentIdCounts() {
        this(-1);
    }

    public PersistentIdCounts(int i) {
        this.lastMapId = i;
    }

    public MapId getNextMapId() {
        MapId mapid = new MapId(++this.lastMapId);

        this.setDirty();
        return mapid;
    }
}
