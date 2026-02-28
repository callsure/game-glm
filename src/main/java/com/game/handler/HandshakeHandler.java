package com.game.handler;

import com.game.net.Session;
import com.game.protocol.generated.CommonProto;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 握手处理器
 * 处理客户端握手请求，建立会话
 *
 * @author Harleysama
 */
@Slf4j
public class HandshakeHandler extends AbstractMessageHandler<CommonProto.Handshake> {

    @Override
    protected com.google.protobuf.Parser<CommonProto.Handshake> getParser() {
        return CommonProto.Handshake.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, CommonProto.Handshake message) throws Exception {
        log.info("客户端握手: version={}, deviceId={}, platform={}",
                message.getVersion(), message.getDeviceId(), message.getPlatform());

        // 生成会话密钥
        String sessionKey = UUID.randomUUID().toString();

        // 构建握手响应
        CommonProto.HandshakeResponse response = CommonProto.HandshakeResponse.newBuilder()
                .setServerTime(System.currentTimeMillis())
                .setSessionKey(sessionKey)
                .setHeartbeatInterval(60) // 60秒心跳间隔
                .build();

        sendResponse(ctx, CommonProto.MessageType.HANDSHAKE_RESP_VALUE, response);

        log.info("握手成功: sessionKey={}", sessionKey);
    }

    @Override
    public int getMessageType() {
        return CommonProto.MessageType.HANDSHAKE_VALUE;
    }
}
