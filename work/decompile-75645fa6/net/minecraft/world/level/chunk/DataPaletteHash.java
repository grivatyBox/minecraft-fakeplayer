package net.minecraft.world.level.chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.VarInt;
import net.minecraft.util.RegistryID;

public class DataPaletteHash<T> implements DataPalette<T> {

    private final RegistryID<T> values;
    private final int bits;

    public DataPaletteHash(int i, List<T> list) {
        this(i);
        RegistryID registryid = this.values;

        Objects.requireNonNull(this.values);
        list.forEach(registryid::add);
    }

    public DataPaletteHash(int i) {
        this(i, RegistryID.create(1 << i));
    }

    private DataPaletteHash(int i, RegistryID<T> registryid) {
        this.bits = i;
        this.values = registryid;
    }

    public static <A> DataPalette<A> create(int i, List<A> list) {
        return new DataPaletteHash<A>(i, list);
    }

    @Override
    public int idFor(T t0, DataPaletteExpandable<T> datapaletteexpandable) {
        int i = this.values.getId(t0);

        if (i == -1) {
            i = this.values.add(t0);
            if (i >= 1 << this.bits) {
                i = datapaletteexpandable.onResize(this.bits + 1, t0);
            }
        }

        return i;
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.getSize(); ++i) {
            if (predicate.test(this.values.byId(i))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public T valueFor(int i) {
        T t0 = (T) this.values.byId(i);

        if (t0 == null) {
            throw new MissingPaletteEntryException(i);
        } else {
            return t0;
        }
    }

    @Override
    public void read(PacketDataSerializer packetdataserializer, Registry<T> registry) {
        this.values.clear();
        int i = packetdataserializer.readVarInt();

        for (int j = 0; j < i; ++j) {
            this.values.add(registry.byIdOrThrow(packetdataserializer.readVarInt()));
        }

    }

    @Override
    public void write(PacketDataSerializer packetdataserializer, Registry<T> registry) {
        int i = this.getSize();

        packetdataserializer.writeVarInt(i);

        for (int j = 0; j < i; ++j) {
            packetdataserializer.writeVarInt(registry.getId(this.values.byId(j)));
        }

    }

    @Override
    public int getSerializedSize(Registry<T> registry) {
        int i = VarInt.getByteSize(this.getSize());

        for (int j = 0; j < this.getSize(); ++j) {
            i += VarInt.getByteSize(registry.getId(this.values.byId(j)));
        }

        return i;
    }

    public List<T> getEntries() {
        ArrayList<T> arraylist = new ArrayList();
        Iterator iterator = this.values.iterator();

        Objects.requireNonNull(arraylist);
        iterator.forEachRemaining(arraylist::add);
        return arraylist;
    }

    @Override
    public int getSize() {
        return this.values.size();
    }

    @Override
    public DataPalette<T> copy() {
        return new DataPaletteHash<T>(this.bits, this.values.copy());
    }
}
