package com.game.net;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 会话对象
 * 封装客户端连接信息
 *
 * @author Harleysama
 */
@Data
public class Session {

    /**
     * 网络通道
     */
    private final Channel channel;

    /**
     * 用户ID（登录后绑定）
     */
    private Long userId;

    /**
     * 角色ID（选择角色后绑定）
     */
    private Long roleId;

    /**
     * 最后活跃时间
     */
    private long lastActiveTime;

    /**
     * 消息接收计数
     */
    private final AtomicLong receiveCount = new AtomicLong(0);

    /**
     * 消息发送计数
     */
    private final AtomicLong sendCount = new AtomicLong(0);

    public Session(Channel channel) {
        this.channel = channel;
        this.lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 更新活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 增加接收计数
     */
    public long incrementReceiveCount() {
        return receiveCount.incrementAndGet();
    }

    /**
     * 增加发送计数
     */
    public long incrementSendCount() {
        return sendCount.incrementAndGet();
    }

    /**
     * 发送消息
     */
    public void send(ProtocolPacket packet) {
        if (channel.isActive()) {
            channel.writeAndFlush(packet);
            incrementSendCount();
        }
    }

    /**
     * 是否已登录
     */
    public boolean isLogged() {
        return userId != null;
    }

    /**
     * 是否已选择角色
     */
    public boolean hasRole() {
        return roleId != null;
    }
}
