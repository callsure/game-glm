package com.game.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游戏事件基础接口
 * 所有游戏事件都需要实现此接口
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：定义最小必要方法
 * - 单一职责原则（SRP）：专注于事件数据定义
 * - 不可变性：事件应该是不可变的，确保线程安全
 *
 * @author Harleysama
 */
public interface GameEvent extends Serializable {

    /**
     * 获取事件唯一标识
     * 用于事件追踪和日志记录
     *
     * @return 事件 ID
     */
    default String getEventId() {
        return this.getClass().getSimpleName() + "@" + System.currentTimeMillis();
    }

    /**
     * 获取事件发生时间
     *
     * @return 事件时间戳
     */
    default long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取事件来源
     * 用于追踪事件发起者
     *
     * @return 事件来源标识
     */
    default String getSource() {
        return "unknown";
    }
}
