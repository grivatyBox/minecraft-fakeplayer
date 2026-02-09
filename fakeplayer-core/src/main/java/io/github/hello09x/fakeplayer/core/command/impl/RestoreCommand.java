package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.command.CommandSupports;
import io.github.hello09x.fakeplayer.core.manager.AutoRestoreService;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.metadata.FakeplayerMetadata;
import io.github.hello09x.fakeplayer.core.metadata.FakeplayerMetadataStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Singleton
public class RestoreCommand extends AbstractCommand {

    private final AutoRestoreService autoRestoreService;
    private final FakeplayerMetadataStore metadataStore;
    private final FakeplayerManager fakeplayerManager;

    @Inject
    public RestoreCommand(
            AutoRestoreService autoRestoreService,
            FakeplayerMetadataStore metadataStore,
            FakeplayerManager fakeplayerManager
    ) {
        this.autoRestoreService = autoRestoreService;
        this.metadataStore = metadataStore;
        this.fakeplayerManager = fakeplayerManager;
    }

    /**
     * 手动触发恢复所有假人
     */
    public void restoreAll(@NotNull CommandSender sender, @NotNull CommandArguments args) {
        var metadataList = metadataStore.load();
        if (metadataList.isEmpty()) {
            sender.sendMessage(translatable(
                    "fakeplayer.command.restore.no-metadata",
                    GRAY
            ));
            return;
        }

        sender.sendMessage(translatable(
                "fakeplayer.command.restore.start",
                WHITE,
                text(String.valueOf(metadataList.size()), GRAY)
        ));

        // 异步恢复
        autoRestoreService.restoreFakeplayers();

        sender.sendMessage(translatable(
                "fakeplayer.command.restore.started",
                GREEN
        ));
    }

    /**
     * 恢复特定假人
     */
    public void restore(@NotNull Player sender, @NotNull CommandArguments args) {
        var selected = fakeplayerManager.getSelection(sender);
        if (selected == null) {
            sender.sendMessage(translatable("fakeplayer.command.error.no-fakeplayer-selected", RED));
            return;
        }

        // 查找匹配的元数据
        var metadataList = metadataStore.load();
        FakeplayerMetadata metadata = null;
        for (var m : metadataList) {
            if (m.getUuid().equals(selected.getUniqueId()) || m.getSequenceName().equals(selected.getName())) {
                metadata = m;
                break;
            }
        }

        if (metadata == null) {
            sender.sendMessage(translatable(
                    "fakeplayer.command.restore.not-found",
                    RED,
                    text(selected.getName(), WHITE)
            ));
            return;
        }

        // 检查假人是否已经在线
        if (selected.isOnline()) {
            sender.sendMessage(translatable(
                    "fakeplayer.command.restore.already-exists",
                    YELLOW,
                    text(selected.getName(), WHITE)
            ));
            return;
        }

        sender.sendMessage(translatable(
                "fakeplayer.command.restore.single.start",
                WHITE,
                text(metadata.getSequenceName(), GRAY)
        ));

        // 异步恢复
        autoRestoreService.restoreFakeplayers();

        sender.sendMessage(translatable(
                "fakeplayer.command.restore.single.started",
                GREEN
        ));
    }

}
