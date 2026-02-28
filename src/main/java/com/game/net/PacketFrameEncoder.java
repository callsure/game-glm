package com.game.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据包帧编码器
 * 编码协议格式: [数据长度(4字节)][消息类型(4字节)][数据体]
 *
 * @author Harleysama
 */
@Slf4j
public class PacketFrameEncoder extends MessageToByteEncoder<ProtocolPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolPacket packet, ByteBuf out) throws Exception {
        byte[] data = packet.getData();
        int dataLength = data == null ? 0 : data.length;

        // 写入数据长度
        out.writeInt(dataLength);

        // 写入消息类型
        out.writeInt(packet.getMessageType());

        // 写入数据体
        if (dataLength > 0) {
            out.writeBytes(data);
        }

        log.debug("编码数据包: 类型={}, 长度={}", packet.getMessageType(), dataLength);
    }
}
