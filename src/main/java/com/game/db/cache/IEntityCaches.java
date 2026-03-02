package com.game.db.cache;

import com.game.db.model.entity.IEntity;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 实体缓存接口
 * 定义实体缓存的核心操作
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：定义最小必要方法
 * - 单一职责原则（SRP）：专注于缓存操作
 *
 * @param <PK> 主键类型
 * @param <E>  实体类型
 * @author Harleysama
 */
public interface IEntityCaches<PK extends Comparable<PK>, E extends IEntity<PK>> {

    /**
     * 从数据库加载数据到缓存
     * 如果数据库不存在则返回一个 id 为空的默认值，并将默认值加入缓存
     *
     * @param pk 主键
     * @return 实体对象
     */
    E load(PK pk);

    /**
     * 加入缓存
     *
     * @param entity 实体对象
     */
    void addLoad(E entity);

    /**
     * 更新缓存中的数据
     * 只更新缓存的时间戳，并通过一定策略写入到数据库
     *
     * @param entity 实体对象
     */
    void update(E entity);

    /**
     * 使缓存失效
     * 不会删除数据库中的数据，只会删除缓存数据
     *
     * @param pk 主键
     */
    void invalidate(PK pk);

    /**
     * 持久化所有缓存数据到数据库
     */
    void persistAll();

    /**
     * 获取所有存在的缓存对象
     *
     * @return 实体列表
     */
    List<E> allPresentCaches();

    /**
     * 遍历所有缓存
     *
     * @param biConsumer 消费者
     */
    void forEach(BiConsumer<PK, E> biConsumer);

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    long size();

    /**
     * 获取缓存状态统计
     *
     * @return 状态字符串
     */
    String recordStatus();
}
