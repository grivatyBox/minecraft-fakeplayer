package io.github.hello09x.fakeplayer.core.manager.action;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionTicker;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.metadata.ActionMetadata;
import io.github.hello09x.fakeplayer.core.metadata.FakeplayerMetadataStore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class ActionManager {

    private final static Logger log = Main.getInstance().getLogger();

    private final Map<UUID, Map<ActionType, ActionTicker>> managers = new HashMap<>();

    private final NMSBridge bridge;
    private final FakeplayerMetadataStore metadataStore;


    @Inject
    public ActionManager(NMSBridge bridge, FakeplayerMetadataStore metadataStore) {
        this.bridge = bridge;
        this.metadataStore = metadataStore;
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::tick, 0, 1);
        // 定期保存动作状态（每 5 秒 = 100 ticks）
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this::saveAllActions, 100L, 100L);
    }

    public boolean hasActiveAction(
            @NotNull Player player,
            @NotNull ActionType action
    ) {
        return Optional.ofNullable(this.managers.get(player.getUniqueId()))
                       .map(manager -> manager.get(action))
                       .filter(ac -> ac.getSetting().remains > 0)
                       .isPresent();
    }

    public @NotNull @Unmodifiable Set<ActionType> getActiveActions(@NotNull Player player) {
        var manager = this.managers.get(player.getUniqueId());
        if (manager == null || managers.isEmpty()) {
            return Collections.emptySet();
        }

        return manager.entrySet()
                      .stream()
                      .filter(actions -> actions.getValue().getSetting().remains > 0)
                      .map(Map.Entry::getKey)
                      .collect(Collectors.toSet());
    }

    public void setAction(
            @NotNull Player player,
            @NotNull ActionType action,
            @NotNull ActionSetting setting
    ) {
        var managers = this.managers.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>());
        managers.put(action, bridge.createAction(player, action, setting));
    }

    public void stop(@NotNull Player player) {
        var managers = this.managers.get(player.getUniqueId());
        if (managers == null || managers.isEmpty()) {
            return;
        }

        for (var entry : managers.entrySet()) {
            if (!entry.getValue().equals(ActionSetting.stop())) {
                entry.setValue(bridge.createAction(player, entry.getKey(), ActionSetting.stop()));
            }
        }
    }

    public void tick() {
        var itr = managers.entrySet().iterator();
        while (itr.hasNext()) {
            var entry = itr.next();
            var player = Bukkit.getPlayer(entry.getKey());

            if (player == null || !player.isValid()) {
                // 假人下线或者死亡
                itr.remove();
                for (var ticker : entry.getValue().values()) {
                    ticker.stop();
                }
                continue;
            }

            // do tick
            entry.getValue().values().removeIf(ticker -> {
                try {
                    return ticker.tick();
                } catch (Throwable e) {
                    log.warning(Throwables.getStackTraceAsString(e));
                    return false;
                }
            });
            if (entry.getValue().isEmpty()) {
                itr.remove();
            }
        }
    }

    /**
     * 立即保存所有假人的动作状态到元数据
     * 用于假人下线或服务器关闭前确保数据不丢失
     */
    public void saveNow() {
        saveAllActions();
    }

    /**
     * 保存所有假人的动作状态到元数据
     */
    private void saveAllActions() {
        if (managers.isEmpty()) {
            return;
        }

        try {
            var allMetadata = metadataStore.load();
            if (allMetadata.isEmpty()) {
                return;
            }

            boolean updated = false;
            for (var metadata : allMetadata) {
                var player = Bukkit.getPlayer(metadata.getUuid());
                if (player == null || !player.isValid()) {
                    continue;
                }

                var actions = managers.get(metadata.getUuid());
                if (actions == null || actions.isEmpty()) {
                    // 如果没有活跃动作，清空元数据中的动作列表
                    if (!metadata.getActions().isEmpty()) {
                        metadata.getActions().clear();
                        updated = true;
                    }
                    continue;
                }

                // 转换动作状态到元数据格式
                var actionList = new ArrayList<ActionMetadata>();
                for (var entry : actions.entrySet()) {
                    var ticker = entry.getValue();
                    var setting = ticker.getSetting();

                    // 跳过已停止的动作（remains = 0 表示已停止）
                    if (setting.remains == 0) {
                        continue;
                    }

                    var actionMeta = new ActionMetadata();
                    actionMeta.setType(entry.getKey());
                    actionMeta.setRemains(setting.remains);
                    actionMeta.setInterval(setting.interval);

                    actionList.add(actionMeta);
                }

                // 检查是否有变化
                if (!actionList.equals(metadata.getActions())) {
                    metadata.setActions(actionList);
                    updated = true;
                }
            }

            if (updated) {
                metadataStore.save(allMetadata);
                log.fine("已保存 " + allMetadata.size() + " 个假人的动作状态");
            }
        } catch (Exception e) {
            log.warning("保存动作状态失败: " + e.getMessage());
        }
    }

}
