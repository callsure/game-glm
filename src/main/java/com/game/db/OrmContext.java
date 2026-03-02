package com.game.db;

import com.game.db.accessor.IAccessor;
import com.game.db.accessor.MongoAccessor;
import com.game.db.cache.IEntityCaches;
import com.game.db.manager.IOrmManager;
import com.game.db.manager.OrmManager;
import com.game.db.model.config.OrmConfig;
import com.game.db.model.entity.IEntity;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;

/**
 * ORM 上下文类
 * ORM 框架的统一入口，管理所有 ORM 组件
 * <p>
 * 设计原则：
 * - 单例模式：确保全局唯一实例
 * - 门面模式：提供简洁的统一入口
 * - 延迟初始化：按需加载组件
 *
 * @author Harleysama
 */
@Slf4j
public class OrmContext {

    private static final OrmContext INSTANCE = new OrmContext();

    private IOrmManager ormManager;

    private IAccessor accessor;

    private boolean stop = false;

    private OrmContext() {
        // 私有构造函数，防止外部实例化
    }

    /**
     * 获取 ORM 上下文实例
     *
     * @return OrmContext 实例
     */
    public static OrmContext getOrmContext() {
        return INSTANCE;
    }

    /**
     * 初始化 ORM 框架
     *
     * @param ormConfig ORM 配置
     */
    public void init(OrmConfig ormConfig) {
        log.info("初始化 ORM 框架...");

        // 创建 ORM 管理器
        ormManager = new OrmManager(ormConfig);

        // 初始化前阶段（扫描实体、创建连接）
        ormManager.initBefore();

        // 创建数据访问器
        accessor = new MongoAccessor(ormManager::getCollection);

        // 注入阶段（注入缓存到 Manager）
        ormManager.inject();

        // 初始化后阶段（清理未使用缓存）
        ormManager.initAfter();

        log.info("ORM 框架启动成功! (*￣︶￣)");
    }

    /**
     * 关闭 ORM 框架
     */
    public void shutdown() {
        shutdownBetween();
        shutdownAfter();
    }

    /**
     * 获取 ORM 管理器
     *
     * @return IOrmManager
     */
    public static IOrmManager getOrmManager() {
        return INSTANCE.ormManager;
    }

    /**
     * 获取数据访问器
     *
     * @return IAccessor
     */
    public static IAccessor getAccessor() {
        return INSTANCE.accessor;
    }

    /**
     * 获取实体缓存
     *
     * @param clazz 实体类型
     * @param <E>   实体类型
     * @return 实体缓存
     */
    public static <E extends IEntity<?>> IEntityCaches<?, E> getEntityCaches(Class<E> clazz) {
        return INSTANCE.ormManager.getEntityCaches(clazz);
    }

    /**
     * 是否已停止
     *
     * @return true 表示已停止
     */
    public static boolean isStop() {
        return INSTANCE.stop;
    }

    /**
     * 关闭中间阶段（持久化缓存）
     */
    private static synchronized void shutdownBetween() {
        INSTANCE.stop = true;
        try {
            INSTANCE.ormManager.getAllEntityCaches()
                    .forEach(IEntityCaches::persistAll);
            log.info("缓存数据持久化完成");
        } catch (Exception e) {
            log.error("关闭服务器时，持久化缓存数据异常", e);
        } finally {
            INSTANCE.stop = true;
        }
    }

    /**
     * 关闭后阶段（关闭数据库连接）
     */
    private static synchronized void shutdownAfter() {
        try {
            if (INSTANCE.ormManager != null) {
                MongoClient mongoClient = INSTANCE.ormManager.mongoClient();
                if (mongoClient != null) {
                    mongoClient.close();
                    log.info("MongoDB 连接已关闭");
                }
            }
        } catch (Exception e) {
            log.error("关闭 MongoDB 连接失败", e);
        }
        log.info("ORM 框架已优雅关闭");
    }
}
