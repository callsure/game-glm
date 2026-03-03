package com.game.actor;

import akka.actor.typed.ActorRef;
import com.game.net.Session;

/**
 * 玩家命令接口
 * 定义玩家 Actor 可以接收的所有命令
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：细分命令类型
 * - 不可变性：所有命令都是不可变记录类
 *
 * @author Harleysama
 */
public interface PlayerCommand extends GameCommand {

    /**
     * 玩家移动命令
     */
    record Move(
            int x,
            int y,
            int direction,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 玩家聊天命令
     */
    record Chat(
            String message,
            int chatType,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 玩家攻击命令
     */
    record Attack(
            long targetId,
            int skillId,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 玩家使用物品命令
     */
    record UseItem(
            long itemId,
            int count,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 处理网络消息
     * 将 Netty 收到的消息转发给玩家 Actor
     */
    record HandleNetMessage(
            Object message,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 绑定会话
     */
    record BindSession(
            Session session,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 解绑会话
     */
    record UnbindSession(
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 保存数据命令
     */
    record SaveData(
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 踢出玩家
     */
    record Kick(
            String reason,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }

    /**
     * 心跳检测
     */
    record Heartbeat(
            long timestamp,
            ActorRef<GameCommand> replyTo
    ) implements PlayerCommand {
    }
}
