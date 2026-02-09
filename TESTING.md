# 测试与调试指南

本文档说明如何测试和调试 FakePlayer 插件。

## 本地测试服务器搭建

### 1. 准备测试服务器

根据 `fakeplayer-dist/pom.xml` 的配置,项目使用多个本地测试服务器:

```
minecraft-fakeplayer/
├── server-1.20.1/      # Minecraft 1.20.1 测试服务器
├── server-1.20.2/      # Minecraft 1.20.2 测试服务器
├── server-1.20.6/      # Minecraft 1.20.6 测试服务器
├── server-1.21/        # Minecraft 1.21 测试服务器
├── server-1.21.1/      # Minecraft 1.21.1 测试服务器
├── server-1.21.3/      # Minecraft 1.21.3 测试服务器
├── server-1.21.4/      # Minecraft 1.21.4 测试服务器
├── server-1.21.5/      # Minecraft 1.21.5 测试服务器
├── server-1.21.6/      # Minecraft 1.21.6 测试服务器
├── server-1.21.7/      # Minecraft 1.21.7 测试服务器
├── server-1.21.8/      # Minecraft 1.21.8 测试服务器
├── server-1.21.9/      # Minecraft 1.21.9 测试服务器
└── server-1.21.10/     # Minecraft 1.21.10 测试服务器
```

如果这些目录不存在,你需要创建它们并设置测试服务器。

### 2. 下载 Paper/Purpur 服务器 jar

以 1.21.10 为例:

```bash
mkdir server-1.21.10
cd server-1.21.10

# 下载 Paper 服务器
# 访问 https://papermc.io/downloads
# 或者使用 wget/curl
wget https://api.papermc.io/v2/projects/paper/versions/1.21.10/builds/XX/downloads/paper-1.21.10-XX.jar

# 重命名为 server.jar 或 paper.jar
mv paper-1.21.10-XX.jar paper.jar
```

### 3. 初始化服务器

```bash
# 启动服务器以生成配置文件
java -Xms2G -Xmx4G -jar paper.jar

# 服务器启动后需要同意 EULA
echo "eula=true" > eula.txt

# 安装必需的 CommandAPI 插件
mkdir plugins
cd plugins
# 从 https://commandapi.jorel.dev 下载 CommandAPI
```

### 4. 配置服务器属性

编辑 `server.properties`:

```properties
# 允许局域网连接
server-ip=0.0.0.0
server-port=25565

# 开启调试相关选项
debug=false
spawn-protection=0

# 方便测试的设置
gamemode=survival
difficulty=normal
hardcore=false
online-mode=false  # 本地测试可以关闭在线验证
max-players=20
```

## 构建与部署

### 1. 完整构建流程

```bash
# 1. 清理并构建项目
mvn clean package

# 2. 构建完成后,jar 会自动复制到各个测试服务器的 plugins 目录
# 位于: target/fakeplayer-{version}.jar
```

### 2. 快速重新构建(开发时使用)

```bash
# 跳过测试和检查,快速构建
mvn clean package -DskipTests
```

### 3. 仅构建特定模块

```bash
# 只构建 core 模块(修改核心代码时)
cd fakeplayer-core
mvn clean install

# 只构建特定版本模块(修复版本特定 bug 时)
cd fakeplayer-v1_21_10
mvn clean install

# 最后构建 dist 模块
cd fakeplayer-dist
mvn clean package
```

## 调试配置

### 方法 1: 远程调试(推荐)

#### 启动服务器时开启调试端口

创建启动脚本 `start-debug.bat` (Windows) 或 `start-debug.sh` (Linux/Mac):

**Windows:**
```batch
@echo off
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Xms2G -Xmx4G -jar paper.jar nogui
pause
```

**Linux/Mac:**
```bash
#!/bin/bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Xms2G -Xmx4G -jar paper.jar nogui
```

#### 在 IDE 中配置远程调试

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. 点击左上角的 +,选择 Remote JVM Debug
3. 配置:
   - Name: `FakePlayer Debug (1.21.10)`
   - Host: `localhost`
   - Port: `5005`
4. 点击 OK
5. 启动服务器后,点击 Debug 按钮连接

**VS Code:**
创建 `.vscode/launch.json`:
```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Attach to Remote Server",
            "request": "attach",
            "hostName": "localhost",
            "port": 5005
        }
    ]
}
```

**Eclipse:**
1. Run → Debug Configurations
2. Remote Java Application → New
3. Project: 选择 `fakeplayer-core`
4. Host: `localhost`
5. Port: `5005`

### 方法 2: 热部署(Hot Swap)

使用 Spring Boot DevTools 或 JRebel 实现代码热加载:

```bash
# 普通热部署(仅限方法内部修改)
# IDE 中修改代码后,使用 Ctrl+F9 (IDEA) 重新编译
# 大多数 JVM 会自动热部署方法内部修改
```

### 方法 3: 日志调试

在代码中添加日志:

```java
import java.util.logging.Logger;

public class YourClass {
    private static final Logger log = Main.getInstance().getLogger();

    public void someMethod() {
        log.info("调试信息: someMethod 被调用");
        log.warning("警告信息");
        log.severe("错误信息");
    }
}
```

## 常用调试命令

### 在服务器中测试

```bash
# 启动服务器
./start.sh 或 start.bat

# 在游戏中测试命令
/fp spawn                   # 创建假人
/fp list                    # 查看假人列表
/fp select <name>           # 选择假人
/fp attack                  # 让假人攻击
/fp status                  # 查看假人状态
/fp kill                    # 移除假人

# 查看日志
tail -f logs/latest.log     # Linux/Mac
Get-Content logs/latest.log -Wait  # Windows PowerShell
```

### 开发者命令

```bash
# 重载配置
/fp reload

# 重载翻译
/fp reload-translation

# 查看插件版本
/version fakeplayer

# 查看插件信息
/plugins
```

## 调试技巧

### 1. 使用调试断点

在关键位置设置断点:
- `FakeplayerManager.spawn()`: 假人创建流程
- `ActionManager`: 假人行为控制
- `NMSBridge` 实现: NMS 调用

### 2. 查看服务器日志

```bash
# 实时查看日志
tail -f logs/latest.log

# 过滤特定关键词
grep "fakeplayer" logs/latest.log
grep "ERROR" logs/latest.log
```

### 3. 使用 Minecraft 调试客户端

某些客户端模组可以帮助调试:
- Carpet Mod (展示碰撞箱、tick 信息等)
- Spark (性能分析)

### 4. 网络包调试

如果涉及网络问题,可以使用:
```yaml
# config.yml
debug: true  # 启用详细日志
```

## 性能分析

### 使用 Spark 插件

```bash
# 1. 安装 Spark 插件到服务器
# 2. 在游戏中执行
/spark tps
/spark profiler --timeout 30
```

### 使用 VisualVM

```bash
# 1. 启动服务器时添加 JMX 参数
java -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -Xms2G -Xmx4G -jar paper.jar

# 2. 使用 jvisualvm 或 jconsole 连接 localhost:9010
```

## 常见问题排查

### 问题 1: 假人无法创建

检查:
1. 服务器版本是否支持
2. CommandAPI 是否正确安装
3. 权限是否正确配置
4. 查看日志中的错误信息

### 问题 2: NMS 相关错误

检查:
1. NMS 版本是否匹配
2. 是否正确安装了 remapped-mojang 依赖
3. 查看是否有版本适配问题

### 问题 3: 假人行为异常

检查:
1. ActionManager 是否正确注册
2. ActionTicker 是否正常运行
3. 配置文件中的特性设置

### 问题 4: 调试器无法连接

检查:
1. 服务器启动时是否添加了 `-agentlib:jdwp` 参数
2. 防火墙是否阻止了连接
3. 端口 5005 是否被占用

## 测试检查清单

在发布新版本前,确保测试:

- [ ] 假人创建和移除
- [ ] 假人基本行为(移动、跳跃、攻击、挖掘)
- [ ] 背包查看和编辑
- [ ] 皮肤复制
- [ ] 自动功能(钓鱼、拾取、补给)
- [ ] 多玩家多假人场景
- [ ] 配置持久化
- [ ] 跨版本兼容性
- [ ] 服务器重启后假人恢复
- [ ] 性能和内存使用
