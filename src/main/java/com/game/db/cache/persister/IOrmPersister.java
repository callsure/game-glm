package com.game.db.cache.persister;

/**
 * ORM 持久化器接口
 * 定义缓存数据的持久化策略
 * <p>
 * 设计原则：
 * - 策略模式：不同的持久化策略实现
 * - 单一职责原则（SRP）：专注于持久化操作
 *
 * @author Harleysama
 */
public interface IOrmPersister {

    /**
     * 启动持久化器
     */
    void start();

    /**
     * 停止持久化器
     */
    void stop();
}
