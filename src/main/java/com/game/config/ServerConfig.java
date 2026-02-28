package com.game.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * 服务器配置
 *
 * @author Harleysama
 */
@Slf4j
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

    /**
     * 从 YAML 配置文件加载配置
     * 如果文件不存在或读取失败，返回默认配置
     *
     * @return 配置对象
     */
    public static ServerConfig load() {
        ServerConfig config = new ServerConfig();

        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = ServerConfig.class.getClassLoader().getResourceAsStream("application.yml");

            if (inputStream == null) {
                log.warn("未找到 application.yml 配置文件，使用默认配置");
                return config;
            }

            Map<String, Object> data = yaml.load(inputStream);
            inputStream.close();

            // 解析 server 配置
            Map<String, Object> serverConfig = (Map<String, Object>) data.get("server");
            if (serverConfig != null) {
                Object port = serverConfig.get("port");
                if (port != null) {
                    config.setPort(((Number) port).intValue());
                }
                Object workerThreads = serverConfig.get("worker-threads");
                if (workerThreads != null) {
                    config.setWorkerThreads(((Number) workerThreads).intValue());
                }
                Object heartbeatTimeout = serverConfig.get("heartbeat-timeout");
                if (heartbeatTimeout != null) {
                    config.setHeartbeatTimeout(((Number) heartbeatTimeout).intValue());
                }
            }

            // 解析 mongodb 配置
            Map<String, Object> mongoConfig = (Map<String, Object>) data.get("mongodb");
            if (mongoConfig != null) {
                Object connectionString = mongoConfig.get("connection-string");
                if (connectionString != null) {
                    config.setMongoConnectionString((String) connectionString);
                }
                Object database = mongoConfig.get("database");
                if (database != null) {
                    config.setDatabaseName((String) database);
                }
            }

            log.info("YAML 配置文件加载成功");

        } catch (Exception e) {
            log.warn("加载 application.yml 失败，使用默认配置: {}", e.getMessage());
        }

        // 环境变量优先级高于 YAML 配置（覆盖 YAML 中的值）
        return applyEnvOverrides(config);
    }

    /**
     * 应用环境变量覆盖（环境变量优先级最高）
     */
    private static ServerConfig applyEnvOverrides(ServerConfig config) {
        String port = System.getenv("GAME_SERVER_PORT");
        if (port != null) {
            config.setPort(Integer.parseInt(port));
            log.debug("环境变量覆盖: GAME_SERVER_PORT={}", port);
        }

        String mongoUrl = System.getenv("MONGO_URL");
        if (mongoUrl != null) {
            config.setMongoConnectionString(mongoUrl);
            log.debug("环境变量覆盖: MONGO_URL={}", mongoUrl);
        }

        String dbName = System.getenv("MONGO_DATABASE");
        if (dbName != null) {
            config.setDatabaseName(dbName);
            log.debug("环境变量覆盖: MONGO_DATABASE={}", dbName);
        }

        return config;
    }
}
