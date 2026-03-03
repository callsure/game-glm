package com.game.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件订阅注解
 * 用于标记监听器方法，支持自动扫描注册
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Subscribe
 * public void onPlayerLogin(PlayerLoginEvent event) {
 *     // 处理玩家登录事件
 * }
 * }</pre>
 * <p>
 * 设计原则：
 * - 约定优于配置：通过方法签名自动识别事件类型
 * - 单一职责：每个监听器方法只处理一种事件类型
 *
 * @author Harleysama
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * 是否异步执行
     * true: 在独立线程池中异步执行
     * false: 在发布线程中同步执行
     */
    boolean async() default false;

    /**
     * 执行优先级（数字越小优先级越高）
     * 同优先级按注册顺序执行
     */
    int priority() default 100;
}
