package com.game.db.dao;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dao 管理器
 * 统一管理和提供所有 Dao 实例的单例访问
 * <p>
 * 设计原则：
 * - 单例模式：确保全局唯一的 DaoManager 实例
 * - 延迟初始化：Dao 实例按需创建，节省资源
 * - 线程安全：使用 ConcurrentHashMap 确保多线程安全
 * - 开闭原则：新增 Dao 类型无需修改现有代码
 * <p>
 * 使用示例：
 * <pre>
 * UserDao userDao = DaoManager.getInstance().getUserDao();
 * RoleDao roleDao = DaoManager.getInstance().getRoleDao();
 * </pre>
 *
 * @author Harleysama
 */
@Slf4j
public enum DaoManager {

    /**
     * 单例枚举实例
     */
    INSTANCE;

    /**
     * Dao 实例缓存
     * 使用 ConcurrentHashMap 确保线程安全
     */
    private final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();

    /**
     * 获取单例实例
     *
     * @return DaoManager 实例
     */
    public static DaoManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取 UserDao 实例
     * 使用双重检查锁定模式的变体，利用 computeIfAbsent 保证线程安全
     *
     * @return UserDao 单例实例
     */
    public UserDao getUserDao() {
        return getDao(UserDao.class);
    }

    /**
     * 获取 RoleDao 实例
     *
     * @return RoleDao 单例实例
     */
    public RoleDao getRoleDao() {
        return getDao(RoleDao.class);
    }

    /**
     * 获取指定类型的 Dao 实例
     * 通用方法，支持获取任意类型的 Dao
     *
     * @param daoClass Dao 类的 Class 对象
     * @param <T>      Dao 类型
     * @return Dao 单例实例
     */
    @SuppressWarnings("unchecked")
    public <T> T getDao(Class<T> daoClass) {
        return (T) daoCache.computeIfAbsent(daoClass, key -> {
            try {
                log.info("创建 Dao 实例: {}", key.getSimpleName());
                return key.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.error("创建 Dao 实例失败: {}", key.getSimpleName(), e);
                throw new RuntimeException("创建 Dao 实例失败: " + key.getSimpleName(), e);
            }
        });
    }

    /**
     * 检查指定类型的 Dao 是否已缓存
     *
     * @param daoClass Dao 类的 Class 对象
     * @return 是否已缓存
     */
    public boolean containsDao(Class<?> daoClass) {
        return daoCache.containsKey(daoClass);
    }

    /**
     * 清空所有 Dao 缓存
     * 主要用于测试场景，生产环境慎用
     */
    public void clearCache() {
        log.warn("清空 Dao 缓存");
        daoCache.clear();
    }

    /**
     * 获取缓存中的 Dao 数量
     *
     * @return Dao 数量
     */
    public int getCachedDaoCount() {
        return daoCache.size();
    }
}
