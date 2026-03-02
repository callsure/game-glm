package com.game.db.model.vo;

import java.lang.reflect.Field;

/**
 * 索引定义类
 * 封装字段的索引元数据信息
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于索引元数据管理
 * - 不可变对象：通过构造函数初始化，不提供 setter
 *
 * @author Harleysama
 */
public class IndexDef {

    /**
     * 索引字段
     */
    private final Field field;

    /**
     * 是否升序索引
     */
    private final boolean ascending;

    /**
     * 是否唯一索引
     */
    private final boolean unique;

    /**
     * TTL 过期时间（秒）
     * -1 表示不开启 TTL
     */
    private final long ttlExpireAfterSeconds;

    /**
     * 构造方法
     *
     * @param field                   索引字段
     * @param ascending               是否升序
     * @param unique                  是否唯一
     * @param ttlExpireAfterSeconds   TTL 过期时间（秒）
     */
    public IndexDef(Field field, boolean ascending, boolean unique, long ttlExpireAfterSeconds) {
        this.field = field;
        this.ascending = ascending;
        this.unique = unique;
        this.ttlExpireAfterSeconds = ttlExpireAfterSeconds;
    }

    public Field getField() {
        return field;
    }

    public boolean isAscending() {
        return ascending;
    }

    public boolean isUnique() {
        return unique;
    }

    public long getTtlExpireAfterSeconds() {
        return ttlExpireAfterSeconds;
    }
}
