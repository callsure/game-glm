package com.game.db.cache.persister;

import com.game.db.model.entity.IEntity;

/**
 * 持久化节点类
 * 封装实体和持久化元数据
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于持久化元数据管理
 * - 不可变实体：实体对象通过 getter 获取
 *
 * @param <E> 实体类型
 * @author Harleysama
 */
public class PNode<E extends IEntity<?>> {

    /**
     * 实体对象
     */
    private E entity;

    /**
     * 修改时间（毫秒）
     */
    private long modifiedTime;

    /**
     * 写入数据库时间（毫秒）
     */
    private long writeToDbTime;

    /**
     * 线程 ID
     */
    private long threadId;

    /**
     * 构造方法
     *
     * @param entity 实体对象
     */
    public PNode(E entity) {
        this.entity = entity;
        this.modifiedTime = System.currentTimeMillis();
        this.writeToDbTime = System.currentTimeMillis();
        this.threadId = 0;
    }

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public long getWriteToDbTime() {
        return writeToDbTime;
    }

    public void setWriteToDbTime(long writeToDbTime) {
        this.writeToDbTime = writeToDbTime;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }
}
