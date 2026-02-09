package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.network.PacketDataSerializer;

public class DataPaletteGlobal<T> implements DataPalette<T> {

    private final Registry<T> registry;

    public DataPaletteGlobal(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public int idFor(T t0, DataPaletteExpandable<T> datapaletteexpandable) {
        int i = this.registry.getId(t0);

        return i == -1 ? 0 : i;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        return true;
    }

    @Override
    public T valueFor(int i) {
        T t0 = this.registry.byId(i);

        if (t0 == null) {
            throw new MissingPaletteEntryException(i);
        } else {
            return t0;
        }
    }

    @Override
    public void read(PacketDataSerializer packetdataserializer, Registry<T> registry) {}

    @Override
    public void write(PacketDataSerializer packetdataserializer, Registry<T> registry) {}

    @Override
    public int getSerializedSize(Registry<T> registry) {
        return 0;
    }

    @Override
    public int getSize() {
        return this.registry.size();
    }

    @Override
    public DataPalette<T> copy() {
        return this;
    }
}
