package com.game.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB 管理器
 * 管理 MongoDB 连接和数据库访问
 *
 * @author Harleysama
 */
@Slf4j
public class MongoManager {

    private static MongoManager INSTANCE;

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    private MongoManager(String connectionString, String databaseName) {
        log.info("正在连接 MongoDB: database={}", databaseName);
        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase(databaseName);
        log.info("MongoDB 连接成功! (*￣︶￣)");
    }

    /**
     * 初始化 MongoDB 管理器
     */
    public static synchronized void init(String connectionString, String databaseName) {
        if (INSTANCE == null) {
            INSTANCE = new MongoManager(connectionString, databaseName);
        }
    }

    /**
     * 获取单例实例
     */
    public static MongoManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("MongoManager 未初始化，请先调用 init() 方法");
        }
        return INSTANCE;
    }

    /**
     * 获取数据库
     */
    public MongoDatabase getDatabase() {
        return database;
    }

    /**
     * 获取 Mongo 客户端
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            log.info("MongoDB 连接已关闭");
        }
    }
}
