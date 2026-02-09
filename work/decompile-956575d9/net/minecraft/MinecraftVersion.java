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
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class MinecraftVersion implements WorldVersion {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final WorldVersion BUILT_IN = new MinecraftVersion();
    private final String id;
    private final String name;
    private final boolean stable;
    private final DataVersion worldVersion;
    private final int protocolVersion;
    private final int resourcePackVersion;
    private final int dataPackVersion;
    private final Date buildTime;

    private MinecraftVersion() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.name = "1.21.5";
        this.stable = true;
        this.worldVersion = new DataVersion(4325, "main");
        this.protocolVersion = SharedConstants.getProtocolVersion();
        this.resourcePackVersion = 55;
        this.dataPackVersion = 71;
        this.buildTime = new Date();
    }

    private MinecraftVersion(JsonObject jsonobject) {
        this.id = ChatDeserializer.getAsString(jsonobject, "id");
        this.name = ChatDeserializer.getAsString(jsonobject, "name");
        this.stable = ChatDeserializer.getAsBoolean(jsonobject, "stable");
        this.worldVersion = new DataVersion(ChatDeserializer.getAsInt(jsonobject, "world_version"), ChatDeserializer.getAsString(jsonobject, "series_id", DataVersion.MAIN_SERIES));
        this.protocolVersion = ChatDeserializer.getAsInt(jsonobject, "protocol_version");
        JsonObject jsonobject1 = ChatDeserializer.getAsJsonObject(jsonobject, "pack_version");

        this.resourcePackVersion = ChatDeserializer.getAsInt(jsonobject1, "resource");
        this.dataPackVersion = ChatDeserializer.getAsInt(jsonobject1, "data");
        this.buildTime = Date.from(ZonedDateTime.parse(ChatDeserializer.getAsString(jsonobject, "build_time")).toInstant());
    }

    public static WorldVersion tryDetectVersion() {
        try {
            MinecraftVersion minecraftversion;

            try (InputStream inputstream = MinecraftVersion.class.getResourceAsStream("/version.json")) {
                if (inputstream == null) {
                    MinecraftVersion.LOGGER.warn("Missing version information!");
                    WorldVersion worldversion = MinecraftVersion.BUILT_IN;

                    return worldversion;
                }

                try (InputStreamReader inputstreamreader = new InputStreamReader(inputstream)) {
                    minecraftversion = new MinecraftVersion(ChatDeserializer.parse((Reader) inputstreamreader));
                }
            }

            return minecraftversion;
        } catch (JsonParseException | IOException ioexception) {
            throw new IllegalStateException("Game version information is corrupt", ioexception);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public DataVersion getDataVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion(EnumResourcePackType enumresourcepacktype) {
        return enumresourcepacktype == EnumResourcePackType.SERVER_DATA ? this.dataPackVersion : this.resourcePackVersion;
    }

    @Override
    public Date getBuildTime() {
        return this.buildTime;
    }

    @Override
    public boolean isStable() {
        return this.stable;
    }
}
