package com.game.db.model.config;

/**
 * 缓存策略配置类
 * 定义缓存的大小、过期时间等参数
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于缓存策略配置
 * - 简单构造模式：提供便捷的构造方法
 *
 * @author Harleysama
 */
public class CacheStrategy {

    /**
     * 策略名称
     */
    private String strategy;

    /**
     * 缓存大小
     */
    private int size;

    /**
     * 过期时间（毫秒）
     */
    private long expireMillisecond;

    public CacheStrategy() {
    }

    /**
     * 构造方法
     *
     * @param strategy           策略名称
     * @param size               缓存大小
     * @param expireMillisecond  过期时间（毫秒）
     */
    public CacheStrategy(String strategy, int size, long expireMillisecond) {
        this.strategy = strategy;
        this.size = size;
        this.expireMillisecond = expireMillisecond;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getExpireMillisecond() {
        return expireMillisecond;
    }

    public void setExpireMillisecond(long expireMillisecond) {
        this.expireMillisecond = expireMillisecond;
    }
}
