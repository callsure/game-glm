package com.game.actor;

/**
 * 网关命令接口
 * 定义网关 Actor 可以接收的所有命令
 * <p>
 * 网关 Actor 是 Netty 和 Akka 之间的桥梁
 *
 * @author Harleysama
 */
public interface GatewayCommand extends GameCommand {

    /**
     * 玩家连接命令
     */
    record PlayerConnected(
            long sessionId,
            String channelId
    ) implements GatewayCommand {
    }

    /**
     * 玩家断开连接命令
     */
    record PlayerDisconnected(
            long sessionId,
            String channelId
    ) implements GatewayCommand {
    }

    /**
     * 转发消息给玩家
     * 将 Netty 收到的消息转发给对应的玩家 Actor
     */
    record ForwardToPlayer(
            long playerId,
            GameCommand message
    ) implements GatewayCommand {
    }

    /**
     * 玩家登录命令
     */
    record PlayerLogin(
            long playerId,
            long sessionId
    ) implements GatewayCommand {
    }

    /**
     * 玩家登出命令
     */
    record PlayerLogout(
            long playerId
    ) implements GatewayCommand {
    }

    /**
     * 广播消息
     */
    record Broadcast(
            GameCommand message
    ) implements GatewayCommand {
    }
}
