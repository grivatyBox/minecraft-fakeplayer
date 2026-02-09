package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.network.PacketDataSerializer;

public interface DataPalette<T> {

    int idFor(T t0, DataPaletteExpandable<T> datapaletteexpandable);

    boolean maybeHas(Predicate<T> predicate);

    T valueFor(int i);

    void read(PacketDataSerializer packetdataserializer, Registry<T> registry);

    void write(PacketDataSerializer packetdataserializer, Registry<T> registry);

    int getSerializedSize(Registry<T> registry);

    int getSize();

    DataPalette<T> copy();

    public interface a {

        <A> DataPalette<A> create(int i, List<A> list);
    }
}
