package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ChatDeserializer;
import org.slf4j.Logger;

public interface DebugReportProvider {

    ToIntFunction<String> FIXED_ORDER_FIELDS = (ToIntFunction) SystemUtils.make(new Object2IntOpenHashMap(), (object2intopenhashmap) -> {
        object2intopenhashmap.put("type", 0);
        object2intopenhashmap.put("parent", 1);
        object2intopenhashmap.defaultReturnValue(2);
    });
    Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(DebugReportProvider.FIXED_ORDER_FIELDS).thenComparing((s) -> {
        return s;
    });
    Logger LOGGER = LogUtils.getLogger();

    CompletableFuture<?> run(CachedOutput cachedoutput);

    String getName();

    static <T> CompletableFuture<?> saveAll(CachedOutput cachedoutput, Codec<T> codec, PackOutput.a packoutput_a, Map<MinecraftKey, T> map) {
        return CompletableFuture.allOf((CompletableFuture[]) map.entrySet().stream().map((entry) -> {
            return saveStable(cachedoutput, codec, entry.getValue(), packoutput_a.json((MinecraftKey) entry.getKey()));
        }).toArray((i) -> {
            return new CompletableFuture[i];
        }));
    }

    static <T> CompletableFuture<?> saveStable(CachedOutput cachedoutput, HolderLookup.a holderlookup_a, Codec<T> codec, T t0, Path path) {
        RegistryOps<JsonElement> registryops = holderlookup_a.createSerializationContext(JsonOps.INSTANCE);

        return saveStable(cachedoutput, (DynamicOps) registryops, codec, t0, path);
    }

    static <T> CompletableFuture<?> saveStable(CachedOutput cachedoutput, Codec<T> codec, T t0, Path path) {
        return saveStable(cachedoutput, (DynamicOps) JsonOps.INSTANCE, codec, t0, path);
    }

    private static <T> CompletableFuture<?> saveStable(CachedOutput cachedoutput, DynamicOps<JsonElement> dynamicops, Codec<T> codec, T t0, Path path) {
        JsonElement jsonelement = (JsonElement) codec.encodeStart(dynamicops, t0).getOrThrow();

        return saveStable(cachedoutput, jsonelement, path);
    }

    static CompletableFuture<?> saveStable(CachedOutput cachedoutput, JsonElement jsonelement, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
                JsonWriter jsonwriter = new JsonWriter(new OutputStreamWriter(hashingoutputstream, StandardCharsets.UTF_8));

                try {
                    jsonwriter.setSerializeNulls(false);
                    jsonwriter.setIndent("  ");
                    ChatDeserializer.writeValue(jsonwriter, jsonelement, DebugReportProvider.KEY_COMPARATOR);
                } catch (Throwable throwable) {
                    try {
                        jsonwriter.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }

                    throw throwable;
                }

                jsonwriter.close();
                cachedoutput.writeIfNeeded(path, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
            } catch (IOException ioexception) {
                DebugReportProvider.LOGGER.error("Failed to save file to {}", path, ioexception);
            }

        }, SystemUtils.backgroundExecutor().forName("saveStable"));
    }

    @FunctionalInterface
    public interface a<T extends DebugReportProvider> {

        T create(PackOutput packoutput);
    }
}
