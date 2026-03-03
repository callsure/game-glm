package com.game.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 事件监听器包装类
 * 封装监听器方法的元数据信息
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于监听器元数据封装
 * - 不可变性：使用 final 字段确保线程安全
 *
 * @author Harleysama
 */
@Data
@AllArgsConstructor
public class EventListenerWrapper {

    /**
     * 监听器对象实例
     */
    private final Object listener;

    /**
     * 监听器方法
     */
    private final Method method;

    /**
     * 是否异步执行
     */
    private final boolean async;

    /**
     * 执行优先级（数字越小优先级越高）
     */
    private final int priority;

    /**
     * 事件类型
     */
    private final Class<? extends GameEvent> eventType;

    /**
     * 执行监听器方法
     *
     * @param event 事件对象
     * @throws Exception 执行异常
     */
    public void invoke(GameEvent event) throws Exception {
        method.setAccessible(true);
        method.invoke(listener, event);
    }

    /**
     * 创建监听器包装器的工厂方法
     *
     * @param listener   监听器对象
     * @param method     监听器方法
     * @param async      是否异步
     * @param priority   优先级
     * @param eventType 事件类型
     * @return 监听器包装器
     */
    public static EventListenerWrapper of(Object listener, Method method, boolean async, int priority, Class<? extends GameEvent> eventType) {
        return new EventListenerWrapper(listener, method, async, priority, eventType);
    }
}
