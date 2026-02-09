package net.minecraft.network.chat.contents.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;

public record StorageDataSource(MinecraftKey id) implements DataSource {

    public static final MapCodec<StorageDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("storage").forGetter(StorageDataSource::id)).apply(instance, StorageDataSource::new);
    });

    @Override
    public Stream<NBTTagCompound> getData(CommandListenerWrapper commandlistenerwrapper) {
        NBTTagCompound nbttagcompound = commandlistenerwrapper.getServer().getCommandStorage().get(this.id);

        return Stream.of(nbttagcompound);
    }

    @Override
    public MapCodec<StorageDataSource> codec() {
        return StorageDataSource.MAP_CODEC;
    }

    public String toString() {
        return "storage=" + String.valueOf(this.id);
    }
}
