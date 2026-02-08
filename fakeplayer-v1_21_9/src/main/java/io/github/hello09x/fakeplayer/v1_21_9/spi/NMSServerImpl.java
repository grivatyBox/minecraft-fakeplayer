package io.github.hello09x.fakeplayer.v1_21_9.spi;

import com.mojang.authlib.GameProfile;
import io.github.hello09x.devtools.core.utils.WorldUtils;
import io.github.hello09x.fakeplayer.api.spi.NMSServer;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NMSServerImpl implements NMSServer {


    @Getter
    private final MinecraftServer handle;

    public NMSServerImpl(@NotNull Server server) {
        this.handle = ((CraftServer) server).getServer();
    }

    @Override
    public @NotNull NMSServerPlayer newPlayer(@NotNull UUID uuid, @NotNull String name) {
        var server = new NMSServerImpl(Bukkit.getServer()).getHandle();
        var world = new NMSServerLevelImpl(WorldUtils.getMainWorld()).getHandle();
        var gameProfile = new GameProfile(uuid, name);
        var clientInfo = ClientInformation.createDefault();

        var handle = new ServerPlayer(server, world, gameProfile, clientInfo);

        // Paper 1.21.9+ 需要手动加载玩家数据
        server.getPlayerList().playerIo.load(handle.nameAndId()).ifPresent(nbt -> {
            var valueInput = TagValueInput.create(
                    ProblemReporter.DISCARDING,
                    server.registryAccess(),
                    (CompoundTag) nbt
            );
            handle.load(valueInput);
        });

        return new NMSServerPlayerImpl(handle.getBukkitEntity());
    }
}
