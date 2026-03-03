package com.game.event.impl;

import com.game.event.GameEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 玩家登录事件
 * 当玩家成功登录时触发
 *
 * @author Harleysama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLoginEvent implements GameEvent {

    /**
     * 玩家 ID
     */
    private Long playerId;

    /**
     * 玩家名称
     */
    private String playerName;

    /**
     * 登录 IP 地址
     */
    private String ipAddress;

    /**
     * 登录时间戳
     */
    private long loginTime;

    @Override
    public String getSource() {
        return "PlayerLogin";
    }
}
