package com.game.actor;

import akka.actor.typed.ActorRef;

/**
 * 游戏命令接口
 * 所有 Actor 消息都需要实现此接口
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：定义最小必要接口
 * - 类型安全：使用 Java 类型系统确保消息安全
 *
 * @author Harleysama
 */
public interface GameCommand {
    // 标记接口，所有 Actor 消息都实现此接口

    /**
     * 获取 LoginActor 引用命令
     * 用于从 Guardian 获取 LoginActor 的引用
     */
    record GetLoginActorRef(
            ActorRef<ActorRef<LoginCommand>> replyTo
    ) implements GameCommand {
    }
}
