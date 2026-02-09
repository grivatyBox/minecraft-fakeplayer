## 背景

**当前状态**：
- 假人数据存储在 `world/playerdata/{uuid}.dat` 文件中（CraftBukkit 标准）
- 服务器关闭时所有假人被移除，但数据文件保留
- 服务器重启后没有机制检测和恢复这些假人
- `FakeplayerManager` 负责假人的生命周期管理（创建、移除等）

**现有机制**：
- `persistent-data: true` 配置使假人数据保存到 playerdata 文件
- 每个假人有唯一的 UUID 和序列名称（如 `userddt_1`, `userddt_2`）
- 假人由 `CommandSender` 创建，有创建者信息

**约束条件**：
- 不能破坏现有 API 和行为
- 必须兼容所有支持的 Minecraft 版本（1.20.1 - 1.21.10）
- 需要处理插件加载顺序问题（在主世界加载后才能恢复）

## 目标 / 非目标

**目标**:
- 服务器启动时自动恢复上次运行期间存在的假人
- 保留假人的完整状态（位置、生命值、物品、行为配置等）
- 提供配置选项控制自动恢复行为
- 优雅处理恢复失败的情况（部分失败不影响其他假人）
- 支持按需禁用或延迟恢复

**非目标**:
- 不恢复已被管理员主动删除的假人
- 不处理跨服务器迁移（BungeeCord 环境需要单独考虑）
- 不实现假人的"死亡恢复"（假人死亡后的重生由现有机制处理）
- 不改变现有假人的创建和管理 API

## 技术决策

### 1. 存储假人列表元数据

**决策**：创建 `plugins/fakeplayer/active-fakeplayers.json` 文件记录活跃假人

**理由**：
- playerdata 文件只包含单个假人数据，无法知道哪些假人是"应该恢复的"
- 需要额外的元数据：创建者 UUID、序列名索引、创建时间等
- JSON 格式易于读写和手动编辑

**替代方案**：
- ~~使用数据库~~：引入额外依赖，过度设计
- ~~扫描 playerdata 目录~~：无法区分正常玩家和假人，无法区分主动删除的假人

**数据结构**：
```json
{
  "version": 1,
  "fakeplayers": [
    {
      "uuid": "xxx-xxx-xxx",
      "sequenceName": "userddt_1",
      "creatorUuid": "yyy-yyy-yyy",
      "creatorName": "userddt",
      "createdAt": 1234567890,
      "spawnedAt": {"world": "world", "y": 64, "x": 100, "z": 200, "yaw": 0, "pitch": 0},
      "options": {
        "invulnerable": false,
        "collidable": true,
        "pickupItems": false
      },
      "actions": [
        {
          "type": "ATTACK",
          "setting": {
            "remains": -1,
            "mode": "AUTO",
            "target": "NEAREST"
          }
        },
        {
          "type": "LOOK_AT_NEAREST_ENTITY",
          "setting": {
            "remains": -1
          }
        }
      ]
    }
  ]
}
```

**动作类型说明**：
- `ATTACK`: 攻击模式（remains: 持续时间，-1 表示无限；mode: AUTO/MANUAL；target: 目标选择）
- `MINE`: 挖掘模式（remains, mode, location）
- `MOVE`: 移动模式（remains, mode, targetLocation）
- `JUMP`: 跳跃模式（remains）
- `USE`: 使用物品模式（remains, mode, offhand）
- `LOOK_AT_NEAREST_ENTITY`: 看向最近实体（remains）
- 其他动作类型...

### 2. 恢复时机

**决策**：在 `WorldLoadEvent` 中延迟恢复，而非插件加载时立即恢复

**理由**：
- 插件加载时主世界可能还未准备好
- 需要等待 `persistent-data` 配置生效
- 避免与其他插件的初始化冲突

**实现**：
```java
@EventHandler(priority = EventPriority.MONITOR)
public void onWorldLoad(WorldLoadEvent event) {
    if (event.getWorld().getEnvironment() != Environment.NORMAL) return;
    // 延迟 1 秒确保世界完全加载
    Bukkit.getScheduler().runTaskLater(plugin, () -> restoreFakeplayers(), 20L);
}
```

### 3. 失败处理策略

**决策**：部分失败不影响其他假人，记录详细日志

**理由**：
- 单个假人恢复失败不应阻止其他假人
- 管理员需要知道哪些假人恢复失败及原因
- 可能的失败原因：玩家重名、权限变更、数据损坏

**实现**：
- try-catch 包裹每个假人的恢复逻辑
- 使用日志记录失败信息（WARN 级别）
- 统计成功/失败数量并在控制台显示

### 4. 配置设计

**决策**：新增配置项控制自动恢复行为

**配置项**：
```yaml
# 自动恢复配置
auto-restore:
  enabled: true                    # 是否启用自动恢复
  delay-seconds: 5                 # 启动后延迟恢复的时间（秒）
  restore-on-first-join: false     # 是否在创建者首次加入时恢复其假人
  max-concurrent-restore: 5        # 同时恢复的最大假人数（避免服务器卡顿）
```

### 5. 版本迁移

**决策**：支持从旧版本平滑升级

**迁移策略**：
- 检测 `active-fakeplayers.json` 不存在时，从 FakeplayerList 中恢复
- 首次运行时扫描 playerdata 目录，识别可能的假人（通过命名模式）
- 迁移完成后备份旧数据

## 风险与权衡

### 风险 1：插件加载顺序问题
**描述**：其他插件可能在假人恢复前尝试访问假人
**缓解**：
- 使用 `EventPriority.MONITOR` 确保在最后执行
- 提供配置项 `delay-seconds` 延迟恢复
- 文档说明可能的兼容性问题

### 风险 2：假人 UUID 冲突
**描述**：如果 UUID 生成逻辑改变，可能导致冲突
**缓解**：
- UUID 生成逻辑保持不变（基于创建者和序号）
- 冲突时跳过该假人并记录警告

### 风险 3：性能影响
**描述**：大量假人同时恢复可能导致服务器卡顿
**缓解**：
- 使用 `max-concurrent-restore` 限制并发数
- 分批恢复，每批间隔 1 tick
- 提供禁用选项

### 风险 4：数据损坏
**描述**：`active-fakeplayers.json` 文件损坏导致无法恢复
**缓解**：
- JSON 格式简单，易于手动修复
- 提供备份机制（保留旧版本文件）
- 损坏时记录错误，不影响服务器运行

## 迁移计划

### 部署步骤

1. **首次部署**：
   - 安装新版本插件
   - 服务器启动，`active-fakeplayers.json` 自动创建
   - 现有假人需要手动重新创建一次（一次性的）

2. **后续启动**：
   - 插件自动读取 `active-fakeplayers.json`
   - 恢复所有记录的假人
   - 管理员无需干预

### 回滚策略

- 保留原 playerdata 文件，回滚后手动重新创建假人即可
- 删除 `active-fakeplayers.json` 禁用自动恢复
- 配置文件向后兼容，无需修改

## 待解决问题

1. **Q**: 是否需要支持部分恢复（如只恢复特定创建者的假人）？
   - **A**: 暂不支持，可以按需添加命令行参数

2. **Q**: 假人死亡后是否应该自动重生？
   - **A**: 不在本次实现范围，由现有机制处理

3. **Q**: 是否需要支持跨世界恢复（假人在世界 A 关闭，在世界 B 恢复）？
   - **A**: 暂不支持，假人在原世界恢复

4. **Q**: 如何处理创建者不再存在的情况（玩家被删除）？
   - **A**: 假人仍会恢复，但创建者信息保留供管理员参考
