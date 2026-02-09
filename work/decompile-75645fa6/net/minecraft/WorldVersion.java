package net.minecraft;

import java.util.Date;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion {

    DataVersion dataVersion();

    String id();

    String name();

    int protocolVersion();

    PackFormat packVersion(EnumResourcePackType enumresourcepacktype);

    Date buildTime();

    boolean stable();

    public static record a(String id, String name, DataVersion dataVersion, int protocolVersion, PackFormat resourcePackVersion, PackFormat datapackVersion, Date buildTime, boolean stable) implements WorldVersion {

        @Override
        public PackFormat packVersion(EnumResourcePackType enumresourcepacktype) {
            PackFormat packformat;

            switch (enumresourcepacktype) {
                case CLIENT_RESOURCES:
                    packformat = this.resourcePackVersion;
                    break;
                case SERVER_DATA:
                    packformat = this.datapackVersion;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return packformat;
        }
    }
}
