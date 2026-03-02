package com.game.db.manager;

import com.game.db.cache.IEntityCaches;
import com.game.db.model.entity.IEntity;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Collection;

/**
 * ORM 管理器接口
 * 定义 ORM 框架的核心管理功能
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：定义最小必要方法
 * - 依赖倒置原则（DIP）：高层依赖抽象接口
 *
 * @author Harleysama
 */
public interface IOrmManager {

    /**
     * 初始化前阶段
     * 扫描实体类、创建缓存对象
     */
    void initBefore();

    /**
     * 注入阶段
     * 将缓存注入到使用 @EntityCachesInjection 注解的字段
     */
    void inject();

    /**
     * 初始化后阶段
     * 清理未使用的缓存
     */
    void initAfter();

    /**
     * 获取 MongoDB 客户端
     * 通过客户端可以获取其他数据库或执行复杂操作
     *
     * @return MongoClient
     */
    MongoClient mongoClient();

    /**
     * 获取实体缓存
     *
     * @param clazz 实体类型
     * @param <E>   实体类型
     * @return 实体缓存
     */
    <E extends IEntity<?>> IEntityCaches<?, E> getEntityCaches(Class<E> clazz);

    /**
     * 获取所有实体缓存
     *
     * @return 实体缓存集合
     */
    Collection<IEntityCaches<?, ?>> getAllEntityCaches();

    /**
     * 获取实体类的 MongoDB 集合
     * 基于对象的 ORM 操作
     *
     * @param entityClazz 实体类型
     * @param <E>         实体类型
     * @return MongoCollection
     */
    <E extends IEntity<?>> MongoCollection<E> getCollection(Class<E> entityClazz);

    /**
     * 获取指定名称的 MongoDB 集合
     * 用于更细粒度的操作
     *
     * @param collection 集合名称
     * @return MongoCollection
     */
    MongoCollection<Document> getCollection(String collection);
}
