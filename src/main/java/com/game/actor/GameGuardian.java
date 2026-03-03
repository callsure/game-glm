package com.game.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Signal;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏 Guardian Actor
 * 系统根 Actor，负责管理所有顶层子 Actor
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于管理子 Actor 的生命周期
 * - 监督策略：定义子 Actor 的错误恢复策略
 *
 * @author Harleysama
 */
@Slf4j
public class GameGuardian extends AbstractBehavior<GameCommand> {

    /**
     * 会话管理器 Actor 引用
     */
    @Getter
    private ActorRef<SessionManagerCommand> sessionManager;

    /**
     * 网关 Actor 引用
     */
    @Getter
    private ActorRef<GatewayCommand> gateway;

    /**
     * 登录 Actor 引用
     */
    @Getter
    private ActorRef<LoginCommand> loginActor;

    /**
     * 私有构造函数
     *
     * @param context Actor 上下文
     */
    private GameGuardian(ActorContext<GameCommand> context) {
        super(context);
        log.info("Game Guardian Actor 启动");
    }

    /**
     * 创建 Guardian Actor 行为
     *
     * @return Behavior
     */
    public static Behavior<GameCommand> create() {
        return Behaviors.setup(GameGuardian::new);
    }

    @Override
    public Receive<GameCommand> createReceive() {
        // 初始化阶段：创建子 Actor
        return newReceiveBuilder()
                .onMessage(InitSystem.class, this::onInitSystem)
                .onSignal(akka.actor.typed.PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * 初始化系统
     */
    private Behavior<GameCommand> onInitSystem(InitSystem msg) {
        log.info("开始初始化游戏 Guardian 子 Actor...");

        // 创建会话管理器 Actor
        sessionManager = getContext().spawn(
                SessionManagerActor.create(),
                "session-manager"
        );
        log.info("会话管理器 Actor 已创建");

        // 创建登录 Actor
        loginActor = getContext().spawn(
                LoginActor.create(),
                "login"
        );
        log.info("登录 Actor 已创建");

        // 创建网关 Actor（需要 SessionManager 引用）
        gateway = getContext().spawn(
                GatewayActor.create(sessionManager),
                "gateway"
        );
        log.info("网关 Actor 已创建");

        // 切换到正常处理模式
        return newReceiveBuilder()
                .onMessage(com.game.actor.GameCommand.GetLoginActorRef.class, this::onGetLoginActorRef)
                .onSignal(akka.actor.typed.PostStop.class, signal -> onPostStop())
                .onSignal(akka.actor.typed.PreRestart.class, signal -> {
                    log.warn("Guardian 重启");
                    return this;
                })
                .build();
    }

    /**
     * 获取 LoginActor 引用
     */
    private Behavior<GameCommand> onGetLoginActorRef(com.game.actor.GameCommand.GetLoginActorRef msg) {
        msg.replyTo().tell(loginActor);
        return this;
    }

    /**
     * Actor 停止时的处理
     */
    private Behavior<GameCommand> onPostStop() {
        log.info("Game Guardian Actor 停止");
        return this;
    }

    // ==================== 内部消息类 ====================

    /**
     * 初始化系统命令
     */
    public static class InitSystem implements GameCommand {
        private InitSystem() {
        }

        public static InitSystem getInstance() {
            return new InitSystem();
        }
    }

    /**
     * 停止后命令
     */
    public static class PostStop implements GameCommand {
        private PostStop() {
        }

        public static PostStop getInstance() {
            return new PostStop();
        }
    }
}
