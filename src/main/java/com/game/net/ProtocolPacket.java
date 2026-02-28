package com.game.net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议数据包
 * 封装消息类型和数据体
 *
 * @author Harleysama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolPacket {

    /**
     * 消息类型
     */
    private int messageType;

    /**
     * 消息数据 (序列化后的 Protobuf 字节)
     */
    private byte[] data;

    /**
     * 创建数据包
     */
    public static ProtocolPacket create(int messageType, byte[] data) {
        return new ProtocolPacket(messageType, data);
    }

    /**
     * 创建空数据包
     */
    public static ProtocolPacket create(int messageType) {
        return new ProtocolPacket(messageType, null);
    }
}
