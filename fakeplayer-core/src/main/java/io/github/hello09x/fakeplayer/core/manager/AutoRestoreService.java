package io.github.hello09x.fakeplayer.core.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import io.github.hello09x.fakeplayer.core.metadata.ActionMetadata;
import io.github.hello09x.fakeplayer.core.metadata.FakeplayerMetadata;
import io.github.hello09x.fakeplayer.core.metadata.FakeplayerMetadataStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * 自动恢复服务
 * <p>
 * 负责在服务器启动后自动恢复之前创建的假人。
 * </p>
 */
@Singleton
public class AutoRestoreService implements Listener {

    private final static Logger log = Main.getInstance().getLogger();

    private final FakeplayerManager fakeplayerManager;
    private final FakeplayerMetadataStore metadataStore;
    private final FakeplayerConfig config;
    private final ActionManager actionManager;

    private boolean restored = false;
    private final Set<UUID> restoredCreators = new HashSet<>();

    @Inject
    public AutoRestoreService(
            FakeplayerManager fakeplayerManager,
            FakeplayerMetadataStore metadataStore,
            FakeplayerConfig config,
            ActionManager actionManager
    ) {
        this.fakeplayerManager = fakeplayerManager;
        this.metadataStore = metadataStore;
        this.config = config;
        this.actionManager = actionManager;

        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

        // 在插件启用时启动自动恢复任务
        if (config.getAutoRestore().isEnabled()) {
            startAutoRestore();
        }
    }

    /**
     * 启动自动恢复任务
     */
    private void startAutoRestore() {
        // 如果设置为首次登录时恢复，则不在启动时恢复
        if (config.getAutoRestore().isRestoreOnFirstJoin()) {
            log.info("自动恢复已启用，将在创建者首次登录时恢复假人");
            return;
        }

        var delay = config.getAutoRestore().getDelaySeconds() * 20L;
        log.info("将在 " + config.getAutoRestore().getDelaySeconds() + " 秒后自动恢复假人");
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!restored) {
                restoreFakeplayers();
                restored = true;
            }
        }, delay);
    }

    /**
     * 当世界加载时触发自动恢复（作为备用触发器）
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
        // WorldLoadEvent 作为备用触发器，不再主动使用
        // 恢复任务在插件启用时就已经启动
    }

    /**
     * 当玩家登录时触发自动恢复（仅当 restore-on-first-join 为 true 时）
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (!config.getAutoRestore().isEnabled()) {
            return;
        }

        if (!config.getAutoRestore().isRestoreOnFirstJoin()) {
            return;
        }

        var player = event.getPlayer();
        var playerId = player.getUniqueId();

        // 检查是否已经恢复过该玩家的假人
        if (restoredCreators.contains(playerId)) {
            return;
        }

        // 标记为已恢复
        restoredCreators.add(playerId);

        // 延迟 1 秒后恢复，确保玩家完全登录
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            restoreCreatorFakeplayers(player);
        }, 20L);
    }

    /**
     * 恢复所有假人
     */
    public void restoreFakeplayers() {
        log.info("开始恢复假人...");

        var metadataList = metadataStore.load();
        if (metadataList.isEmpty()) {
            log.info("没有需要恢复的假人");
            return;
        }

        log.info("找到 " + metadataList.size() + " 个假人需要恢复");

        var maxConcurrent = config.getAutoRestore().getMaxConcurrentRestore();
        var successCount = new AtomicInteger(0);
        var failureCount = new AtomicInteger(0);
        var skippedCount = new AtomicInteger(0);

        // 批处理恢复（异步执行，不阻塞主线程）
        for (int i = 0; i < metadataList.size(); i += maxConcurrent) {
            var batch = metadataList.subList(i, Math.min(i + maxConcurrent, metadataList.size()));
            var batchNumber = i / maxConcurrent + 1;
            var totalBatches = (int) Math.ceil((double) metadataList.size() / maxConcurrent);

            log.fine("恢复第 " + batchNumber + "/" + totalBatches + " 批 (" + batch.size() + " 个假人)");

            for (var metadata : batch) {
                restoreFakeplayer(metadata)
                        .thenAccept(restoreResult -> {
                            if (restoreResult == RestoreResult.SUCCESS) {
                                successCount.incrementAndGet();
                            } else if (restoreResult == RestoreResult.FAILURE) {
                                failureCount.incrementAndGet();
                            } else {
                                skippedCount.incrementAndGet();
                            }
                        })
                        .exceptionally(e -> {
                            log.warning("恢复假人时发生异常: " + e.getMessage());
                            failureCount.incrementAndGet();
                            return null;
                        });
            }

            // 如果不是最后一批，延迟一段时间再处理下一批
            if (i + maxConcurrent < metadataList.size()) {
                try {
                    Thread.sleep(100); // 批次之间的延迟
                } catch (InterruptedException e) {
                    log.warning("恢复过程被中断");
                    break;
                }
            }
        }

        // 延迟输出统计信息
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            log.info(String.format(
                    "假人恢复完成: 成功 %d, 失败 %d, 跳过 %d",
                    successCount.get(),
                    failureCount.get(),
                    skippedCount.get()
            ));
        }, 20L);
    }

    /**
     * 恢复特定创建者的假人
     *
     * @param creator 创建者
     */
    private void restoreCreatorFakeplayers(@NotNull Player creator) {
        var allMetadata = metadataStore.load();
        if (allMetadata.isEmpty()) {
            return;
        }

        // 筛选出属于该创建者的假人
        var creatorMetadata = allMetadata.stream()
                .filter(m -> creator.getUniqueId().equals(m.getCreatorUuid()))
                .toList();

        if (creatorMetadata.isEmpty()) {
            return;
        }

        log.info("开始恢复 " + creator.getName() + " 的 " + creatorMetadata.size() + " 个假人");

        var maxConcurrent = config.getAutoRestore().getMaxConcurrentRestore();
        var successCount = new AtomicInteger(0);
        var failureCount = new AtomicInteger(0);
        var skippedCount = new AtomicInteger(0);

        // 批处理恢复（异步执行，不阻塞主线程）
        for (int i = 0; i < creatorMetadata.size(); i += maxConcurrent) {
            var batch = creatorMetadata.subList(i, Math.min(i + maxConcurrent, creatorMetadata.size()));
            var batchNumber = i / maxConcurrent + 1;
            var totalBatches = (int) Math.ceil((double) creatorMetadata.size() / maxConcurrent);

            log.fine("恢复第 " + batchNumber + "/" + totalBatches + " 批 (" + batch.size() + " 个假人)");

            for (var metadata : batch) {
                restoreFakeplayer(metadata)
                        .thenAccept(restoreResult -> {
                            if (restoreResult == RestoreResult.SUCCESS) {
                                successCount.incrementAndGet();
                            } else if (restoreResult == RestoreResult.FAILURE) {
                                failureCount.incrementAndGet();
                            } else {
                                skippedCount.incrementAndGet();
                            }
                        })
                        .exceptionally(e -> {
                            log.warning("恢复假人时发生异常: " + e.getMessage());
                            failureCount.incrementAndGet();
                            return null;
                        });
            }

            // 如果不是最后一批，延迟一段时间再处理下一批
            if (i + maxConcurrent < creatorMetadata.size()) {
                try {
                    Thread.sleep(100); // 批次之间的延迟
                } catch (InterruptedException e) {
                    log.warning("恢复过程被中断");
                    break;
                }
            }
        }

        // 延迟输出统计信息
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            log.info(String.format(
                    "%s 的假人恢复完成: 成功 %d, 失败 %d, 跳过 %d",
                    creator.getName(),
                    successCount.get(),
                    failureCount.get(),
                    skippedCount.get()
            ));
        }, 20L);
    }

    /**
     * 恢复单个假人
     *
     * @param metadata 假人元数据
     * @return 恢复结果
     */
    private CompletableFuture<RestoreResult> restoreFakeplayer(@NotNull FakeplayerMetadata metadata) {
        // 检查假人是否已经存在
        var existingPlayer = fakeplayerManager.get(metadata.getSequenceName());
        if (existingPlayer != null) {
            log.fine("假人已存在，跳过恢复: " + metadata.getSequenceName());
            return CompletableFuture.completedFuture(RestoreResult.SKIPPED);
        }

        // 获取生成位置
        var location = getSpawnLocation(metadata);
        if (location == null) {
            log.warning("无法找到生成位置，跳过恢复: " + metadata.getSequenceName());
            return CompletableFuture.completedFuture(RestoreResult.SKIPPED);
        }

        // 获取创建者（如果在线）
        var creator = getCreator(metadata);

        // 如果创建者不在线，使用控制台作为创建者
        var actualCreator = creator != null ? creator : Bukkit.getConsoleSender();

        try {
            // 生成假人
            return fakeplayerManager.spawnAsync(
                            actualCreator,
                            metadata.getSequenceName(),
                            location,
                            0  // 无限存活时间
                    )
                    .thenApply(player -> {
                        // 应用生成选项
                        applySpawnOptions(player, metadata);

                        // 恢复动作
                        restoreActions(player, metadata);

                        log.info("成功恢复假人: " + player.getName());
                        return RestoreResult.SUCCESS;
                    })
                    .exceptionally(e -> {
                        log.warning("恢复假人失败: " + metadata.getSequenceName() + ", " + e.getMessage());
                        return RestoreResult.FAILURE;
                    });
        } catch (Exception e) {
            log.warning("恢复假人失败: " + metadata.getSequenceName() + ", " + e.getMessage());
            return CompletableFuture.completedFuture(RestoreResult.FAILURE);
        }
    }

    /**
     * 获取创建者
     */
    private CommandSender getCreator(@NotNull FakeplayerMetadata metadata) {
        if (metadata.getCreatorUuid() == null) {
            return null;
        }

        var player = Bukkit.getPlayer(metadata.getCreatorUuid());
        return player != null && player.isOnline() ? player : null;
    }

    /**
     * 获取生成位置
     */
    private Location getSpawnLocation(@NotNull FakeplayerMetadata metadata) {
        var spawnedAt = metadata.getSpawnedAt();
        if (spawnedAt == null) {
            return null;
        }

        var world = Bukkit.getWorld(spawnedAt.getWorld());
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                spawnedAt.getX(),
                spawnedAt.getY(),
                spawnedAt.getZ(),
                spawnedAt.getYaw(),
                spawnedAt.getPitch()
        );
    }

    /**
     * 应用生成选项
     */
    private void applySpawnOptions(@NotNull Player player, @NotNull FakeplayerMetadata metadata) {
        var options = metadata.getOptions();
        if (options == null) {
            return;
        }

        player.setInvulnerable(options.isInvulnerable());
        player.setCollidable(options.isCollidable());
        player.setCanPickupItems(options.isPickupItems());
    }

    /**
     * 恢复动作
     */
    private void restoreActions(@NotNull Player player, @NotNull FakeplayerMetadata metadata) {
        var actions = metadata.getActions();
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (var actionMeta : actions) {
            try {
                var type = actionMeta.getType();
                if (type == null) {
                    continue;
                }

                var setting = convertActionSetting(actionMeta.getRemains(), actionMeta.getInterval());
                if (setting == null) {
                    continue;
                }

                actionManager.setAction(player, type, setting);
                log.fine("已恢复动作: " + player.getName() + " - " + type.name() +
                        " (remains=" + actionMeta.getRemains() + ", interval=" + actionMeta.getInterval() + ")");
            } catch (Exception e) {
                log.warning("恢复动作失败: " + player.getName() + " - " + e.getMessage());
            }
        }
    }

    /**
     * 转换动作配置
     * remains < 0: 无限次执行（interval 或 continuous 模式）
     * remains = 0: 已停止
     * remains > 0: 有限次数执行
     *
     * @param remains 剩余次数
     * @param interval 执行间隔（ticks）
     */
    private io.github.hello09x.fakeplayer.api.spi.ActionSetting convertActionSetting(int remains, int interval) {
        if (remains == 0) {
            return io.github.hello09x.fakeplayer.api.spi.ActionSetting.stop();
        }

        // 如果有 interval 信息，使用 interval 模式
        if (interval > 0) {
            return io.github.hello09x.fakeplayer.api.spi.ActionSetting.interval(interval);
        }

        // 否则使用 continuous 模式
        return io.github.hello09x.fakeplayer.api.spi.ActionSetting.continuous();
    }

    /**
     * 恢复结果
     */
    private enum RestoreResult {
        SUCCESS,
        FAILURE,
        SKIPPED
    }
}
