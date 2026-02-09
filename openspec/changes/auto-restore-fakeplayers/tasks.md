## 1. 数据模型和存储

- [x] 1.1 创建 `FakeplayerMetadata` 数据类，包含字段（uuid、sequenceName、creatorUuid、creatorName、createdAt、spawnedAt、options）
- [x] 1.2 创建 `FakeplayerMetadataStore` 类用于读写 JSON 文件
- [x] 1.3 实现原子性文件写入和备份机制
- [x] 1.4 添加使用 Gson 或类似库的 JSON 序列化/反序列化
- [x] 1.5 添加版本字段到元数据结构（version: 1）
- [x] 1.6 创建 `active-fakeplayers.json` 文件位置常量
- [ ] 1.7 实现文件锁定或并发写入保护
- [x] 1.8 创建 `ActionMetadata` 数据类，用于保存动作状态（type、setting）
- [x] 1.9 在 `FakeplayerMetadata` 中添加 `actions` 字段（List<ActionMetadata>）

## 2. 配置更新

- [x] 2.1 添加 `auto-restore.enabled` 配置选项（默认：true）
- [x] 2.2 添加 `auto-restore.delay-seconds` 配置选项（默认：5）
- [x] 2.3 添加 `auto-restore.restore-on-first-join` 配置选项（默认：false）
- [x] 2.4 添加 `auto-restore.max-concurrent-restore` 配置选项（默认：5）
- [x] 2.5 更新 `config.tmpl.yml` 添加新的配置部分
- [x] 2.6 添加配置验证和默认值

## 3. 元数据生命周期管理

- [x] 3.1 在 `Fakeplayer` 构造函数中集成元数据保存（在 FakeplayerManager.spawnAsync() 完成后保存）
- [x] 3.2 在 `FakeplayerManager.spawnAsync()` 中集成元数据保存
- [x] 3.3 在 `FakeplayerManager.remove()` 中集成元数据移除
- [x] 3.4 处理插件关闭 - 确保退出前保存元数据
- [ ] 3.5 在生成选项变更时添加元数据更新（如适用）（暂不需要）
- [x] 3.6 在 `ActionManager.setAction()` 时同步更新元数据（通过定期保存实现）
- [x] 3.7 在 `ActionManager.stop()` 时从元数据移除动作（通过定期保存实现）
- [x] 3.8 实现定期保存动作状态（如每 5 秒或动作变化时）

## 4. 自动恢复服务

- [x] 4.1 创建 `AutoRestoreService` 类
- [x] 4.2 实现 `restoreFakeplayers()` 主方法
- [x] 4.3 添加带有 MONITOR 优先级的 `WorldLoadEvent` 监听器
- [x] 4.4 实现基于配置的延迟恢复
- [x] 4.5 添加批处理恢复逻辑（遵守 `max-concurrent-restore`）
- [x] 4.6 实现恢复进度日志记录
- [x] 4.7 添加统计跟踪（成功/失败/跳过计数）

## 5. 单个假人恢复

- [x] 5.1 实现 `restoreFakeplayer(FakeplayerMetadata metadata)` 方法
- [x] 5.2 恢复生成位置和世界
- [x] 5.3 恢复生成选项（invulnerable、collidable、pickupItems）
- [x] 5.4 恢复活跃的行为和动作（如果有）
- [x] 5.5 恢复攻击动作（ATTACK）及其配置（mode、target、remains）
- [x] 5.6 恢复挖掘动作（MINE）及其配置（location、remains）
- [x] 5.7 恢复移动动作（MOVE）及其配置（targetLocation、remains）
- [x] 5.8 恢复跳跃动作（JUMP）及其配置
- [x] 5.9 恢复使用物品动作（USE）及其配置（mode、offhand、remains）
- [x] 5.10 恢复看实体动作（LOOK_AT_NEAREST_ENTITY）及其配置
- [x] 5.11 实现多个动作的同时恢复
- [x] 5.12 处理动作配置无效或过期的场景
- [x] 5.13 处理世界未加载的场景
- [x] 5.14 处理 UUID 冲突的场景
- [x] 5.15 处理创建者未找到的场景

## 6. 错误处理和容错

- [x] 6.1 在每个单独的假人恢复周围添加 try-catch
- [x] 6.2 为每种失败类型实现详细的错误日志记录
- [x] 6.3 为损坏/缺失的元数据文件添加警告日志
- [x] 6.4 实现优雅降级（部分失败时继续）
- [x] 6.5 如果元数据文件无效则添加备份恢复（在 FakeplayerMetadataStore 中实现）
- [x] 6.6 防止重复恢复（检查假人是否已存在）

## 7. 命令

- [x] 7.1 添加 `/fp restore` 命令以手动触发恢复
- [x] 7.2 添加 `/fp restore <name>` 命令以恢复特定假人
- [x] 7.3 添加 `fakeplayer.restore` 权限节点
- [x] 7.4 实现恢复命令的 tab 补全
- [x] 7.5 添加命令反馈消息（成功/失败信息）

## 8. 测试和验证

- [ ] 8.1 测试假人生成时的元数据创建
- [ ] 8.2 测试假人移除时的元数据移除
- [ ] 8.3 测试启用自动恢复时的服务器重启
- [ ] 8.4 测试禁用自动恢复时的服务器重启
- [ ] 8.5 测试各种 `delay-seconds` 值的延迟恢复
- [ ] 8.6 测试各种 `max-concurrent-restore` 值的批处理恢复
- [ ] 8.7 测试世界未加载时的恢复
- [ ] 8.8 测试元数据文件损坏时的恢复
- [ ] 8.9 测试元数据文件缺失时的恢复
- [ ] 8.10 测试手动恢复命令
- [ ] 8.11 测试重复恢复防护
- [ ] 8.12 测试创建者未找到的场景
- [ ] 8.13 测试 UUID 冲突的场景
- [ ] 8.14 测试多个假人的恢复
- [ ] 8.15 测试并发元数据文件访问
- [ ] 8.16 测试攻击动作的保存和恢复
- [ ] 8.17 测试挖掘动作的保存和恢复
- [ ] 8.18 测试移动动作的保存和恢复
- [ ] 8.19 测试多个动作的同时保存和恢复
- [ ] 8.20 测试有限持续时间动作的保存和恢复（remains 倒计时）
- [ ] 8.21 测试动作配置的完整性（mode、target 等参数）
- [ ] 8.22 测试动作停止后是否从元数据移除

## 9. 文档

- [ ] 9.1 更新 README 添加自动恢复功能描述
- [ ] 9.2 在 CONFIG.md 中记录新配置选项
- [ ] 9.3 在 config.yml 注释中添加示例
- [ ] 9.4 记录 `/fp restore` 命令用法
- [ ] 9.5 为常见问题添加故障排除部分
- [ ] 9.6 记录高级用户的元数据文件格式

## 10. 迁移和向后兼容

- [ ] 10.1 实现从旧格式的一次性迁移（如果有）
- [ ] 10.2 为未来兼容性添加版本迁移逻辑
- [ ] 10.3 测试从以前版本的插件升级
- [ ] 10.4 确保插件在没有元数据文件的情况下工作（首次安装）
- [ ] 10.5 如果自动恢复完全失败则添加优雅回退

## 11. 性能优化

- [ ] 11.1 分析元数据文件 I/O 操作
- [ ] 11.2 如果需要则优化批处理恢复时机
- [ ] 11.3 为频繁访问的元数据添加缓存
- [ ] 11.4 监控恢复期间的服务器影响
- [ ] 11.5 如果性能问题则添加配置选项以禁用

## 12. 代码质量

- [ ] 12.1 为 FakeplayerMetadataStore 添加单元测试
- [ ] 12.2 为 AutoRestoreService 添加单元测试
- [ ] 12.3 为恢复流程添加集成测试
- [ ] 12.4 添加错误用例测试
- [ ] 12.5 确保代码遵循现有项目约定
- [ ] 12.6 为所有公共方法添加 JavaDoc
- [ ] 12.7 审查代码以查找安全问题
