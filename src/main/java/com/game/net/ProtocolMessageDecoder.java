package com.game.net;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

/**
 * Protobuf 消息解码器
 * 将 ProtocolPacket 解析为具体的 Protobuf 消息对象
 *
 * @author Harleysama
 */
@Slf4j
public class ProtocolMessageDecoder {

    /**
     * 解析消息
     *
     * @param packet 协议数据包
     * @param parser Protobuf 解析器
     * @param <T>    消息类型
     * @return 解析后的消息对象
     */
    public static <T extends Message> T decode(ProtocolPacket packet, Parser<T> parser) {
        if (packet == null || packet.getData() == null) {
            log.warn("数据包为空，无法解析");
            return null;
        }

        try {
            return parser.parseFrom(packet.getData());
        } catch (InvalidProtocolBufferException e) {
            log.error("消息解析失败: type={}, error={}", packet.getMessageType(), e.getMessage());
            return null;
        }
    }

    /**
     * 解析消息（带默认值）
     */
    public static <T extends Message> T decode(ProtocolPacket packet, Parser<T> parser, T defaultInstance) {
        T message = decode(packet, parser);
        return message != null ? message : defaultInstance;
    }
}
