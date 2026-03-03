package com.game.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.game.db.OrmContext;
import com.game.db.cache.IEntityCaches;
import com.game.event.EventManager;
import com.game.event.impl.PlayerLevelUpEvent;
import com.game.event.impl.PlayerLoginEvent;
import com.game.model.User;
import com.game.net.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 玩家 Actor
 * 处理单个玩家的所有游戏逻辑
 * <p>
 * 设计原则：
 * - 单线程模型：Actor 内部消息串行处理，无需考虑线程安全
 * - 状态封装：玩家状态完全封装在 Actor 内部
 * - 事件驱动：通过事件系统与其他模块解耦
 *
 * @author Harleysama
 */
@Slf4j
public class PlayerActor extends AbstractBehavior<PlayerCommand> {

    /**
     * 玩家 ID
     */
    private final long playerId;

    /**
     * 玩家名称
     */
    private String playerName;

    /**
     * 玩家等级
     */
    @Getter
    private int level;

    /**
     * 关联的会话
     */
    private Session session;

    /**
     * 最后活跃时间
     */
    private long lastActiveTime;

    /**
     * 私有构造函数
     *
     * @param context  Actor 上下文
     * @param playerId 玩家 ID
     */
    private PlayerActor(ActorContext<PlayerCommand> context, long playerId) {
        super(context);
        this.playerId = playerId;
        this.lastActiveTime = System.currentTimeMillis();
        log.info("玩家 Actor 创建: playerId={}", playerId);
    }

    /**
     * 创建 PlayerActor 行为
     *
     * @param playerId 玩家 ID
     * @return Behavior
     */
    public static Behavior<PlayerCommand> create(long playerId) {
        return Behaviors.setup(context -> new PlayerActor(context, playerId));
    }

    @Override
    public Receive<PlayerCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlayerCommand.Chat.class, this::onChat)
                .onMessage(PlayerCommand.UseItem.class, this::onUseItem)
                .onMessage(PlayerCommand.HandleNetMessage.class, this::onHandleNetMessage)
                .onMessage(PlayerCommand.BindSession.class, this::onBindSession)
                .onMessage(PlayerCommand.UnbindSession.class, this::onUnbindSession)
                .onMessage(PlayerCommand.SaveData.class, this::onSaveData)
                .onMessage(PlayerCommand.Kick.class, this::onKick)
                .onMessage(PlayerCommand.Heartbeat.class, this::onHeartbeat)
                .onSignal(akka.actor.typed.PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * Actor 停止时保存数据
     */
    private Behavior<PlayerCommand> onPostStop() {
        log.info("玩家 Actor 停止: playerId={}", playerId);
        savePlayerData();
        return this;
    }

    /**
     * 处理玩家聊天
     */
    private Behavior<PlayerCommand> onChat(PlayerCommand.Chat msg) {
        log.info("玩家聊天: playerId={}, message={}, chatType={}",
                playerId, msg.message(), msg.chatType());

        // TODO: 过滤敏感词、广播聊天消息等

        updateActiveTime();
        return this;
    }

    /**
     * 处理使用物品
     */
    private Behavior<PlayerCommand> onUseItem(PlayerCommand.UseItem msg) {
        log.debug("玩家使用物品: playerId={}, itemId={}, count={}",
                playerId, msg.itemId(), msg.count());

        // TODO: 扣除物品、应用效果等

        updateActiveTime();
        return this;
    }

    /**
     * 处理网络消息
     * 将 Netty 收到的消息分发到对应的处理方法
     */
    private Behavior<PlayerCommand> onHandleNetMessage(PlayerCommand.HandleNetMessage msg) {
        log.debug("处理网络消息: playerId={}, message={}", playerId, msg.message().getClass().getSimpleName());

        // TODO: 根据消息类型分发到不同的处理逻辑

        updateActiveTime();
        return this;
    }

    /**
     * 绑定会话
     */
    private Behavior<PlayerCommand> onBindSession(PlayerCommand.BindSession msg) {
        log.info("绑定会话: playerId={}", playerId);
        this.session = msg.session();

        // 加载玩家数据
        loadPlayerData();

        return this;
    }

    /**
     * 解绑会话
     */
    private Behavior<PlayerCommand> onUnbindSession(PlayerCommand.UnbindSession msg) {
        log.info("解绑会话: playerId={}", playerId);
        this.session = null;

        // 保存玩家数据
        savePlayerData();

        return this;
    }

    /**
     * 保存数据
     */
    private Behavior<PlayerCommand> onSaveData(PlayerCommand.SaveData msg) {
        savePlayerData();
        return this;
    }

    /**
     * 踢出玩家
     */
    private Behavior<PlayerCommand> onKick(PlayerCommand.Kick msg) {
        log.warn("踢出玩家: playerId={}, reason={}", playerId, msg.reason());

        // TODO: 通知 Netty 关闭连接

        return this;
    }

    /**
     * 处理心跳
     */
    private Behavior<PlayerCommand> onHeartbeat(PlayerCommand.Heartbeat msg) {
        this.lastActiveTime = msg.timestamp();
        // 心跳响应
        return this;
    }

    /**
     * 加载玩家数据
     */
    private void loadPlayerData() {
        try {
            // 使用 ORM 加载玩家数据
            IEntityCaches caches = OrmContext.getEntityCaches(User.class);
            User user = (User) caches.load(playerId);

            if (user != null) {
                log.info("玩家数据加载成功: playerId={}, name={}, level=({}, {})",
                        playerId, playerName);

                // 发布登录事件
                String remoteAddr = "unknown";
                EventManager.getInstance().publish(new PlayerLoginEvent(
                        playerId,
                        playerName,
                        remoteAddr,
                        System.currentTimeMillis()
                ));
            }
        } catch (Exception e) {
            log.error("加载玩家数据失败: playerId={}", playerId, e);
        }
    }

    /**
     * 保存玩家数据
     */
    @SuppressWarnings("unchecked")
    private void savePlayerData() {
        try {
            IEntityCaches caches = OrmContext.getEntityCaches(User.class);
            User user = (User) caches.load(playerId);
            if (user != null) {
                // 使用 save() 方法（upsert）
                caches.save(user);
                log.debug("玩家数据保存成功: playerId={}", playerId);
            }
        } catch (Exception e) {
            log.error("保存玩家数据失败: playerId={}", playerId, e);
        }
    }

    /**
     * 更新活跃时间
     */
    private void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 玩家升级
     */
    private void levelUp() {
        int oldLevel = this.level;
        this.level++;

        log.info("玩家升级: playerId={}, {} -> {}", playerId, oldLevel, this.level);

        // 发布升级事件
        EventManager.getInstance().publish(new PlayerLevelUpEvent(
                playerId,
                playerName,
                oldLevel,
                this.level
        ));
    }

    /**
     * 获取玩家名称
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * 检查是否在线
     */
    public boolean isOnline() {
        return session != null;
    }
}
