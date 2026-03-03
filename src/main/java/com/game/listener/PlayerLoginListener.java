package com.game.listener;

import com.game.event.Subscribe;
import com.game.event.impl.PlayerLoginEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 玩家登录监听器
 * 处理玩家登录相关事件
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于处理玩家登录事件
 * - 关注点分离：业务逻辑从 Handler 中分离出来
 *
 * @author Harleysama
 */
@Slf4j
public class PlayerLoginListener {

    /**
     * 处理玩家登录事件（同步）
     * 用于记录日志、发送欢迎消息等必须同步执行的操作
     *
     * @param event 玩家登录事件
     */
    @Subscribe
    public void onPlayerLogin(PlayerLoginEvent event) {
        log.info("=== 玩家登录 (同步) ===");
        log.info("玩家ID: {}", event.getPlayerId());
        log.info("玩家名称: {}", event.getPlayerName());
        log.info("登录IP: {}", event.getIpAddress());
        log.info("登录时间: {}", event.getLoginTime());

        // TODO: 发送欢迎消息、更新在线列表等
    }

    /**
     * 处理玩家登录事件（异步）
     * 用于保存登录日志、发放新手礼包等可以异步执行的操作
     *
     * @param event 玩家登录事件
     */
    @Subscribe(async = true)
    public void onPlayerLoginAsync(PlayerLoginEvent event) {
        log.info("=== 玩家登录 (异步) ===");
        log.info("玩家ID: {}", event.getPlayerId());
        log.info("记录登录日志到数据库...");

        // TODO: 异步保存登录日志、发放奖励等
    }
}
