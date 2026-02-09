package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.VarInt;
import org.apache.commons.lang3.Validate;

public class SingleValuePalette<T> implements DataPalette<T> {

    @Nullable
    private T value;

    public SingleValuePalette(List<T> list) {
        if (list.size() > 0) {
            Validate.isTrue(list.size() <= 1, "Can't initialize SingleValuePalette with %d values.", (long) list.size());
            this.value = (T) list.get(0);
        }

    }

    public static <A> DataPalette<A> create(int i, List<A> list) {
        return new SingleValuePalette<A>(list);
    }

    @Override
    public int idFor(T t0, DataPaletteExpandable<T> datapaletteexpandable) {
        if (this.value != null && this.value != t0) {
            return datapaletteexpandable.onResize(1, t0);
        } else {
            this.value = t0;
            return 0;
        }
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            return predicate.test(this.value);
        }
    }

    @Override
    public T valueFor(int i) {
        if (this.value != null && i == 0) {
            return this.value;
        } else {
            throw new IllegalStateException("Missing Palette entry for id " + i + ".");
        }
    }

    @Override
    public void read(PacketDataSerializer packetdataserializer, Registry<T> registry) {
        this.value = registry.byIdOrThrow(packetdataserializer.readVarInt());
    }

    @Override
    public void write(PacketDataSerializer packetdataserializer, Registry<T> registry) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            packetdataserializer.writeVarInt(registry.getId(this.value));
        }
    }

    @Override
    public int getSerializedSize(Registry<T> registry) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            return VarInt.getByteSize(registry.getId(this.value));
        }
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public DataPalette<T> copy() {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        } else {
            return this;
        }
    }
}
