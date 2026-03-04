package com.game;

import com.game.config.ServerConfig;
import com.game.db.OrmContext;
import com.game.db.model.config.CacheStrategy;
import com.game.db.model.config.HostConfig;
import com.game.db.model.config.OrmConfig;
import com.game.db.model.config.PersisterStrategy;
import com.game.event.EventManager;
import com.game.handler.MessageHandlerManager;
import com.game.net.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;

/**
 * 游戏服务器启动类
 *
 * @author Harleysama
 */
@Slf4j
public class GameServer {

    /**
     * 处理器扫描包路径
     */
    private static final String HANDLER_PACKAGE = "com.game.handler";

    /**
     * 实体类扫描包路径
     */
    private static final String ENTITY_PACKAGE = "com.game.model";

    /**
     * 事件监听器扫描包路径
     */
    private static final String LISTENER_PACKAGE = "com.game.listener";

    public static void main(String[] args) {
        log.info("========================================");
        log.info("  游戏服务器启动中...");
        log.info("  by Harleysama (￣▽￣)ﾉ");
        log.info("========================================");

        try {
            // 加载配置 (优先从 YAML 读取，环境变量可覆盖)
            ServerConfig config = ServerConfig.load();
            log.info("配置加载成功: port={}, mongo={}, workerThreads={}, heartbeatTimeout={}",
                    config.getPort(), config.getMongoConnectionString(),
                    config.getWorkerThreads(), config.getHeartbeatTimeout());

            // 初始化 ORM 框架
            initOrmFramework(config);

            // 注册消息处理器（扫描方式）
            registerHandlers();

            // 注册事件监听器（扫描方式）
            registerEventListeners();

            // 启动 Netty 服务器
            NettyServer server = new NettyServer(config);
            server.start();

        } catch (Exception e) {
            log.error("服务器启动失败! (T_T)", e);
            System.exit(1);
        }
    }

    /**
     * 注册所有消息处理器
     * 使用注解扫描方式自动注册
     */
    private static void registerHandlers() {
        MessageHandlerManager manager = MessageHandlerManager.getInstance();

        // 自动扫描并注册所有带 @GameHandler 注解的处理器
        manager.scanAndRegister(HANDLER_PACKAGE);

        log.info("消息处理器注册完成! 共 {} 个", manager.getHandlerCount());
    }

    /**
     * 注册所有事件监听器
     * 使用注解扫描方式自动注册
     */
    private static void registerEventListeners() {
        EventManager manager = EventManager.getInstance();

        // 自动扫描并注册所有带 @Subscribe 注解的监听器方法
        manager.scanAndRegister(LISTENER_PACKAGE);

        log.info("事件监听器注册完成! 共 {} 个监听器方法", manager.getTotalListenerCount());
    }

    /**
     * 初始化 ORM 框架
     * 从 ServerConfig 构建 OrmConfig 并初始化 OrmContext
     *
     * @param config 服务器配置
     */
    private static void initOrmFramework(ServerConfig config) {
        log.info("初始化 ORM 框架...");

        // 构建数据库主机配置（支持复制集，分号分隔多个地址）
        HashMap<String, String> addressMap = extractMongoAddresses(config.getMongoConnectionString());
        HostConfig hostConfig = HostConfig.valueOf(
                config.getDatabaseName(),
                "",     // MongoDB 连接字符串中已包含认证信息
                "",     // 暂不使用用户名密码
                addressMap
        );

        // 构建缓存策略（默认：缓存大小1000，过期时间10分钟）
        CacheStrategy cacheStrategy = new CacheStrategy("default", 1000, 10 * 60 * 1000L);

        // 构建持久化策略（默认：每5分钟持久化一次）
        PersisterStrategy persisterStrategy = new PersisterStrategy("default", "TIME", "300000");

        // 构建 ORM 配置
        OrmConfig ormConfig = new OrmConfig();
        ormConfig.setId("game-orm");
        ormConfig.setEntityPackage(ENTITY_PACKAGE);
        ormConfig.setHost(hostConfig);
        ormConfig.setCaches(Collections.singletonList(cacheStrategy));
        ormConfig.setPersisters(Collections.singletonList(persisterStrategy));

        // 初始化 OrmContext
        OrmContext.getOrmContext().init(ormConfig);

        // 注册关闭钩子，确保优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("检测到关闭信号，开始优雅关闭...");

            // 关闭事件管理器
            log.info("关闭事件管理器...");
            EventManager.getInstance().shutdown();

            // 关闭 ORM 框架
            log.info("关闭 ORM 框架...");
            OrmContext.getOrmContext().shutdown();

            log.info("服务器已关闭，再见！(*￣︶￣)");
        }));

        log.info("ORM 框架初始化完成!");
    }

    /**
     * 从 MongoDB 连接字符串中提取主机地址列表
     * 支持单主机和复制集（分号分隔多个地址）
     * <p>
     * 格式示例：
     * - 单主机: mongodb://localhost:27017
     * - 复制集: mongodb://host1:27017;host2:27017;host3:27017
     * - 带认证: mongodb://user:pass@host1:27017;host2:27017
     *
     * @param connectionString MongoDB 连接字符串
     * @return 主机地址映射（key: host序号, value: host:port 格式）
     */
    private static HashMap<String, String> extractMongoAddresses(String connectionString) {
        HashMap<String, String> addressMap = new HashMap<>();

        try {
            // 移除 mongodb:// 前缀
            String stripped = connectionString.replace("mongodb://", "");

            // 移除认证信息（user:pass@）
            int atIndex = stripped.indexOf('@');
            if (atIndex >= 0) {
                stripped = stripped.substring(atIndex + 1);
            }

            // 提取主机部分（移除数据库和其他参数）
            int slashIndex = stripped.indexOf('/');
            if (slashIndex >= 0) {
                stripped = stripped.substring(0, slashIndex);
            }

            int questionIndex = stripped.indexOf('?');
            if (questionIndex >= 0) {
                stripped = stripped.substring(0, questionIndex);
            }

            // 支持分号或逗号分隔多个地址（复制集）
            String[] addresses = stripped.split("[;,]");

            for (int i = 0; i < addresses.length; i++) {
                String address = addresses[i].trim();
                if (!address.isEmpty()) {
                    addressMap.put("node" + i, address);
                }
            }

            if (addressMap.isEmpty()) {
                log.warn("未能解析出有效的 MongoDB 地址，使用默认地址");
                addressMap.put("default", "localhost:27017");
            } else {
                log.info("解析到 {} 个 MongoDB 节点: {}", addressMap.size(), addressMap.values());
            }

        } catch (Exception e) {
            log.warn("解析 MongoDB 连接字符串失败，使用默认地址: {}", e.getMessage());
            addressMap.put("default", "localhost:27017");
        }

        return addressMap;
    }
}
