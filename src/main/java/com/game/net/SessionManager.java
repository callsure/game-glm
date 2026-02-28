package com.game.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器
 * 管理所有客户端连接会话
 *
 * @author Harleysama
 */
@Slf4j
public class SessionManager {

    /**
     * Channel ID -> Session 映射
     */
    private static final Map<ChannelId, Session> sessions = new ConcurrentHashMap<>();

    /**
     * User ID -> Channel ID 映射（用于快速查找用户）
     */
    private static final Map<Long, ChannelId> userChannelMap = new ConcurrentHashMap<>();

    /**
     * 添加会话
     */
    public static void addSession(Channel channel) {
        Session session = new Session(channel);
        sessions.put(channel.id(), session);
        log.debug("添加会话: channelId={}", channel.id());
    }

    /**
     * 移除会话
     */
    public static void removeSession(Channel channel) {
        Session session = sessions.remove(channel.id());
        if (session != null && session.getUserId() != null) {
            userChannelMap.remove(session.getUserId());
        }
        log.debug("移除会话: channelId={}", channel.id());
    }

    /**
     * 获取会话
     */
    public static Session getSession(Channel channel) {
        return sessions.get(channel.id());
    }

    /**
     * 根据用户ID获取会话
     */
    public static Session getSessionByUserId(Long userId) {
        ChannelId channelId = userChannelMap.get(userId);
        if (channelId == null) {
            return null;
        }
        return sessions.get(channelId);
    }

    /**
     * 绑定用户
     */
    public static void bindUser(Channel channel, Long userId) {
        Session session = sessions.get(channel.id());
        if (session != null) {
            // 先解绑旧用户
            if (session.getUserId() != null) {
                userChannelMap.remove(session.getUserId());
            }
            // 绑定新用户
            session.setUserId(userId);
            userChannelMap.put(userId, channel.id());
            log.info("绑定用户: userId={}, channelId={}", userId, channel.id());
        }
    }

    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return sessions.size();
    }

    /**
     * 广播消息给所有在线用户
     */
    public static void broadcast(ProtocolPacket packet) {
        sessions.values().forEach(session -> {
            if (session.getChannel().isActive()) {
                session.getChannel().writeAndFlush(packet);
            }
        });
    }
}
