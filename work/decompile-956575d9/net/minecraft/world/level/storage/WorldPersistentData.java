package net.minecraft.world.level.storage;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

public class WorldPersistentData implements AutoCloseable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final PersistentBase.a context;
    public final Map<SavedDataType<?>, Optional<PersistentBase>> cache = new HashMap();
    private final DataFixer fixerUpper;
    private final HolderLookup.a registries;
    private final Path dataFolder;
    private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture((Object) null);

    public WorldPersistentData(PersistentBase.a persistentbase_a, Path path, DataFixer datafixer, HolderLookup.a holderlookup_a) {
        this.context = persistentbase_a;
        this.fixerUpper = datafixer;
        this.dataFolder = path;
        this.registries = holderlookup_a;
    }

    private Path getDataFile(String s) {
        return this.dataFolder.resolve(s + ".dat");
    }

    public <T extends PersistentBase> T computeIfAbsent(SavedDataType<T> saveddatatype) {
        T t0 = this.get(saveddatatype);

        if (t0 != null) {
            return t0;
        } else {
            T t1 = (T) (saveddatatype.constructor().apply(this.context));

            this.set(saveddatatype, t1);
            return t1;
        }
    }

    @Nullable
    public <T extends PersistentBase> T get(SavedDataType<T> saveddatatype) {
        Optional<PersistentBase> optional = (Optional) this.cache.get(saveddatatype);

        if (optional == null) {
            optional = Optional.ofNullable(this.readSavedData(saveddatatype));
            this.cache.put(saveddatatype, optional);
        }

        return (T) (optional.orElse((Object) null));
    }

    @Nullable
    private <T extends PersistentBase> T readSavedData(SavedDataType<T> saveddatatype) {
        try {
            Path path = this.getDataFile(saveddatatype.id());

            if (Files.exists(path, new LinkOption[0])) {
                NBTTagCompound nbttagcompound = this.readTagFromDisk(saveddatatype.id(), saveddatatype.dataFixType(), SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                RegistryOps<NBTBase> registryops = this.registries.<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

                return (T) (((Codec) saveddatatype.codec().apply(this.context)).parse(registryops, nbttagcompound.get("data")).resultOrPartial((s) -> {
                    WorldPersistentData.LOGGER.error("Failed to parse saved data for '{}': {}", saveddatatype, s);
                }).orElse((Object) null));
            }
        } catch (Exception exception) {
            WorldPersistentData.LOGGER.error("Error loading saved data: {}", saveddatatype, exception);
        }

        return null;
    }

    public <T extends PersistentBase> void set(SavedDataType<T> saveddatatype, T t0) {
        this.cache.put(saveddatatype, Optional.of(t0));
        t0.setDirty();
    }

    public NBTTagCompound readTagFromDisk(String s, DataFixTypes datafixtypes, int i) throws IOException {
        NBTTagCompound nbttagcompound;

        try (InputStream inputstream = Files.newInputStream(this.getDataFile(s)); PushbackInputStream pushbackinputstream = new PushbackInputStream(new FastBufferedInputStream(inputstream), 2);) {
            NBTTagCompound nbttagcompound1;

            if (this.isGzip(pushbackinputstream)) {
                nbttagcompound1 = NBTCompressedStreamTools.readCompressed((InputStream) pushbackinputstream, NBTReadLimiter.unlimitedHeap());
            } else {
                try (DataInputStream datainputstream = new DataInputStream(pushbackinputstream)) {
                    nbttagcompound1 = NBTCompressedStreamTools.read((DataInput) datainputstream);
                }
            }

            int j = GameProfileSerializer.getDataVersion(nbttagcompound1, 1343);

            nbttagcompound = datafixtypes.update(this.fixerUpper, nbttagcompound1, j, i);
        }

        return nbttagcompound;
    }

    private boolean isGzip(PushbackInputStream pushbackinputstream) throws IOException {
        byte[] abyte = new byte[2];
        boolean flag = false;
        int i = pushbackinputstream.read(abyte, 0, 2);

        if (i == 2) {
            int j = (abyte[1] & 255) << 8 | abyte[0] & 255;

            if (j == 35615) {
                flag = true;
            }
        }

        if (i != 0) {
            pushbackinputstream.unread(abyte, 0, i);
        }

        return flag;
    }

    public CompletableFuture<?> scheduleSave() {
        Map<SavedDataType<?>, NBTTagCompound> map = this.collectDirtyTagsToSave();

        if (map.isEmpty()) {
            return CompletableFuture.completedFuture((Object) null);
        } else {
            int i = SystemUtils.maxAllowedExecutorThreads();
            int j = map.size();

            if (j > i) {
                this.pendingWriteFuture = this.pendingWriteFuture.thenCompose((object) -> {
                    List<CompletableFuture<?>> list = new ArrayList(i);
                    int k = MathHelper.positiveCeilDiv(j, i);

                    for (List<Map.Entry<SavedDataType<?>, NBTTagCompound>> list1 : Iterables.partition(map.entrySet(), k)) {
                        list.add(CompletableFuture.runAsync(() -> {
                            for (Map.Entry<SavedDataType<?>, NBTTagCompound> map_entry : list1) {
                                this.tryWrite((SavedDataType) map_entry.getKey(), (NBTTagCompound) map_entry.getValue());
                            }

                        }, SystemUtils.ioPool()));
                    }

                    return CompletableFuture.allOf((CompletableFuture[]) list.toArray((l) -> {
                        return new CompletableFuture[l];
                    }));
                });
            } else {
                this.pendingWriteFuture = this.pendingWriteFuture.thenCompose((object) -> {
                    return CompletableFuture.allOf((CompletableFuture[]) map.entrySet().stream().map((entry) -> {
                        return CompletableFuture.runAsync(() -> {
                            this.tryWrite((SavedDataType) entry.getKey(), (NBTTagCompound) entry.getValue());
                        }, SystemUtils.ioPool());
                    }).toArray((k) -> {
                        return new CompletableFuture[k];
                    }));
                });
            }

            return this.pendingWriteFuture;
        }
    }

    private Map<SavedDataType<?>, NBTTagCompound> collectDirtyTagsToSave() {
        Map<SavedDataType<?>, NBTTagCompound> map = new Object2ObjectArrayMap();
        RegistryOps<NBTBase> registryops = this.registries.<NBTBase>createSerializationContext(DynamicOpsNBT.INSTANCE);

        this.cache.forEach((saveddatatype, optional) -> {
            optional.filter(PersistentBase::isDirty).ifPresent((persistentbase) -> {
                map.put(saveddatatype, this.encodeUnchecked(saveddatatype, persistentbase, registryops));
                persistentbase.setDirty(false);
            });
        });
        return map;
    }

    private <T extends PersistentBase> NBTTagCompound encodeUnchecked(SavedDataType<T> saveddatatype, PersistentBase persistentbase, RegistryOps<NBTBase> registryops) {
        Codec<T> codec = (Codec) saveddatatype.codec().apply(this.context);
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.put("data", (NBTBase) codec.encodeStart(registryops, persistentbase).getOrThrow());
        GameProfileSerializer.addCurrentDataVersion(nbttagcompound);
        return nbttagcompound;
    }

    private void tryWrite(SavedDataType<?> saveddatatype, NBTTagCompound nbttagcompound) {
        Path path = this.getDataFile(saveddatatype.id());

        try {
            NBTCompressedStreamTools.writeCompressed(nbttagcompound, path);
        } catch (IOException ioexception) {
            WorldPersistentData.LOGGER.error("Could not save data to {}", path.getFileName(), ioexception);
        }

    }

    public void saveAndJoin() {
        this.scheduleSave().join();
    }

    public void close() {
        this.saveAndJoin();
    }
}
