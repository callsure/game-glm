# Game Server Framework

> 高性能游戏服务器架构 - 基于 Netty + MongoDB + Protocol Buffers + 自研ORM框架
>
> _by 哈雷酱 (￣▽￣)ﾉ_

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 基础运行环境 |
| Netty | 4.1.112 | 高性能网络通信框架 |
| MongoDB | 5.1.4 | NoSQL 数据库 |
| Protocol Buffers | 3.25.5 | 高效序列化协议 |
| Maven | - | 项目构建工具 |
| Lombok | 1.18.34 | 代码简化工具 |
| SLF4J + Logback | - | 日志框架 |
| 自研 ORM | - | 轻量级对象关系映射框架 |

## 项目结构

```
game-glm/
├── src/
│   ├── main/
│   │   ├── java/com/game/
│   │   │   ├── GameServer.java           # 服务器启动入口
│   │   │   ├── config/                   # 配置相关
│   │   │   │   └── ServerConfig.java     # 服务器配置
│   │   │   ├── net/                      # 网络层
│   │   │   │   ├── NettyServer.java      # Netty服务器
│   │   │   │   ├── GameServerHandler.java # 主处理器
│   │   │   │   ├── Session.java          # 会话对象
│   │   │   │   ├── SessionManager.java   # 会话管理器
│   │   │   │   ├── ProtocolPacket.java   # 协议数据包
│   │   │   │   ├── PacketFrameDecoder.java # 帧解码器
│   │   │   │   ├── PacketFrameEncoder.java # 帧编码器
│   │   │   │   ├── ProtocolMessageDecoder.java # 消息解码器
│   │   │   │   └── ProtocolMessageEncoder.java # 消息编码器
│   │   │   ├── handler/                  # 消息处理器
│   │   │   │   ├── MessageHandler.java   # 处理器接口
│   │   │   │   ├── AbstractMessageHandler.java # 抽象处理器
│   │   │   │   ├── MessageHandlerManager.java # 处理器管理器
│   │   │   │   ├── HeartbeatHandler.java # 心跳处理
│   │   │   │   ├── HandshakeHandler.java # 握手处理
│   │   │   │   └── LoginHandler.java     # 登录处理
│   │   │   ├── db/                       # 数据访问层
│   │   │   │   ├── OrmContext.java       # ORM上下文（统一入口）
│   │   │   │   ├── MongoManager.java     # MongoDB管理器
│   │   │   │   ├── BaseDao.java          # DAO基类
│   │   │   │   ├── dao/                  # DAO实现类
│   │   │   │   │   ├── UserDao.java      # 用户DAO
│   │   │   │   │   └── RoleDao.java      # 角色DAO
│   │   │   │   ├── accessor/             # 数据访问器
│   │   │   │   │   └── MongoAccessor.java # MongoDB访问器
│   │   │   │   ├── cache/                # 缓存系统
│   │   │   │   │   ├── IEntityCaches.java # 缓存接口
│   │   │   │   │   ├── EntityCaches.java  # 缓存实现
│   │   │   │   │   └── persister/        # 持久化器
│   │   │   │   ├── manager/              # ORM管理器
│   │   │   │   │   ├── IOrmManager.java  # 管理器接口
│   │   │   │   │   └── OrmManager.java   # 管理器实现
│   │   │   │   └── model/                # ORM模型
│   │   │   │       ├── anno/             # 注解定义
│   │   │   │       ├── config/           # 配置类
│   │   │   │       ├── entity/           # 实体接口
│   │   │   │       └── vo/               # 值对象
│   │   │   ├── model/                    # 业务数据模型
│   │   │   │   ├── User.java             # 用户实体
│   │   │   │   └── Role.java             # 角色实体
│   │   │   ├── utils/                    # 工具类
│   │   │   ├── protocol/                 # 协议相关
│   │   │   │   └── generated/            # 生成的协议类
│   │   ├── proto/                        # Protocol Buffers 定义
│   │   │   ├── common.proto              # 通用消息定义
│   │   │   ├── auth.proto                # 认证消息定义
│   │   │   └── game.proto                # 游戏逻辑消息定义
│   │   └── resources/
│   │       ├── logback.xml               # 日志配置
│   │       └── application.yml           # 应用配置
│   └── test/
│       └── java/com/game/
└── pom.xml                               # Maven配置文件
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.x
- MongoDB 4.x+

### 启动步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd game-glm
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **编译 Protocol Buffers**
   ```bash
   mvn protobuf:compile
   ```

4. **启动 MongoDB**
   ```bash
   mongod --dbpath /path/to/data
   ```

5. **运行服务器**
   ```bash
   mvn exec:java -Dexec.mainClass="com.game.GameServer"
   ```

### 环境变量配置

```bash
export GAME_SERVER_PORT=8888
export MONGO_DATABASE=game_db

# 单节点 MongoDB
export MONGO_URL=mongodb://localhost:27017

# 复制集 MongoDB（分号分隔多个节点）
export MONGO_URL=mongodb://172.16.0.32:27017;172.16.0.27:27017;172.16.0.30:27017

# 带认证的复制集
export MONGO_URL=mongodb://user:password@172.16.0.32:27017;172.16.0.27:27017;172.16.0.30:27017
```

## 核心特性

### 高性能网络通信

- 基于 Netty 的异步事件驱动架构
- TCP 长连接 + 心跳保活机制
- Protobuf 二进制协议，节省带宽

### 自研 ORM 框架

- **统一入口**: `OrmContext` 单例模式，全局唯一访问点
- **自动扫描**: 自动扫描实体类并创建缓存和持久化策略
- **缓存管理**: 内置 Caffeine 高性能缓存，支持多级缓存策略
- **持久化策略**: 支持定时（CRON）、定间隔（TIME）等多种持久化方式
- **优雅关闭**: JVM 关闭钩子自动触发缓存持久化，确保数据安全

**使用示例：**
```java
// 通过 DAO 访问数据（已集成缓存）
UserDao userDao = new UserDao();
User user = userDao.load(userId);
userDao.update(user);

// 直接访问 ORM 组件
IEntityCaches<Long, User> caches = OrmContext.getEntityCaches(User.class);
IAccessor accessor = OrmContext.getAccessor();
```

### 模块化消息处理

- 统一的消息处理器接口
- 自动消息路由分发
- 支持处理器热插拔
- 注解驱动的处理器注册

### 数据持久化

- MongoDB 存储，灵活的文档模型
- DAO 封装，简洁的数据访问接口
- 自动缓存管理和持久化
- 支持事务操作（MongoDB 4.0+）

### 可扩展架构

- 分层设计：网络层 → 业务层 → 数据层
- 便于扩展新的消息类型和业务逻辑
- 线程安全的会话管理
- 优雅的服务启停流程

## 系统启动流程

```
┌─────────────────────────────────────────────────────────┐
│                    GameServer 启动                       │
└─────────────────────────────────────────────────────────┘
                         │
                         ↓
        ┌──────────────────────────────┐
        │  1. 加载 ServerConfig         │
        │     (YAML + 环境变量)         │
        └──────────────────────────────┘
                         │
                         ↓
        ┌──────────────────────────────┐
        │  2. 初始化 ORM 框架           │
        │     - 构建 OrmConfig          │
        │     - 扫描实体类               │
        │     - 创建缓存策略             │
        │     - 创建持久化策略           │
        │     - 注册关闭钩子             │
        └──────────────────────────────┘
                         │
                         ↓
        ┌──────────────────────────────┐
        │  3. 注册消息处理器            │
        │     (扫描 @GameHandler)       │
        └──────────────────────────────┘
                         │
                         ↓
        ┌──────────────────────────────┐
        │  4. 启动 Netty 服务器         │
        └──────────────────────────────┘
                         │
                         ↓
                    [运行中...]
                         │
         (Ctrl+C / kill) │
                         ↓
        ┌──────────────────────────────┐
        │  关闭钩子触发                 │
        │  - 持久化所有缓存数据         │
        │  - 关闭数据库连接             │
        │  - 释放资源                   │
        └──────────────────────────────┘
```

## 协议格式

```
[数据长度(4字节)][消息类型(4字节)][Protobuf数据体]
```

## 消息类型

| 类型 | 值 | 说明 |
|------|------|------|
| HEARTBEAT | 1 | 心跳包 |
| HANDSHAKE | 2 | 握手请求 |
| LOGIN | 100 | 登录请求 |
| MOVE | 300 | 移动请求 |
| CHAT | 400 | 聊天消息 |

## 开发指南

### 添加新的消息处理器

1. 在 `.proto` 文件中定义消息格式
2. 实现 `AbstractMessageHandler<T>` 接口
3. 添加 `@GameHandler` 注解

示例：
```java
@GameHandler(messageType = CommonProto.MessageType.MOVE_VALUE)
public class MoveHandler extends AbstractMessageHandler<MoveRequest> {
    @Override
    protected Parser<MoveRequest> getParser() {
        return MoveRequest.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, MoveRequest message) {
        // 处理移动逻辑
    }
}
```

### 创建新的 DAO

1. 在 `com.game.db.dao` 包下创建 DAO 类
2. 通过 `OrmContext` 访问缓存和访问器

示例：
```java
package com.game.db.dao;

@Slf4j
public class ItemDao {
    private IEntityCaches<Long, Item> getItemCaches() {
        return (IEntityCaches<Long, Item>) OrmContext.getEntityCaches(Item.class);
    }

    public Item load(Long id) {
        return getItemCaches().load(id);
    }

    public void save(Item item) {
        getItemCaches().addLoad(item);
    }
}
```

### 创建新的实体

1. 在 `com.game.model` 包下创建实体类
2. 添加 `@Entity` 注解标注实体类
3. 使用 `@Id` 注解标注主键字段

示例：
```java
package com.game.model;

@Entity(collection = "items")
public class Item implements IEntity<Long> {
    @Id
    private Long id;

    private String name;
    private int count;

    // getters and setters...
}
```

## ORM 配置

### 默认配置

```java
// 在 GameServer.initOrmFramework() 中设置
缓存策略: 1000个对象, 10分钟过期
持久化策略: 每5分钟持久化一次
实体扫描包: com.game.model
```

### 自定义配置

可以通过修改 `GameServer.java` 中的 `initOrmFramework()` 方法来自定义 ORM 配置。

## 设计原则

- **KISS**: 保持简洁，避免过度设计
- **DRY**: 代码复用，减少重复
- **SOLID**: 遵循面向对象设计原则
- **高性能**: 异步处理，无阻塞设计
- **优雅关闭**: 确保数据完整性

## 许可证

MIT License

---

_哼，本小姐搭建的架构当然是完美的！(￣▽￣)ﾉ_
_自研ORM框架可是本小姐的得意之作呢！(*￣︶￣)_
