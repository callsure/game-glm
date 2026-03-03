package com.game.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.ChildFailed;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话管理器 Actor
 * 负责管理所有玩家 Actor 的创建和生命周期
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于玩家 Actor 的管理
 * - 容错机制：子 Actor 崩溃时自动重启
 * - 资源管理：跟踪所有活跃玩家
 *
 * @author Harleysama
 */
@Slf4j
public class SessionManagerActor extends AbstractBehavior<SessionManagerCommand> {

    /**
     * 在线玩家映射 (playerId -> ActorRef)
     */
    private final Map<Long, ActorRef<PlayerCommand>> onlinePlayers = new HashMap<>();

    /**
     * 会话到玩家映射 (sessionId -> playerId)
     */
    private final Map<Long, Long> sessionToPlayer = new HashMap<>();

    /**
     * 私有构造函数
     *
     * @param context Actor 上下文
     */
    private SessionManagerActor(ActorContext<SessionManagerCommand> context) {
        super(context);
        log.info("会话管理器 Actor 启动");
    }

    /**
     * 创建 SessionManager Actor 行为
     *
     * @return Behavior
     */
    public static Behavior<SessionManagerCommand> create() {
        return Behaviors.setup(SessionManagerActor::new);
    }

    @Override
    public Receive<SessionManagerCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(SessionManagerCommand.PlayerConnect.class, this::onPlayerConnect)
                .onMessage(SessionManagerCommand.PlayerDisconnect.class, this::onPlayerDisconnect)
                .onMessage(SessionManagerCommand.GetPlayerActor.class, this::onGetPlayerActor)
                .onMessage(SessionManagerCommand.PlayerLogin.class, this::onPlayerLogin)
                .onMessage(SessionManagerCommand.PlayerLogout.class, this::onPlayerLogout)
                .onMessage(SessionManagerCommand.Broadcast.class, this::onBroadcast)
                .onSignal(ChildFailed.class, this::onChildFailed)
                .onSignal(akka.actor.typed.PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * 处理玩家连接
     */
    private Behavior<SessionManagerCommand> onPlayerConnect(SessionManagerCommand.PlayerConnect msg) {
        log.debug("玩家连接: sessionId={}, channelId={}", msg.sessionId(), msg.channelId());
        // 连接时不创建 Player Actor，等登录后再创建
        return this;
    }

    /**
     * 处理玩家断开连接
     */
    private Behavior<SessionManagerCommand> onPlayerDisconnect(SessionManagerCommand.PlayerDisconnect msg) {
        log.debug("玩家断开连接: sessionId={}", msg.sessionId());

        Long playerId = sessionToPlayer.remove(msg.sessionId());
        if (playerId != null) {
            ActorRef<PlayerCommand> playerActor = onlinePlayers.get(playerId);
            if (playerActor != null) {
                // 通知玩家 Actor 会话断开
                playerActor.tell(new PlayerCommand.UnbindSession(null));
            }
        }
        return this;
    }

    /**
     * 获取玩家 Actor
     */
    private Behavior<SessionManagerCommand> onGetPlayerActor(SessionManagerCommand.GetPlayerActor msg) {
        ActorRef<PlayerCommand> playerActor = onlinePlayers.get(msg.playerId());
        if (playerActor != null) {
            // 返回玩家 Actor 引用（这里简化处理）
            log.debug("获取玩家 Actor: playerId={}", msg.playerId());
        } else {
            log.warn("玩家不在线: playerId={}", msg.playerId());
        }
        return this;
    }

    /**
     * 处理玩家登录
     */
    private Behavior<SessionManagerCommand> onPlayerLogin(SessionManagerCommand.PlayerLogin msg) {
        log.info("玩家登录: playerId={}, sessionId={}", msg.playerId(), msg.sessionId());

        ActorRef<PlayerCommand> playerActor = onlinePlayers.get(msg.playerId());

        if (playerActor == null) {
            // 创建新的玩家 Actor
            playerActor = getContext().spawn(
                    Behaviors.supervise(PlayerActor.create(msg.playerId()))
                            .onFailure(Exception.class, SupervisorStrategy.restart()),
                    "player-" + msg.playerId()
            );
            onlinePlayers.put(msg.playerId(), playerActor);
            log.info("创建玩家 Actor: playerId={}", msg.playerId());
        }

        // 绑定会话
        sessionToPlayer.put(msg.sessionId(), msg.playerId());

        return this;
    }

    /**
     * 处理玩家登出
     */
    private Behavior<SessionManagerCommand> onPlayerLogout(SessionManagerCommand.PlayerLogout msg) {
        log.info("玩家登出: playerId={}", msg.playerId());

        ActorRef<PlayerCommand> playerActor = onlinePlayers.remove(msg.playerId());
        if (playerActor != null) {
            // 停止玩家 Actor
            getContext().stop(playerActor);
        }

        // 移除会话映射
        sessionToPlayer.values().removeIf(id -> id.equals(msg.playerId()));

        return this;
    }

    /**
     * 广播消息
     */
    private Behavior<SessionManagerCommand> onBroadcast(SessionManagerCommand.Broadcast msg) {
        log.debug("广播消息给所有在线玩家，当前在线人数: {}", onlinePlayers.size());

        // 广播给所有在线玩家
        onlinePlayers.values().forEach(playerActor -> {
            playerActor.tell(new PlayerCommand.HandleNetMessage(msg.message(), null));
        });

        return this;
    }

    /**
     * 处理子 Actor 失败
     */
    private Behavior<SessionManagerCommand> onChildFailed(ChildFailed signal) {
        log.error("子 Actor 失败: {}", signal.getRef());
        return this;
    }

    /**
     * Actor 停止处理
     */
    private Behavior<SessionManagerCommand> onPostStop() {
        log.info("会话管理器 Actor 停止，清理 {} 个在线玩家", onlinePlayers.size());
        onlinePlayers.clear();
        sessionToPlayer.clear();
        return this;
    }

    /**
     * 获取在线玩家数量
     */
    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }
}
