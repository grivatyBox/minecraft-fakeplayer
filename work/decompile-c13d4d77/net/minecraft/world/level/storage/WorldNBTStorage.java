package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.EntityHuman;
import org.slf4j.Logger;

public class WorldNBTStorage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;

    public WorldNBTStorage(Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer) {
        this.fixerUpper = datafixer;
        this.playerDir = convertable_conversionsession.getLevelPath(SavedFile.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(EntityHuman entityhuman) {
        try {
            NBTTagCompound nbttagcompound = entityhuman.saveWithoutId(new NBTTagCompound());
            Path path = this.playerDir.toPath();
            Path path1 = Files.createTempFile(path, entityhuman.getStringUUID() + "-", ".dat");

            NBTCompressedStreamTools.writeCompressed(nbttagcompound, path1);
            Path path2 = path.resolve(entityhuman.getStringUUID() + ".dat");
            Path path3 = path.resolve(entityhuman.getStringUUID() + ".dat_old");

            SystemUtils.safeReplaceFile(path2, path1, path3);
        } catch (Exception exception) {
            WorldNBTStorage.LOGGER.warn("Failed to save player data for {}", entityhuman.getName().getString());
        }

    }

    @Nullable
    public NBTTagCompound load(EntityHuman entityhuman) {
        NBTTagCompound nbttagcompound = null;

        try {
            File file = new File(this.playerDir, entityhuman.getStringUUID() + ".dat");

            if (file.exists() && file.isFile()) {
                nbttagcompound = NBTCompressedStreamTools.readCompressed(file.toPath(), NBTReadLimiter.unlimitedHeap());
            }
        } catch (Exception exception) {
            WorldNBTStorage.LOGGER.warn("Failed to load player data for {}", entityhuman.getName().getString());
        }

        if (nbttagcompound != null) {
            int i = GameProfileSerializer.getDataVersion(nbttagcompound, -1);

            nbttagcompound = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, nbttagcompound, i);
            entityhuman.load(nbttagcompound);
        }

        return nbttagcompound;
    }

    public String[] getSeenPlayers() {
        String[] astring = this.playerDir.list();

        if (astring == null) {
            astring = new String[0];
        }

        for (int i = 0; i < astring.length; ++i) {
            if (astring[i].endsWith(".dat")) {
                astring[i] = astring[i].substring(0, astring[i].length() - 4);
            }
        }

        return astring;
    }
}
