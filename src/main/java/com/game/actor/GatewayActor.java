package com.game.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import lombok.extern.slf4j.Slf4j;

/**
 * 网关 Actor
 * Netty 和 Akka 之间的桥梁，负责将网络消息转发给 Actor 系统
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于消息转发
 * - 异步处理：不阻塞 Netty IO 线程
 * - 解耦设计：Netty 不直接依赖 Akka
 *
 * @author Harleysama
 */
@Slf4j
public class GatewayActor extends AbstractBehavior<GatewayCommand> {

    /**
     * SessionManager Actor 引用
     */
    private final ActorRef<SessionManagerCommand> sessionManager;

    /**
     * 私有构造函数
     *
     * @param context Actor 上下文
     * @param sessionManager SessionManager 引用
     */
    private GatewayActor(ActorContext<GatewayCommand> context, ActorRef<SessionManagerCommand> sessionManager) {
        super(context);
        this.sessionManager = sessionManager;
        log.info("网关 Actor 启动");
    }

    /**
     * 创建 GatewayActor 行为
     *
     * @param sessionManager SessionManager 引用
     * @return Behavior
     */
    public static Behavior<GatewayCommand> create(ActorRef<SessionManagerCommand> sessionManager) {
        return Behaviors.setup(context -> new GatewayActor(context, sessionManager));
    }

    @Override
    public Receive<GatewayCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(GatewayCommand.PlayerConnected.class, this::onPlayerConnected)
                .onMessage(GatewayCommand.PlayerDisconnected.class, this::onPlayerDisconnected)
                .onMessage(GatewayCommand.ForwardToPlayer.class, this::onForwardToPlayer)
                .onMessage(GatewayCommand.PlayerLogin.class, this::onPlayerLogin)
                .onMessage(GatewayCommand.PlayerLogout.class, this::onPlayerLogout)
                .onMessage(GatewayCommand.Broadcast.class, this::onBroadcast)
                .onSignal(akka.actor.typed.PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * 处理玩家连接
     */
    private Behavior<GatewayCommand> onPlayerConnected(GatewayCommand.PlayerConnected msg) {
        log.debug("玩家连接: sessionId={}, channelId={}", msg.sessionId(), msg.channelId());

        // 通知 SessionManager
        sessionManager.tell(new SessionManagerCommand.PlayerConnect(
                msg.sessionId(),
                msg.channelId()
        ));

        return this;
    }

    /**
     * 处理玩家断开连接
     */
    private Behavior<GatewayCommand> onPlayerDisconnected(GatewayCommand.PlayerDisconnected msg) {
        log.debug("玩家断开: sessionId={}", msg.sessionId());

        // 通知 SessionManager
        sessionManager.tell(new SessionManagerCommand.PlayerDisconnect(
                msg.sessionId(),
                msg.channelId()
        ));

        return this;
    }

    /**
     * 转发消息给玩家
     */
    private Behavior<GatewayCommand> onForwardToPlayer(GatewayCommand.ForwardToPlayer msg) {
        log.debug("转发消息给玩家: playerId={}, messageType={}",
                msg.playerId(), msg.message().getClass().getSimpleName());

        // 先登录才能转发消息
        if (msg.playerId() > 0) {
            // TODO: 获取玩家 Actor 并转发消息
            log.debug("转发消息: playerId={}", msg.playerId());
        }

        return this;
    }

    /**
     * 处理玩家登录
     */
    private Behavior<GatewayCommand> onPlayerLogin(GatewayCommand.PlayerLogin msg) {
        log.info("玩家登录: playerId={}, sessionId={}", msg.playerId(), msg.sessionId());

        // 通知 SessionManager
        sessionManager.tell(new SessionManagerCommand.PlayerLogin(
                msg.playerId(),
                msg.sessionId()
        ));

        return this;
    }

    /**
     * 处理玩家登出
     */
    private Behavior<GatewayCommand> onPlayerLogout(GatewayCommand.PlayerLogout msg) {
        log.info("玩家登出: playerId={}", msg.playerId());

        // 通知 SessionManager
        sessionManager.tell(new SessionManagerCommand.PlayerLogout(msg.playerId()));

        return this;
    }

    /**
     * 广播消息
     */
    private Behavior<GatewayCommand> onBroadcast(GatewayCommand.Broadcast msg) {
        log.debug("广播消息: messageType={}", msg.message().getClass().getSimpleName());

        // 通知 SessionManager 广播
        sessionManager.tell(new SessionManagerCommand.Broadcast(msg.message()));

        return this;
    }

    /**
     * Actor 停止处理
     */
    private Behavior<GatewayCommand> onPostStop() {
        log.info("网关 Actor 停止");
        return this;
    }
}
