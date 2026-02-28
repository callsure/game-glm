package com.game;

import com.game.config.ServerConfig;
import com.game.db.MongoManager;
import com.game.handler.HandshakeHandler;
import com.game.handler.HeartbeatHandler;
import com.game.handler.LoginHandler;
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

            // 注册消息处理器
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
     */
    private static void registerHandlers() {
        MessageHandlerManager manager = MessageHandlerManager.getInstance();

        manager.registerHandler(new HandshakeHandler());
        manager.registerHandler(new HeartbeatHandler());
        manager.registerHandler(new LoginHandler());

        // TODO: 注册更多处理器
        // manager.registerHandler(new CreateRoleHandler());
        // manager.registerHandler(new MoveHandler());
        // ...

        log.info("消息处理器注册完成! 共{}个", manager);
    }
}
