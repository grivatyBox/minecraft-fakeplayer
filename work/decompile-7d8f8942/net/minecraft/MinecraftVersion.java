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
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class MinecraftVersion {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldVersion BUILT_IN = createFromConstants();

    public MinecraftVersion() {}

    private static WorldVersion createFromConstants() {
        return new WorldVersion.a(UUID.randomUUID().toString().replaceAll("-", ""), "1.21.8", new DataVersion(4440, "main"), SharedConstants.getProtocolVersion(), 64, 81, new Date(), true);
    }

    private static WorldVersion createFromJson(JsonObject jsonobject) {
        JsonObject jsonobject1 = ChatDeserializer.getAsJsonObject(jsonobject, "pack_version");

        return new WorldVersion.a(ChatDeserializer.getAsString(jsonobject, "id"), ChatDeserializer.getAsString(jsonobject, "name"), new DataVersion(ChatDeserializer.getAsInt(jsonobject, "world_version"), ChatDeserializer.getAsString(jsonobject, "series_id", "main")), ChatDeserializer.getAsInt(jsonobject, "protocol_version"), ChatDeserializer.getAsInt(jsonobject1, "resource"), ChatDeserializer.getAsInt(jsonobject1, "data"), Date.from(ZonedDateTime.parse(ChatDeserializer.getAsString(jsonobject, "build_time")).toInstant()), ChatDeserializer.getAsBoolean(jsonobject, "stable"));
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
