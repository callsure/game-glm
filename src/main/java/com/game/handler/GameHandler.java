package com.game.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 游戏消息处理器注解
 * 用于标记消息处理器类，支持自动扫描注册
 *
 * @author Harleysama
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameHandler {

    /**
     * 消息类型
     * 对应 Protocol Buffers 中定义的消息类型值
     */
    int messageType();
}
