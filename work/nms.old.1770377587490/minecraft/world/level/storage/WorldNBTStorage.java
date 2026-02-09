package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.EntityHuman;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.core.IRegistryCustom;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
// CraftBukkit end

public class WorldNBTStorage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;
    private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

    public WorldNBTStorage(Convertable.ConversionSession convertable_conversionsession, DataFixer datafixer) {
        this.fixerUpper = datafixer;
        this.playerDir = convertable_conversionsession.getLevelPath(SavedFile.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(EntityHuman entityhuman) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(entityhuman.problemPath(), WorldNBTStorage.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, entityhuman.registryAccess());

            entityhuman.saveWithoutId(tagvalueoutput);
            Path path = this.playerDir.toPath();
            Path path1 = Files.createTempFile(path, entityhuman.getStringUUID() + "-", ".dat");
            NBTTagCompound nbttagcompound = tagvalueoutput.buildResult();

            NBTCompressedStreamTools.writeCompressed(nbttagcompound, path1);
            Path path2 = path.resolve(entityhuman.getStringUUID() + ".dat");
            Path path3 = path.resolve(entityhuman.getStringUUID() + ".dat_old");

            SystemUtils.safeReplaceFile(path2, path1, path3);
        } catch (Exception exception) {
            WorldNBTStorage.LOGGER.warn("Failed to save player data for {}", entityhuman.getPlainTextName());
        }

    }

    private void backup(NameAndId nameandid, String s) {
        Path path = this.playerDir.toPath();
        String s1 = nameandid.id().toString();
        Path path1 = path.resolve(s1 + s);
        Path path2 = path.resolve(s1 + "_corrupted_" + LocalDateTime.now().format(WorldNBTStorage.FORMATTER) + s);

        if (Files.isRegularFile(path1, new LinkOption[0])) {
            try {
                Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception exception) {
                WorldNBTStorage.LOGGER.warn("Failed to copy the player.dat file for {}", nameandid.name(), exception);
            }

        }
    }

    private Optional<NBTTagCompound> load(NameAndId nameandid, String s) {
        File file = this.playerDir;
        String s1 = nameandid.id().toString();
        File file1 = new File(file, s1 + s);

        if (file1.exists() && file1.isFile()) {
            try {
                return Optional.of(NBTCompressedStreamTools.readCompressed(file1.toPath(), NBTReadLimiter.unlimitedHeap()));
            } catch (Exception exception) {
                WorldNBTStorage.LOGGER.warn("Failed to load player data for {}", nameandid.name());
            }
        }

        return Optional.empty();
    }

    // CraftBukkit start
    public Optional<NBTTagCompound> load(EntityHuman entityhuman) {
        return load(entityhuman.nameAndId()).map((nbttagcompound) -> {
            if (entityhuman instanceof EntityPlayer) {
                CraftPlayer player = (CraftPlayer) entityhuman.getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDir, entityhuman.getStringUUID() + ".dat").lastModified();
                if (modified < player.getFirstPlayed()) {
                    player.setFirstPlayed(modified);
                }
            }

            return nbttagcompound;
        });
    }
    // CraftBukkit end

    public Optional<NBTTagCompound> load(NameAndId nameandid) {
        Optional<NBTTagCompound> optional = this.load(nameandid, ".dat");

        if (optional.isEmpty()) {
            this.backup(nameandid, ".dat");
        }

        return optional.or(() -> {
            return this.load(nameandid, ".dat_old");
        }).map((nbttagcompound) -> {
            int i = GameProfileSerializer.getDataVersion(nbttagcompound, -1);

            nbttagcompound = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, nbttagcompound, i);
            return nbttagcompound;
        });
    }

    // CraftBukkit start
    public File getPlayerDir() {
        return playerDir;
    }
    // CraftBukkit end
}
