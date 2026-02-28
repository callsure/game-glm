package com.game.handler;

import com.game.net.Session;
import com.game.protocol.generated.CommonProto;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳处理器
 * 处理客户端心跳包
 *
 * @author Harleysama
 */
@Slf4j
@GameHandler(messageType = CommonProto.MessageType.HEARTBEAT_VALUE)
public class HeartbeatHandler extends AbstractMessageHandler<CommonProto.Heartbeat> {

    @Override
    protected com.google.protobuf.Parser<CommonProto.Heartbeat> getParser() {
        return CommonProto.Heartbeat.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, CommonProto.Heartbeat message) throws Exception {
        log.debug("收到心跳: sessionId={}", session.getUserId());

        // 原样返回心跳包
        sendResponse(ctx, CommonProto.MessageType.HEARTBEAT_VALUE, message);
    }
}
