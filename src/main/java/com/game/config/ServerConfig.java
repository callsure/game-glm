package com.game.config;

import lombok.Data;

/**
 * 服务器配置
 *
 * @author Harleysama
 */
@Data
public class ServerConfig {

    /**
     * 服务器端口
     */
    private int port = 8888;

    /**
     * Worker线程数 (默认: CPU核心数 * 2)
     */
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 心跳超时时间(秒)
     */
    private int heartbeatTimeout = 60;

    /**
     * MongoDB 连接字符串
     */
    private String mongoConnectionString = "mongodb://localhost:27017";

    /**
     * MongoDB 数据库名称
     */
    private String databaseName = "game_db";

    /**
     * 从环境变量加载配置
     */
    public static ServerConfig fromEnv() {
        ServerConfig config = new ServerConfig();

        String port = System.getenv("GAME_SERVER_PORT");
        if (port != null) {
            config.setPort(Integer.parseInt(port));
        }

        String mongoUrl = System.getenv("MONGO_URL");
        if (mongoUrl != null) {
            config.setMongoConnectionString(mongoUrl);
        }

        String dbName = System.getenv("MONGO_DATABASE");
        if (dbName != null) {
            config.setDatabaseName(dbName);
        }

        return config;
    }
}
