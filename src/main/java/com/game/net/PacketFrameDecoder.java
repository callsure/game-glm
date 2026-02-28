package com.game.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 数据包帧解码器
 * 解析协议格式: [数据长度(4字节)][消息类型(4字节)][数据体]
 *
 * @author Harleysama
 */
@Slf4j
public class PacketFrameDecoder extends ByteToMessageDecoder {

    private static final int HEADER_SIZE = 8; // 长度(4) + 类型(4)
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 最大1MB

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的字节读取头部
        if (in.readableBytes() < HEADER_SIZE) {
            return;
        }

        // 标记当前读取位置
        in.markReaderIndex();

        // 读取数据长度
        int dataLength = in.readInt();

        // 安全检查：防止恶意大包攻击
        if (dataLength <= 0 || dataLength > MAX_FRAME_LENGTH) {
            log.error("检测到非法数据包长度: {}, 连接将被关闭!", dataLength);
            ctx.close();
            return;
        }

        // 读取消息类型
        int messageType = in.readInt();

        // 检查数据体是否完整
        if (in.readableBytes() < dataLength) {
            // 数据不完整，重置读取位置，等待更多数据
            in.resetReaderIndex();
            return;
        }

        // 读取数据体
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 封装成协议数据包
        ProtocolPacket packet = new ProtocolPacket(messageType, data);
        out.add(packet);

        log.debug("解码数据包: 类型={}, 长度={}", messageType, dataLength);
    }
}
