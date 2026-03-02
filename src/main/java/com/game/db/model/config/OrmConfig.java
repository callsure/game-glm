package com.game.db.model.config;

import java.util.List;

/**
 * ORM 配置类
 * 包含数据库连接、实体包扫描、缓存策略、持久化策略等配置
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于配置管理
 * - 开闭原则（OCP）：通过配置扩展，无需修改代码
 *
 * @author Harleysama
 */
public class OrmConfig {

    /**
     * 配置ID
     */
    private String id;

    /**
     * 实体类扫描包路径
     */
    private String entityPackage;

    /**
     * 数据库主机配置
     */
    private HostConfig host;

    /**
     * 缓存策略列表
     */
    private List<CacheStrategy> caches;

    /**
     * 持久化策略列表
     */
    private List<PersisterStrategy> persisters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityPackage() {
        return entityPackage;
    }

    public void setEntityPackage(String entityPackage) {
        this.entityPackage = entityPackage;
    }

    public HostConfig getHost() {
        return host;
    }

    public void setHost(HostConfig host) {
        this.host = host;
    }

    public List<CacheStrategy> getCaches() {
        return caches;
    }

    public void setCaches(List<CacheStrategy> caches) {
        this.caches = caches;
    }

    public List<PersisterStrategy> getPersisters() {
        return persisters;
    }

    public void setPersisters(List<PersisterStrategy> persisters) {
        this.persisters = persisters;
    }
}
