package io.github.hello09x.fakeplayer.core.metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.hello09x.fakeplayer.core.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 假人元数据存储
 * <p>
 * 负责读写 active-fakeplayers.json 文件，管理假人元数据的持久化。
 * </p>
 */
public class FakeplayerMetadataStore {

    private final static Logger log = Main.getInstance().getLogger();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File metadataFile;
    private final File backupFile;
    private final File dataFolder;

    private static final int MAX_BACKUPS = 3;

    public FakeplayerMetadataStore(@NotNull File dataFolder) {
        this.dataFolder = dataFolder;
        this.metadataFile = new File(dataFolder, "active-fakeplayers.json");
        this.backupFile = new File(dataFolder, "active-fakeplayers.json.backup");

        // 确保数据目录存在
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * 加载所有假人元数据
     */
    public @NotNull List<FakeplayerMetadata> load() {
        if (!metadataFile.exists()) {
            log.log(Level.INFO, "元数据文件不存在，返回空列表");
            return new ArrayList<>();
        }

        try (BufferedReader reader = Files.newBufferedReader(metadataFile.toPath())) {
            Type listType = new TypeToken<FakeplayerMetadataList>() {}.getType();
            FakeplayerMetadataList container = gson.fromJson(reader, listType);

            if (container == null || container.fakeplayers == null) {
                log.log(Level.WARNING, "元数据文件为空或格式错误");
                return new ArrayList<>();
            }

            // 验证版本
            if (container.version != 1) {
                log.log(Level.WARNING, "不支持的元数据版本: " + container.version);
                return new ArrayList<>();
            }

            return container.fakeplayers;
        } catch (Exception e) {
            log.log(Level.SEVERE, "加载元数据文件失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存所有假人元数据
     */
    public void save(@NotNull List<FakeplayerMetadata> metadataList) {
        try {
            // 创建备份
            createBackup();

            // 写入文件
            try (BufferedWriter writer = Files.newBufferedWriter(metadataFile.toPath())) {
                FakeplayerMetadataList container = new FakeplayerMetadataList();
                container.version = 1;
                container.fakeplayers = metadataList;
                gson.toJson(container, writer);
            }

            log.log(Level.FINE, "元数据已保存: " + metadataList.size() + " 个假人");
        } catch (IOException e) {
            log.log(Level.SEVERE, "保存元数据文件失败", e);
        }
    }

    /**
     * 添加一个假人元数据
     */
    public void add(@NotNull FakeplayerMetadata metadata) {
        List<FakeplayerMetadata> list = load();
        removeByUuid(list, metadata.getUuid()); // 移除旧的（如果存在）
        list.add(metadata);
        save(list);
    }

    /**
     * 移除一个假人元数据
     */
    public void remove(@NotNull UUID uuid) {
        List<FakeplayerMetadata> list = load();
        if (removeByUuid(list, uuid)) {
            save(list);
        }
    }

    /**
     * 检查假人是否存在
     */
    public boolean contains(@NotNull UUID uuid) {
        List<FakeplayerMetadata> list = load();
        return list.stream().anyMatch(m -> m.getUuid().equals(uuid));
    }

    /**
     * 从列表中移除指定 UUID 的假人
     * @return 是否找到并移除
     */
    private boolean removeByUuid(@NotNull List<FakeplayerMetadata> list, @NotNull UUID uuid) {
        return list.removeIf(m -> m.getUuid().equals(uuid));
    }

    /**
     * 创建备份文件
     */
    private void createBackup() {
        if (!metadataFile.exists()) {
            return;
        }

        try {
            // 轮换备份文件
            File backup3 = new File(dataFolder, "active-fakeplayers.json.backup2");
            File backup2 = new File(dataFolder, "active-fakeplayer.json.backup1");
            File backup1 = new File(dataFolder, "active-fakeplayers.json.backup0");

            if (backup3.exists()) {
                Files.deleteIfExists(backup3.toPath());
            }
            if (backup2.exists()) {
                Files.move(backup2.toPath(), backup3.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if (backup1.exists()) {
                Files.move(backup1.toPath(), backup2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Files.copy(metadataFile.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);

            log.log(Level.FINE, "元数据备份已创建");
        } catch (IOException e) {
            log.log(Level.WARNING, "创建元数据备份失败", e);
        }
    }

    /**
     * 尝试从备份恢复
     */
    public boolean restoreFromBackup() {
        if (backupFile.exists()) {
            try {
                Files.copy(backupFile.toPath(), metadataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.log(Level.INFO, "从备份恢复元数据文件成功");
                return true;
            } catch (IOException e) {
                log.log(Level.SEVERE, "从备份恢复元数据文件失败", e);
                return false;
            }
        }
        return false;
    }

    /**
     * 清空元数据
     */
    public void clear() {
        save(new ArrayList<>());
    }

    /**
     * 获取元数据文件
     */
    public @NotNull File getMetadataFile() {
        return metadataFile;
    }

    /**
     * 用于 JSON 反序列化的容器类
     */
    private static class FakeplayerMetadataList {
        int version;
        List<FakeplayerMetadata> fakeplayers;
    }
}
