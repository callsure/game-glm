package com.game.actor;

/**
 * 会话管理器命令接口
 * 定义会话管理相关的所有命令
 *
 * @author Harleysama
 */
public interface SessionManagerCommand extends GameCommand {

    /**
     * 玩家连接命令
     * 当新玩家连接时触发
     */
    record PlayerConnect(
            long sessionId,
            String channelId
    ) implements SessionManagerCommand {
    }

    /**
     * 玩家断开连接命令
     * 当玩家断开连接时触发
     */
    record PlayerDisconnect(
            long sessionId,
            String channelId
    ) implements SessionManagerCommand {
    }

    /**
     * 获取玩家 Actor 引用
     */
    record GetPlayerActor(
            long playerId
    ) implements SessionManagerCommand {
    }

    /**
     * 玩家登录成功
     */
    record PlayerLogin(
            long playerId,
            long sessionId
    ) implements SessionManagerCommand {
    }

    /**
     * 玩家登出
     */
    record PlayerLogout(
            long playerId
    ) implements SessionManagerCommand {
    }

    /**
     * 广播消息给所有在线玩家
     */
    record Broadcast(
            GameCommand message
    ) implements SessionManagerCommand {
    }
}
