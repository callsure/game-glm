package com.game.net;

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * Protobuf 消息编码器
 * 将 Protobuf 消息对象编码为 ProtocolPacket
 *
 * @author Harleysama
 */
@Slf4j
public class ProtocolMessageEncoder {

    /**
     * 编码消息
     *
     * @param messageType 消息类型
     * @param message     Protobuf 消息对象
     * @return 协议数据包
     */
    public static ProtocolPacket encode(int messageType, Message message) {
        if (message == null) {
            log.warn("消息对象为空，返回空数据包");
            return ProtocolPacket.create(messageType, null);
        }

        byte[] data = message.toByteArray();
        return ProtocolPacket.create(messageType, data);
    }

    /**
     * 编码消息（简化版本，自动获取消息类型）
     */
    public static ProtocolPacket encode(Message message) {
        // 注意：实际使用时需要维护 messageType 的映射关系
        // 这里简化处理，调用方需要指定类型
        throw new UnsupportedOperationException("请使用 encode(int messageType, Message message) 方法");
    }
}
