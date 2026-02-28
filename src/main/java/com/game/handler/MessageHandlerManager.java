package com.game.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理器管理器
 * 管理所有消息处理器的注册和分发
 *
 * @author Harleysama
 */
@Slf4j
public class MessageHandlerManager {

    private static final MessageHandlerManager INSTANCE = new MessageHandlerManager();

    /**
     * 消息类型 -> 处理器 映射
     */
    private final Map<Integer, MessageHandler> handlers = new ConcurrentHashMap<>();

    private MessageHandlerManager() {
    }

    /**
     * 获取单例实例
     */
    public static MessageHandlerManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册处理器
     */
    public void registerHandler(MessageHandler handler) {
        int messageType = handler.getMessageType();
        handlers.put(messageType, handler);
        log.info("注册消息处理器: type={}, handler={}", messageType, handler.getClass().getSimpleName());
    }

    /**
     * 获取处理器
     */
    public MessageHandler getHandler(int messageType) {
        return handlers.get(messageType);
    }

    /**
     * 移除处理器
     */
    public void removeHandler(int messageType) {
        handlers.remove(messageType);
        log.info("移除消息处理器: type={}", messageType);
    }
}
