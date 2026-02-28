package com.game.handler;

import com.game.net.ProtocolPacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理器接口
 * 所有具体的消息处理器都需要实现此接口
 *
 * @author Harleysama
 */
public interface MessageHandler {

    /**
     * 处理消息
     *
     * @param ctx     Channel上下文
     * @param packet  协议数据包
     * @throws Exception 处理异常
     */
    void handle(ChannelHandlerContext ctx, ProtocolPacket packet) throws Exception;

    /**
     * 获取支持的消息类型
     *
     * @return 消息类型ID
     */
    int getMessageType();
}
