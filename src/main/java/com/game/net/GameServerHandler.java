package com.game.net;

import com.game.handler.MessageHandler;
import com.game.handler.MessageHandlerManager;
import com.game.protocol.generated.CommonProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏服务器主处理器
 * 处理所有客户端消息的入口
 *
 * @author Harleysama
 */
@Slf4j
public class GameServerHandler extends SimpleChannelInboundHandler<ProtocolPacket> {

    private final MessageHandlerManager handlerManager;

    public GameServerHandler() {
        this.handlerManager = MessageHandlerManager.getInstance();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接: {}", ctx.channel().remoteAddress());
        // 可以在这里初始化会话信息
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端断开: {}", ctx.channel().remoteAddress());
        // 清理会话信息
        SessionManager.removeSession(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolPacket packet) throws Exception {
        log.debug("收到消息: type={}", packet.getMessageType());

        // 获取对应的消息处理器
        MessageHandler handler = handlerManager.getHandler(packet.getMessageType());
        if (handler == null) {
            log.warn("未找到消息处理器: type={}", packet.getMessageType());
            sendError(ctx, CommonProto.ErrorCode.UNKNOWN_ERROR, "未知的消息类型");
            return;
        }

        try {
            // 处理消息
            handler.handle(ctx, packet);
        } catch (Exception e) {
            log.error("消息处理异常: type={}, error={}", packet.getMessageType(), e.getMessage(), e);
            sendError(ctx, CommonProto.ErrorCode.SERVER_ERROR, "服务器内部错误");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.warn("客户端读超时，关闭连接: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("连接异常: {}, error={}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }

    /**
     * 发送错误消息
     */
    private void sendError(ChannelHandlerContext ctx, CommonProto.ErrorCode code, String message) {
        CommonProto.Error error = CommonProto.Error.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();

        ProtocolPacket packet = ProtocolMessageEncoder.encode(
                CommonProto.MessageType.ERROR_VALUE,
                error
        );

        ctx.writeAndFlush(packet);
    }
}
