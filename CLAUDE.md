# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 **Java 17 + Netty + MongoDB + Protocol Buffers** 的高性能游戏服务器项目，采用自研 ORM 框架实现数据缓存与持久化。

## 核心架构

### 技术栈
- **网络框架**: Netty 4.1.112 (异步事件驱动)
- **数据库**: MongoDB 5.1.4 (支持复制集)
- **序列化**: Protocol Buffers 3.25.5
- **缓存**: Caffeine 3.1.8
- **构建工具**: Maven
- **日志**: SLF4J + Logback

### 分层架构
```
网络层 (Netty) → 消息处理层 (Handler) → 业务逻辑层 → ORM层 (自研) → MongoDB
```

## 常用命令

### 构建
```bash
# 编译项目
mvn clean compile

# 编译 Protocol Buffers
mvn protobuf:compile

# 打包
mvn package

# 打包可执行 JAR
mvn clean package spring-boot:repackage
```

### 运行
```bash
# 直接运行
mvn exec:java -Dexec.mainClass="com.game.GameServer"

# 运行打包后的 JAR
java -jar target/game-glm-1.0.0.jar
```

### 测试
```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassName

# 跳过测试
mvn package -DskipTests
```

## 关键设计模式

### 1. 自研 ORM 框架

**统一入口**: `OrmContext` 单例模式
```java
// 获取实体缓存
IEntityCaches<Long, User> caches = OrmContext.getEntityCaches(User.class);

// 获取数据访问器
IAccessor accessor = OrmContext.getAccessor();
```

**核心特性**:
- 自动扫描 `com.game.model` 包下的实体类
- 基于 Caffeine 的两级缓存（内存 + MongoDB）
- 支持定时持久化（TIME）和 CRON 持久化策略
- JVM 关闭钩子确保数据安全

### 2. 消息处理器系统

**注解驱动注册**: 使用 `@GameHandler(messageType = X)` 标记处理器
```java
@GameHandler(messageType = CommonProto.MessageType.LOGIN_VALUE)
public class LoginHandler extends AbstractMessageHandler<LoginRequest> {
    @Override
    protected Parser<LoginRequest> getParser() {
        return LoginRequest.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, LoginRequest message) {
        // 业务逻辑
    }
}
```

**自动扫描**: `MessageHandlerManager` 扫描 `com.game.handler` 包自动注册处理器

### 3. 协议编解码

**协议格式**: `[数据长度(4字节)][消息类型(4字节)][Protobuf数据体]`

**编解码器**:
- `PacketFrameDecoder/Encoder`: 帧编解码
- `ProtocolMessageDecoder/Encoder`: Protobuf 消息编解码

## 开发规范

### 添加新的消息处理器

1. 在 `src/main/proto/` 目录下定义 `.proto` 消息格式
2. 运行 `mvn protobuf:compile` 生成 Java 类
3. 在 `com.game.handler` 包下创建处理器类
4. 继承 `AbstractMessageHandler<T>` 并添加 `@GameHandler` 注解

### 添加新的实体类

1. 在 `com.game.model` 包下创建实体类
2. 实现 `IEntity<ID>` 接口
3. 使用 `@Id` 注解标注主键字段
4. ORM 框架会自动扫描并创建缓存策略

### 配置管理

**主配置文件**: `src/main/resources/application.yml`
```yaml
server:
  port: 8888
  worker-threads: 8
  heartbeat-timeout: 60

mongodb:
  connection-string: ${MONGO_URL:mongodb://localhost:27017}
  database: ${MONGO_DATABASE:game_db}
```

**环境变量支持**:
- `GAME_SERVER_PORT`: 服务器端口
- `MONGO_URL`: MongoDB 连接串（支持复制集，用分号分隔多个节点）
- `MONGO_DATABASE`: 数据库名称

## 重要注意事项

### ORM 持久化策略
- 默认配置: 缓存 1000 个对象，10 分钟过期，每 5 分钟持久化一次
- 配置位置: `GameServer.initOrmFramework()` 方法
- 生产环境建议根据业务特点调整缓存大小和持久化频率

### 会话管理
- 使用 `SessionManager` 管理所有客户端连接
- 每个 Session 绑定一个 Channel，支持心跳保活
- 心跳超时默认 60 秒，可在 `application.yml` 中配置

### 线程安全
- Netty EventLoop 线程模型确保 Channel 线程安全
- ORM 缓存层使用 Caffeine，线程安全
- 避免在 Handler 中执行阻塞操作，使用异步模式

### 优雅关闭
- 服务器关闭时会自动触发 ORM 缓存持久化
- 确保所有数据写入 MongoDB 后才关闭连接
- 使用 JVM Shutdown Hook 保证异常退出时的数据安全

## 项目结构关键路径

| 路径 | 用途 |
|------|------|
| `src/main/java/com/game/GameServer.java` | 服务器启动入口 |
| `src/main/java/com/game/handler/` | 消息处理器 |
| `src/main/java/com/game/db/` | ORM 数据访问层 |
| `src/main/java/com/game/model/` | 业务实体类 |
| `src/main/java/com/game/net/` | Netty 网络层 |
| `src/main/proto/` | Protocol Buffers 定义 |
| `src/main/resources/application.yml` | 主配置文件 |
| `src/main/resources/logback.xml` | 日志配置 |

## 代码风格

- 使用 Lombok 简化代码（`@Data`, `@Slf4j`, `@Builder` 等）
- 遵循 SOLID 原则，单一职责，接口隔离
- 优先使用抽象类和接口，便于扩展
- 注释语言保持与现有代码一致（本项目使用中文注释）
- 代码上要加上中文注释

## 工作流程

- 写完代码后,记得维护README.md文件
- 增加新功能后,需要编译一遍,检查是否有报错
