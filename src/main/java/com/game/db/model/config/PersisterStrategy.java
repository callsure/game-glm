package com.game.db.model.config;

/**
 * 持久化策略配置类
 * 定义持久化的类型（定时、定间隔等）和配置参数
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于持久化策略配置
 * - 策略模式：通过 type 枚举选择不同的持久化实现
 *
 * @author Harleysama
 */
public class PersisterStrategy {

    /**
     * 策略名称
     */
    private String strategy;

    /**
     * 持久化类型
     */
    private PersisterTypeEnum type;

    /**
     * 策略配置（如 cron 表达式、时间间隔等）
     */
    private String config;

    public PersisterStrategy() {
    }

    /**
     * 构造方法
     *
     * @param strategy  策略名称
     * @param type      持久化类型
     * @param config    策略配置
     */
    public PersisterStrategy(String strategy, String type, String config) {
        this.strategy = strategy;
        this.config = config;
        this.type = PersisterTypeEnum.getPersisterType(type);
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public PersisterTypeEnum getType() {
        return type;
    }

    public void setType(PersisterTypeEnum type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
