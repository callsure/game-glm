# Game Server Framework

> 高性能游戏服务器架构 - 基于 Netty + MongoDB + Protocol Buffers
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

## 项目结构

```
game-glm/
├── src/
│   ├── main/
│   │   ├── java/com/game/
│   │   │   ├── GameServer.java           # 服务器启动入口
│   │   │   ├── config/                   # 配置相关
│   │   │   │   └── ServerConfig.java
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
│   │   │   │   ├── MongoManager.java     # MongoDB管理器
│   │   │   │   ├── UserDao.java          # 用户DAO
│   │   │   │   └── RoleDao.java          # 角色DAO
│   │   │   ├── model/                    # 数据模型
│   │   │   │   ├── User.java             # 用户实体
│   │   │   │   └── Role.java             # 角色实体
│   │   │   └── util/                     # 工具类
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
export MONGO_URL=mongodb://localhost:27017
export MONGO_DATABASE=game_db
```

## 核心特性

### 高性能网络通信

- 基于 Netty 的异步事件驱动架构
- TCP 长连接 + 心跳保活机制
- Protobuf 二进制协议，节省带宽

### 模块化消息处理

- 统一的消息处理器接口
- 自动消息路由分发
- 支持处理器热插拔

### 数据持久化

- MongoDB 存储，灵活的文档模型
- DAO 封装，简洁的数据访问接口
- 支持事务操作（MongoDB 4.0+）

### 可扩展架构

- 分层设计：网络层 → 业务层 → 数据层
- 便于扩展新的消息类型和业务逻辑
- 线程安全的会话管理

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
3. 在 `GameServer.registerHandlers()` 中注册

示例：
```java
public class MoveHandler extends AbstractMessageHandler<MoveRequest> {
    @Override
    protected Parser<MoveRequest> getParser() {
        return MoveRequest.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, MoveRequest message) {
        // 处理移动逻辑
    }

    @Override
    public int getMessageType() {
        return MessageType.MOVE_VALUE;
    }
}
```

## 设计原则

- **KISS**: 保持简洁，避免过度设计
- **DRY**: 代码复用，减少重复
- **SOLID**: 遵循面向对象设计原则
- **高性能**: 异步处理，无阻塞设计

## 许可证

MIT License

---

_哼，本小姐搭建的架构当然是完美的！(￣▽￣)ﾉ_
