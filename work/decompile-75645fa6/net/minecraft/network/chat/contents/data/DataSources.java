package net.minecraft.network.chat.contents.data;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;

public class DataSources {

    private static final ExtraCodecs.b<String, MapCodec<? extends DataSource>> ID_MAPPER = new ExtraCodecs.b<String, MapCodec<? extends DataSource>>();
    public static final MapCodec<DataSource> CODEC = ComponentSerialization.<DataSource>createLegacyComponentMatcher(DataSources.ID_MAPPER, DataSource::codec, "source");

    public DataSources() {}

    static {
        DataSources.ID_MAPPER.put("entity", EntityDataSource.MAP_CODEC);
        DataSources.ID_MAPPER.put("block", BlockDataSource.MAP_CODEC);
        DataSources.ID_MAPPER.put("storage", StorageDataSource.MAP_CODEC);
    }
}
