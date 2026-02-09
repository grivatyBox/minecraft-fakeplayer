package net.minecraft.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PersistentCommandStorage {

    private static final String ID_PREFIX = "command_storage_";
    private final Map<String, PersistentCommandStorage.a> namespaces = new HashMap();
    private final WorldPersistentData storage;

    public PersistentCommandStorage(WorldPersistentData worldpersistentdata) {
        this.storage = worldpersistentdata;
    }

    public NBTTagCompound get(MinecraftKey minecraftkey) {
        PersistentCommandStorage.a persistentcommandstorage_a = this.getContainer(minecraftkey.getNamespace());

        return persistentcommandstorage_a != null ? persistentcommandstorage_a.get(minecraftkey.getPath()) : new NBTTagCompound();
    }

    @Nullable
    private PersistentCommandStorage.a getContainer(String s) {
        PersistentCommandStorage.a persistentcommandstorage_a = (PersistentCommandStorage.a) this.namespaces.get(s);

        if (persistentcommandstorage_a != null) {
            return persistentcommandstorage_a;
        } else {
            PersistentCommandStorage.a persistentcommandstorage_a1 = (PersistentCommandStorage.a) this.storage.get(PersistentCommandStorage.a.type(s));

            if (persistentcommandstorage_a1 != null) {
                this.namespaces.put(s, persistentcommandstorage_a1);
            }

            return persistentcommandstorage_a1;
        }
    }

    private PersistentCommandStorage.a getOrCreateContainer(String s) {
        PersistentCommandStorage.a persistentcommandstorage_a = (PersistentCommandStorage.a) this.namespaces.get(s);

        if (persistentcommandstorage_a != null) {
            return persistentcommandstorage_a;
        } else {
            PersistentCommandStorage.a persistentcommandstorage_a1 = (PersistentCommandStorage.a) this.storage.computeIfAbsent(PersistentCommandStorage.a.type(s));

            this.namespaces.put(s, persistentcommandstorage_a1);
            return persistentcommandstorage_a1;
        }
    }

    public void set(MinecraftKey minecraftkey, NBTTagCompound nbttagcompound) {
        this.getOrCreateContainer(minecraftkey.getNamespace()).put(minecraftkey.getPath(), nbttagcompound);
    }

    public Stream<MinecraftKey> keys() {
        return this.namespaces.entrySet().stream().flatMap((entry) -> {
            return ((PersistentCommandStorage.a) entry.getValue()).getKeys((String) entry.getKey());
        });
    }

    static String createId(String s) {
        return "command_storage_" + s;
    }

    private static class a extends PersistentBase {

        public static final Codec<PersistentCommandStorage.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.unboundedMap(ExtraCodecs.RESOURCE_PATH_CODEC, NBTTagCompound.CODEC).fieldOf("contents").forGetter((persistentcommandstorage_a) -> {
                return persistentcommandstorage_a.storage;
            })).apply(instance, PersistentCommandStorage.a::new);
        });
        private final Map<String, NBTTagCompound> storage;

        private a(Map<String, NBTTagCompound> map) {
            this.storage = new HashMap(map);
        }

        private a() {
            this(new HashMap());
        }

        public static SavedDataType<PersistentCommandStorage.a> type(String s) {
            return new SavedDataType<PersistentCommandStorage.a>(PersistentCommandStorage.createId(s), PersistentCommandStorage.a::new, PersistentCommandStorage.a.CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
        }

        public NBTTagCompound get(String s) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) this.storage.get(s);

            return nbttagcompound != null ? nbttagcompound : new NBTTagCompound();
        }

        public void put(String s, NBTTagCompound nbttagcompound) {
            if (nbttagcompound.isEmpty()) {
                this.storage.remove(s);
            } else {
                this.storage.put(s, nbttagcompound);
            }

            this.setDirty();
        }

        public Stream<MinecraftKey> getKeys(String s) {
            return this.storage.keySet().stream().map((s1) -> {
                return MinecraftKey.fromNamespaceAndPath(s, s1);
            });
        }
    }
}
