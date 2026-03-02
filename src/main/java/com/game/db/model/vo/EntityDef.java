package com.game.db.model.vo;

import com.game.db.model.config.PersisterStrategy;
import com.game.db.model.entity.IEntity;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 实体定义类
 * 封装实体的元数据信息，包括 ID 字段、缓存策略、持久化策略、索引定义等
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于实体元数据管理
 * - 工厂方法模式：提供 valueOf 静态工厂方法
 *
 * @author Harleysama
 */
public class EntityDef {

    /**
     * ID 字段
     */
    private Field idField;

    /**
     * 实体类
     */
    private Class<? extends IEntity<?>> clazz;

    /**
     * 缓存大小
     */
    private int cacheSize;

    /**
     * 过期时间（毫秒）
     */
    private long expireMillisecond;

    /**
     * 持久化策略
     */
    private PersisterStrategy persisterStrategy;

    /**
     * 索引定义映射 (字段名 -> 索引定义)
     */
    private Map<String, IndexDef> indexDefMap;

    /**
     * 文本索引定义映射 (字段名 -> 文本索引定义)
     */
    private Map<String, IndexTextDef> indexTextDefMap;

    /**
     * 静态工厂方法
     *
     * @param idField              ID 字段
     * @param clazz                实体类
     * @param cacheSize            缓存大小
     * @param expireMillisecond    过期时间（毫秒）
     * @param persisterStrategy    持久化策略
     * @param indexDefMap          索引定义映射
     * @param indexTextDefMap      文本索引定义映射
     * @return EntityDef 实例
     */
    public static EntityDef valueOf(Field idField, Class<? extends IEntity<?>> clazz, int cacheSize, long expireMillisecond
            , PersisterStrategy persisterStrategy, Map<String, IndexDef> indexDefMap, Map<String, IndexTextDef> indexTextDefMap) {
        EntityDef entityDef = new EntityDef();
        entityDef.idField = idField;
        entityDef.clazz = clazz;
        entityDef.cacheSize = cacheSize;
        entityDef.expireMillisecond = expireMillisecond;
        entityDef.persisterStrategy = persisterStrategy;
        entityDef.indexDefMap = indexDefMap;
        entityDef.indexTextDefMap = indexTextDefMap;
        return entityDef;
    }

    public Class<? extends IEntity<?>> getClazz() {
        return clazz;
    }

    /**
     * 创建新实体实例
     *
     * @param id 主键值
     * @return 新实体实例
     */
    public IEntity<?> newEntity(Object id) {
        try {
            IEntity<?> entity = clazz.getDeclaredConstructor().newInstance();
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("无法创建实体实例: " + clazz.getSimpleName(), e);
        }
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public long getExpireMillisecond() {
        return expireMillisecond;
    }

    public PersisterStrategy getPersisterStrategy() {
        return persisterStrategy;
    }

    public Map<String, IndexDef> getIndexDefMap() {
        return indexDefMap;
    }

    public Map<String, IndexTextDef> getIndexTextDefMap() {
        return indexTextDefMap;
    }

    public Field getIdField() {
        return idField;
    }
}
