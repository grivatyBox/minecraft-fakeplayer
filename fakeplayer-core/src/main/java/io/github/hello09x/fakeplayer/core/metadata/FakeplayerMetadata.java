package io.github.hello09x.fakeplayer.core.metadata;

import lombok.Data;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 假人元数据
 * <p>
 * 用于持久化假人的完整状态，包括基本信息和动作配置。
 * 服务器重启后可以据此恢复假人。
 * </p>
 */
@Data
public class FakeplayerMetadata {

    private int version = 1;
    private UUID uuid;
    private String sequenceName;
    private UUID creatorUuid;
    private String creatorName;
    private long createdAt;
    private SpawnLocation spawnedAt;
    private SpawnOptions options;
    private List<ActionMetadata> actions = new ArrayList<>();

    /**
     * 生成位置信息
     */
    @Data
    public static class SpawnLocation {
        private String world;
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public SpawnLocation() {}

        public SpawnLocation(Location location) {
            this.world = location.getWorld().getName();
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.yaw = location.getYaw();
            this.pitch = location.getPitch();
        }
    }

    /**
     * 生成选项
     */
    @Data
    public static class SpawnOptions {
        private boolean invulnerable;
        private boolean collidable;
        private boolean pickupItems;
    }
}
