package net.minecraft.nbt;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface NBTList extends Iterable<NBTBase>, NBTBase permits NBTTagList, NBTTagByteArray, NBTTagIntArray, NBTTagLongArray {

    void clear();

    boolean setTag(int i, NBTBase nbtbase);

    boolean addTag(int i, NBTBase nbtbase);

    NBTBase remove(int i);

    NBTBase get(int i);

    int size();

    default boolean isEmpty() {
        return this.size() == 0;
    }

    default Iterator<NBTBase> iterator() {
        return new Iterator<NBTBase>() {
            private int index;

            public boolean hasNext() {
                return this.index < NBTList.this.size();
            }

            public NBTBase next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    return NBTList.this.get(this.index++);
                }
            }
        };
    }

    default Stream<NBTBase> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
