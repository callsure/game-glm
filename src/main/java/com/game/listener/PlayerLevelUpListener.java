package com.game.listener;

import com.game.event.Subscribe;
import com.game.event.impl.PlayerLevelUpEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 玩家升级监听器
 * 处理玩家升级相关事件
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于处理玩家升级事件
 * - 关注点分离：业务逻辑从 Handler 中分离出来
 *
 * @author Harleysama
 */
@Slf4j
public class PlayerLevelUpListener {

    /**
     * 处理玩家升级事件（同步，高优先级）
     * 用于检查升级奖励、解锁新功能等核心逻辑
     *
     * @param event 玩家升级事件
     */
    @Subscribe(priority = 10)
    public void onPlayerLevelUp(PlayerLevelUpEvent event) {
        log.info("=== 玩家升级 (同步，高优先级) ===");
        log.info("玩家ID: {}", event.getPlayerId());
        log.info("玩家名称: {}", event.getPlayerName());
        log.info("等级: {} -> {}", event.getOldLevel(), event.getNewLevel());

        // TODO: 发放升级奖励、解锁新功能等
    }

    /**
     * 处理玩家升级事件（异步）
     * 用于记录升级日志、推送成就等可以异步执行的操作
     *
     * @param event 玩家升级事件
     */
    @Subscribe(async = true)
    public void onPlayerLevelUpAsync(PlayerLevelUpEvent event) {
        log.info("=== 玩家升级 (异步) ===");
        log.info("玩家ID: {}", event.getPlayerId());
        log.info("记录升级日志到数据库...");

        // TODO: 异步保存日志、推送成就等
    }
}
