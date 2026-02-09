# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Paper/Purpur 服务器的 Minecraft 插件,用于创建和管理假人 (Fake Player)。假人看起来像真实玩家,可以被服务器识别,能保持区块加载,并支持完全的行为控制。

## 构建与开发

### 构建项目

```bash
# 构建前需要先构建 NMS 依赖
java -jar BuildTools.jar --rev 1.21 --remapped

# 然后构建项目
mvn clean package
```

**重要**: Mojang 不允许将重新映射的 NMS jar 发布到公共仓库,因此需要使用 [BuildTools](https://www.spigotmc.org/wiki/buildtools/) 手动构建。根据项目需求,可能需要安装其他版本。

**自动部署**: 构建完成后,jar 文件会自动复制到项目根目录外的测试服务器目录 (`../server-1.XX.X/plugins/fakeplayer.jar`)。详见 [TESTING.md](TESTING.md)。

### 测试与调试

详细的测试和调试指南请参阅 [TESTING.md](TESTING.md),包括:
- 本地测试服务器搭建
- 远程调试配置
- 热部署和日志调试
- 性能分析工具
- 常见问题排查

### 核心依赖

- Java 21
- Maven
- Paper/Purpur 服务器
- [CommandAPI](https://commandapi.jorel.dev) 插件 (除了 10.0.0 以外的任何版本)
- Google Guice (依赖注入)
- Lombok

## 架构设计

### 模块化架构

项目采用多模块 Maven 架构:

- **fakeplayer-api**: API 接口定义,包含 `NMSBridge` 及相关 SPI 接口
- **fakeplayer-core**: 核心功能实现,包含命令、管理器、监听器等
- **fakeplayer-dist**: 分发包,负责将所有模块打包成最终的插件 jar
- **fakeplayer-v1_XX_X**: 各 Minecraft 版本的 NMS 适配模块 (1.20.1 到 1.21.10)

### NMS 桥接机制

项目使用 SPI (ServiceLoader) 机制实现版本适配:

1. API 模块定义 `NMSBridge` 接口 (`fakeplayer-api/src/main/java/io/github/hello09x/fakeplayer/api/spi/NMSBridge.java`)
2. 每个版本模块提供实现类 (如 `NMSBridgeImpl`)
3. 通过 `META-INF/services/io.github.hello09x.fakeplayer.api.spi.NMSBridge` 文件注册所有实现
4. 运行时通过 `ServiceLoader` 动态加载匹配当前服务器版本的实现

关键接口:
- `NMSEntity`: 实体操作
- `NMSServer`: 服务器操作
- `NMSServerLevel`: 世界/维度操作
- `NMSServerPlayer`: 玩家操作
- `NMSNetwork`: 网络连接操作
- `ActionTicker`: 行为控制器 (移动、攻击、挖掘等)

### 依赖注入

使用 Google Guice 进行依赖注入:

- `FakeplayerModule` (fakeplayer-core): 核心模块配置
- `CommandModule` (devtools): 命令注册
- `DatabaseModule` (devtools): 数据库配置
- `TranslationModule` (devtools): 多语言支持

重要: `NMSBridge` 通过 `ServiceLoader` 动态加载,在 `FakeplayerModule` 中通过 `@Provides` 方法提供。

### 核心管理器

位于 `fakeplayer-core/src/main/java/io/github/hello09x/fakeplayer/core/manager/`:

- **FakeplayerManager**: 假人生命周期管理 (创建、移除、重生等)
- **ActionManager**: 假人行为管理 (移动、攻击、挖掘、使用物品等)
- **FakeplayerSkinManager**: 皮肤管理
- **InvseeManager**: 背包查看管理 (支持 OpenInv 集成和简单实现)
- **FakeplayerReplenishManager**: 自动补给管理
- **FakeplayerAutofishManager**: 自动钓鱼管理
- **FakeplayerList**: 假人列表管理
- **WildFakeplayerManager**: 跨服务器假人管理 (BungeeCord 支持)

### 命令系统

位于 `fakeplayer-core/src/main/java/io/github/hello09x/fakeplayer/core/command/`:

使用 CommandAPI 和 devtools-command 框架实现命令注册和执行。

命令分类:
- 基础命令: spawn, kill, list, select 等
- 行动命令: attack, mine, use, jump, move 等
- 传送命令: tp, tphere, tps 等
- 配置命令: config, set 等

## 配置文件

**关键**: 插件只生成模板配置文件 `config.tmpl.yml`,用户需要将其重命名为 `config.yml` 使用。这样可以在升级时预览新内容。

配置文件位置: `fakeplayer-core/src/main/resources/config.yml`

重要配置项:
- `server-limit` / `player-limit`: 假人数量限制
- `name-template` / `name-prefix`: 假人命名规则
- `prevent-kicking`: 防止被其他插件踢出 (NEVER, ON_SPAWNING, ALWAYS)
- `follow-quiting`: 创建者下线时是否跟随下线
- `invsee-implement`: 背包查看实现 (AUTO, SIMPLE)
- 各种生命周期钩子命令 (pre-spawn-commands, post-spawn-commands 等)

## 版本适配流程

当需要支持新的 Minecraft 版本时:

1. 创建新的版本模块 (如 `fakeplayer-v1_22`)
2. 在 `pom.xml` 中添加新模块
3. 继承之前最近的版本模块 (减少重复代码)
4. 实现 `NMSBridge` 接口和所有必需的 NMS 类
5. 在 `fakeplayer-dist/src/main/resources/META-INF/services/io.github.hello09x.fakeplayer.api.spi.NMSBridge` 中注册新实现
6. 更新版本检查逻辑

## 多语言支持

使用 devtools 的 TranslationModule 实现:

- 翻译文件位于 `fakeplayer-core/src/main/resources/message/`
- 支持动态加载和重载
- 可以通过创建 `message_language_region.properties` 文件来自定义翻译

## 扩展集成

### PlaceholderAPI

- 支持 PlaceholderAPI 变量扩展
- 实现类: `FakeplayerPlaceholderExpansionImpl`
- 在 `FakeplayerModule` 中通过条件注入

### OpenInv

- 可选的背包查看增强
- 在 `FakeplayerModule.invseeManager()` 中自动检测并选择实现

## 开发注意事项

1. **版本兼容性**: 修改核心代码时需要考虑所有支持的 Minecraft 版本
2. **配置管理**: 不要直接修改 `config.yml`,修改 `config.tmpl.yml` 模板
3. **NMS 代码**: 涉及 NMS 的修改需要在对应版本模块中进行
4. **依赖注入**: 新的管理器和服务应该通过 Guice 注入
5. **SPI 注册**: 新增版本模块时必须在 META-INF/services 中注册
6. **代码混淆**: 使用 SpecialSource 进行代码混淆映射,确保兼容性
