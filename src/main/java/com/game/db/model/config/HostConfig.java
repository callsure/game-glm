package com.game.db.model.config;

import java.util.Map;

/**
 * 数据库主机配置类
 * 包含数据库名称、用户名、密码、地址等信息
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于主机配置
 * - 工厂方法模式：提供 valueOf 静态工厂方法
 *
 * @author Harleysama
 */
public class HostConfig {

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    /**
     * 地址映射 (key: 主机标识, value: host:port)
     */
    private Map<String, String> address;

    /**
     * 静态工厂方法
     *
     * @param database     数据库名称
     * @param user         用户名
     * @param password     密码
     * @param addressMap   地址映射
     * @return HostConfig 实例
     */
    public static HostConfig valueOf(String database, String user, String password, Map<String, String> addressMap) {
        HostConfig config = new HostConfig();
        config.database = database;
        config.user = user;
        config.password = password;
        config.address = addressMap;
        return config;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }
}
