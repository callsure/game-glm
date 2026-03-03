package com.game.event.impl;

import com.game.event.GameEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 玩家升级事件
 * 当玩家等级提升时触发
 *
 * @author Harleysama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLevelUpEvent implements GameEvent {

    /**
     * 玩家 ID
     */
    private Long playerId;

    /**
     * 玩家名称
     */
    private String playerName;

    /**
     * 旧等级
     */
    private int oldLevel;

    /**
     * 新等级
     */
    private int newLevel;

    @Override
    public String getSource() {
        return "PlayerLevelUp";
    }
}
