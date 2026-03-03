package com.game.db.model.entity;

import com.game.db.OrmContext;

/**
 * ORM 实体接口
 * 所有需要持久化的实体都必须实现此接口
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：定义实体最小必要方法
 * - 依赖倒置原则（DIP）：高层模块依赖抽象接口而非具体实现
 *
 * @param <PK> 主键类型，必须可比较
 * @author Harleysama
 */
public interface IEntity<PK extends Comparable<PK>> {

    /**
     * 获取实体主键
     *
     * @return 主键值
     */
    PK id();

    /**
     * 获取版本号
     * 用于分布式环境下的数据一致性控制
     * 写入数据时会对比版本号，只有版本号一致才能写入
     *
     * @return 版本号
     */
    default long gvs() {
        return 0L;
    }

    /**
     * 设置版本号
     *
     * @param vs 版本号
     */
    default void svs(long vs) {
    }

    /**
     * 判断实体是否为空
     * 查询不存在的数据时缓存中也会有一份，因此需要根据实际类型判断
     *
     * @return 实体为空返回true
     */
    default boolean empty() {
        PK idValue = id();
        if (idValue == null) {
            return true;
        }
        // id只能是 Number 或 String 类型
        if (idValue instanceof Number) {
            return ((Number) idValue).doubleValue() == 0D;
        } else {
            return idValue.toString().isEmpty();
        }
    }

    /**
     * 保存并插入实体
     * 便捷方法，直接调用数据访问层插入数据
     */
    default void saveAndInsert() {
        OrmContext.getAccessor().save(this);
    }

    /**
     * 删除实体
     * 便捷方法，直接调用数据访问层删除数据
     */
    default void deleteEntity() {
        OrmContext.getAccessor().delete(this);
    }
}
