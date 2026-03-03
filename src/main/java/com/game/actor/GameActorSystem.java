package com.game.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * 游戏 Actor 系统管理类
 * 管理 Akka ActorSystem 的生命周期
 * <p>
 * 设计原则：
 * - 单例模式：确保全局唯一的 ActorSystem
 * - 优雅关闭：使用 CoordinatedShutdown 确保安全退出
 * - 分离调度器：IO 和业务逻辑使用不同的 Dispatcher
 *
 * @author Harleysama
 */
@Slf4j
public class GameActorSystem {

    private static ActorSystem<GameCommand> SYSTEM;

    /**
     * 系统根 Actor 名称
     */
    private static final String SYSTEM_NAME = "game-server";

    /**
     * 私有构造函数
     */
    private GameActorSystem() {
    }

    /**
     * 初始化 Actor 系统
     * 必须在使用前调用
     */
    public static synchronized void init() {
        if (SYSTEM != null) {
            log.warn("Actor 系统已经初始化，跳过重复初始化");
            return;
        }

        log.info("初始化 Actor 系统: {}", SYSTEM_NAME);

        // 创建 ActorSystem
        SYSTEM = ActorSystem.create(
                GameGuardian.create(),
                SYSTEM_NAME
        );

        // 注册关闭钩子
        SYSTEM.getWhenTerminated().thenRun(() -> {
            log.info("Actor 系统已关闭");
        });

        log.info("Actor 系统初始化完成!");
    }

    /**
     * 获取 ActorSystem 实例
     *
     * @return ActorSystem 实例
     * @throws IllegalStateException 如果系统未初始化
     */
    public static ActorSystem<GameCommand> getSystem() {
        if (SYSTEM == null) {
            throw new IllegalStateException("Actor 系统未初始化，请先调用 init() 方法");
        }
        return SYSTEM;
    }

    /**
     * 获取 LoginActor 引用
     * 通过 Ask 模式从 Guardian 获取
     *
     * @return LoginActor 引用
     */
    public static ActorRef<LoginCommand> getLoginActor() {
        // 使用 Ask 模式请求 Guardian 返回 LoginActor 引用
        CompletionStage<ActorRef<LoginCommand>> future = AskPattern.ask(
                SYSTEM,
                GameCommand.GetLoginActorRef::new,
                Duration.ofSeconds(3),
                SYSTEM.scheduler()
        );

        try {
            return future.toCompletableFuture().get();
        } catch (Exception e) {
            log.error("获取 LoginActor 引用失败", e);
            throw new RuntimeException("获取 LoginActor 引用失败", e);
        }
    }

    /**
     * 关闭 Actor 系统
     * 优雅关闭所有 Actor
     */
    public static synchronized void shutdown() {
        if (SYSTEM == null) {
            log.warn("Actor 系统未初始化，无需关闭");
            return;
        }

        log.info("关闭 Actor 系统中...");
        SYSTEM.terminate();
        SYSTEM = null;
    }

    /**
     * 检查系统是否已初始化
     *
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return SYSTEM != null;
    }
}
