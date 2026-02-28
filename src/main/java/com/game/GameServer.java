package com.game;

import com.game.config.ServerConfig;
import com.game.db.MongoManager;
import com.game.handler.MessageHandlerManager;
import com.game.net.NettyServer;
import lombok.extern.slf4j.Slf4j;

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

    public static void main(String[] args) {
        log.info("========================================");
        log.info("  游戏服务器启动中...");
        log.info("  by Harleysama (￣▽￣)ﾉ");
        log.info("========================================");

        try {
            // 加载配置
            ServerConfig config = ServerConfig.fromEnv();
            log.info("配置加载成功: port={}, mongo={}", config.getPort(), config.getMongoConnectionString());

            // 初始化 MongoDB
            MongoManager.init(config.getMongoConnectionString(), config.getDatabaseName());

            // 注册消息处理器（扫描方式）
            registerHandlers();

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
}
