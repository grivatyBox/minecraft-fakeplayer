package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class MinecraftVersion {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldVersion BUILT_IN = createBuiltIn(UUID.randomUUID().toString().replaceAll("-", ""), "Development Version");

    public MinecraftVersion() {}

    public static WorldVersion createBuiltIn(String s, String s1) {
        return createBuiltIn(s, s1, true);
    }

    public static WorldVersion createBuiltIn(String s, String s1, boolean flag) {
        return new WorldVersion.a(s, s1, new DataVersion(4556, "main"), SharedConstants.getProtocolVersion(), PackFormat.of(69, 0), PackFormat.of(88, 0), new Date(), flag);
    }

    private static WorldVersion createFromJson(JsonObject jsonobject) {
        JsonObject jsonobject1 = ChatDeserializer.getAsJsonObject(jsonobject, "pack_version");

        return new WorldVersion.a(ChatDeserializer.getAsString(jsonobject, "id"), ChatDeserializer.getAsString(jsonobject, "name"), new DataVersion(ChatDeserializer.getAsInt(jsonobject, "world_version"), ChatDeserializer.getAsString(jsonobject, "series_id", "main")), ChatDeserializer.getAsInt(jsonobject, "protocol_version"), PackFormat.of(ChatDeserializer.getAsInt(jsonobject1, "resource_major"), ChatDeserializer.getAsInt(jsonobject1, "resource_minor")), PackFormat.of(ChatDeserializer.getAsInt(jsonobject1, "data_major"), ChatDeserializer.getAsInt(jsonobject1, "data_minor")), Date.from(ZonedDateTime.parse(ChatDeserializer.getAsString(jsonobject, "build_time")).toInstant()), ChatDeserializer.getAsBoolean(jsonobject, "stable"));
    }

    public static WorldVersion tryDetectVersion() {
        try {
            WorldVersion worldversion;

            try (InputStream inputstream = MinecraftVersion.class.getResourceAsStream("/version.json")) {
                if (inputstream == null) {
                    MinecraftVersion.LOGGER.warn("Missing version information!");
                    WorldVersion worldversion1 = MinecraftVersion.BUILT_IN;

                    return worldversion1;
                }

                try (InputStreamReader inputstreamreader = new InputStreamReader(inputstream)) {
                    worldversion = createFromJson(ChatDeserializer.parse((Reader) inputstreamreader));
                }
            }

            return worldversion;
        } catch (JsonParseException | IOException ioexception) {
            throw new IllegalStateException("Game version information is corrupt", ioexception);
        }
    }
}
