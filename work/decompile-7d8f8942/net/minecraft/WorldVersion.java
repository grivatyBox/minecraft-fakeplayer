package net.minecraft;

import java.util.Date;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion {

    DataVersion dataVersion();

    String id();

    String name();

    int protocolVersion();

    int packVersion(EnumResourcePackType enumresourcepacktype);

    Date buildTime();

    boolean stable();

    public static record a(String id, String name, DataVersion dataVersion, int protocolVersion, int resourcePackVersion, int datapackVersion, Date buildTime, boolean stable) implements WorldVersion {

        @Override
        public int packVersion(EnumResourcePackType enumresourcepacktype) {
            int i;

            switch (enumresourcepacktype) {
                case CLIENT_RESOURCES:
                    i = this.resourcePackVersion;
                    break;
                case SERVER_DATA:
                    i = this.datapackVersion;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return i;
        }
    }
}
