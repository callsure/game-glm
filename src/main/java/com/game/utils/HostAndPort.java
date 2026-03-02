package com.game.utils;

import java.util.*;

/**
 * 主机和端口工具类
 * 解析和管理主机端口信息
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于主机端口解析
 * - 不可变对象：通过 valueOf 创建实例
 * <p>
 * 使用示例：
 * <pre>
 * HostAndPort hp = HostAndPort.valueOf("localhost:27017");
 * String host = hp.getHost(); // "localhost"
 * int port = hp.getPort();     // 27017
 * </pre>
 *
 * @author Harleysama
 */
public class HostAndPort {

    private final String host;
    private final int port;

    /**
     * 私有构造函数，使用 valueOf 创建实例
     */
    private HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 创建 HostAndPort 实例
     *
     * @param host 主机
     * @param port 端口
     * @return HostAndPort 实例
     */
    public static HostAndPort valueOf(String host, int port) {
        return new HostAndPort(host, port);
    }

    /**
     * 从字符串解析创建 HostAndPort 实例
     *
     * @param hostAndPort 格式: "host:port" 例如 "localhost:27017"
     * @return HostAndPort 实例
     */
    public static HostAndPort valueOf(String hostAndPort) {
        String[] split = hostAndPort.trim().split(StrUtil.COLON);
        if (split.length != 2) {
            throw new IllegalArgumentException("无效的主机端口格式: " + hostAndPort + "，期望格式: host:port");
        }
        return valueOf(split[0].trim(), Integer.parseInt(split[1].trim()));
    }

    /**
     * 从字符串列表解析创建 HostAndPort 列表
     *
     * @param hostAndPorts 格式: "host:port,host:port,host:port"
     * @return HostAndPort 列表
     */
    public static List<HostAndPort> toHostAndPortList(String hostAndPorts) {
        if (StrUtil.isEmpty(hostAndPorts)) {
            return Collections.emptyList();
        }

        String[] hostAndPortSplits = hostAndPorts.split(StrUtil.COMMA);
        List<HostAndPort> hostAndPortList = new ArrayList<>();
        for (String hostAndPort : hostAndPortSplits) {
            hostAndPortList.add(valueOf(hostAndPort));
        }
        return hostAndPortList;
    }

    /**
     * 从集合转换创建 HostAndPort 列表
     *
     * @param list 主机端口字符串集合
     * @return HostAndPort 列表
     */
    public static List<HostAndPort> toHostAndPortList(Collection<String> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<HostAndPort> hostAndPortList = new ArrayList<>();
        list.forEach(it -> hostAndPortList.addAll(toHostAndPortList(it)));
        return hostAndPortList;
    }

    /**
     * 转换为字符串格式 "host:port"
     *
     * @return 字符串格式
     */
    public String toHostAndPortStr() {
        return StrUtil.format("{}:{}", this.host.trim(), this.port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostAndPort that = (HostAndPort) o;
        return port == that.port && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return StrUtil.format("[{}]", toHostAndPortStr());
    }
}
