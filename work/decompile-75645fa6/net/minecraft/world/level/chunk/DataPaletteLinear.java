package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.VarInt;
import org.apache.commons.lang3.Validate;

public class DataPaletteLinear<T> implements DataPalette<T> {

    private final T[] values;
    private final int bits;
    private int size;

    private DataPaletteLinear(int i, List<T> list) {
        this.values = (T[]) (new Object[1 << i]);
        this.bits = i;
        Validate.isTrue(list.size() <= this.values.length, "Can't initialize LinearPalette of size %d with %d entries", new Object[]{this.values.length, list.size()});

        for (int j = 0; j < list.size(); ++j) {
            this.values[j] = list.get(j);
        }

        this.size = list.size();
    }

    private DataPaletteLinear(T[] at, int i, int j) {
        this.values = at;
        this.bits = i;
        this.size = j;
    }

    public static <A> DataPalette<A> create(int i, List<A> list) {
        return new DataPaletteLinear<A>(i, list);
    }

    @Override
    public int idFor(T t0, DataPaletteExpandable<T> datapaletteexpandable) {
        for (int i = 0; i < this.size; ++i) {
            if (this.values[i] == t0) {
                return i;
            }
        }

        int j = this.size;

        if (j < this.values.length) {
            this.values[j] = t0;
            ++this.size;
            return j;
        } else {
            return datapaletteexpandable.onResize(this.bits + 1, t0);
        }
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        for (int i = 0; i < this.size; ++i) {
            if (predicate.test(this.values[i])) {
                return true;
            }
        }

        return false;
    }

    @Override
    public T valueFor(int i) {
        if (i >= 0 && i < this.size) {
            return (T) this.values[i];
        } else {
            throw new MissingPaletteEntryException(i);
        }
    }

    @Override
    public void read(PacketDataSerializer packetdataserializer, Registry<T> registry) {
        this.size = packetdataserializer.readVarInt();

        for (int i = 0; i < this.size; ++i) {
            this.values[i] = registry.byIdOrThrow(packetdataserializer.readVarInt());
        }

    }

    @Override
    public void write(PacketDataSerializer packetdataserializer, Registry<T> registry) {
        packetdataserializer.writeVarInt(this.size);

        for (int i = 0; i < this.size; ++i) {
            packetdataserializer.writeVarInt(registry.getId(this.values[i]));
        }

    }

    @Override
    public int getSerializedSize(Registry<T> registry) {
        int i = VarInt.getByteSize(this.getSize());

        for (int j = 0; j < this.getSize(); ++j) {
            i += VarInt.getByteSize(registry.getId(this.values[j]));
        }

        return i;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public DataPalette<T> copy() {
        return new DataPaletteLinear<T>(this.values.clone(), this.bits, this.size);
    }
}
