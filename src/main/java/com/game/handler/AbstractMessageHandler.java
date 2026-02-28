package com.game.handler;

import com.game.net.ProtocolPacket;
import com.game.net.Session;
import com.game.net.SessionManager;
import com.game.protocol.generated.CommonProto;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象消息处理器
 * 提供消息处理的基础功能实现
 *
 * @author Harleysama
 */
@Slf4j
public abstract class AbstractMessageHandler<T extends Message> implements MessageHandler {

    /**
     * 获取Protobuf解析器
     */
    protected abstract Parser<T> getParser();

    /**
     * 处理具体的业务逻辑
     */
    protected abstract void handle(ChannelHandlerContext ctx, Session session, T message) throws Exception;

    @Override
    public void handle(ChannelHandlerContext ctx, ProtocolPacket packet) throws Exception {
        // 获取会话
        Session session = SessionManager.getSession(ctx.channel());
        if (session == null) {
            log.warn("会话不存在，关闭连接");
            ctx.close();
            return;
        }

        // 更新活跃时间
        session.updateActiveTime();
        session.incrementReceiveCount();

        // 解析消息
        T message = parseMessage(packet);
        if (message == null) {
            sendError(ctx, CommonProto.ErrorCode.INVALID_PARAM, "消息格式错误");
            return;
        }

        // 处理业务逻辑
        handle(ctx, session, message);
    }

    /**
     * 解析消息
     */
    protected T parseMessage(ProtocolPacket packet) {
        try {
            return getParser().parseFrom(packet.getData());
        } catch (InvalidProtocolBufferException e) {
            log.error("消息解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送响应消息
     */
    protected void sendResponse(ChannelHandlerContext ctx, int messageType, Message message) {
        ProtocolPacket packet = new ProtocolPacket(messageType, message.toByteArray());
        ctx.writeAndFlush(packet);
    }

    /**
     * 发送错误消息
     */
    protected void sendError(ChannelHandlerContext ctx, CommonProto.ErrorCode code, String message) {
        CommonProto.Error error = CommonProto.Error.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();

        sendResponse(ctx, CommonProto.MessageType.ERROR_VALUE, error);
    }

    /**
     * 检查用户是否已登录
     */
    protected boolean checkLogin(Session session) {
        if (!session.isLogged()) {
            log.warn("用户未登录");
            return false;
        }
        return true;
    }
}
