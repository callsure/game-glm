package com.game.db.cache.persister;

import com.game.db.cache.EntityCaches;
import com.game.db.model.vo.EntityDef;
import lombok.extern.slf4j.Slf4j;

/**
 * ORM 持久化器抽象基类
 * 提供持久化器的通用实现
 * <p>
 * 设计原则：
 * - 模板方法模式：定义执行流程，子类实现具体细节
 * - 单一职责原则（SRP）：专注于持久化管理
 *
 * @author Harleysama
 */
@Slf4j
public abstract class AbstractOrmPersister implements IOrmPersister {

    /**
     * 实体定义
     */
    protected final EntityDef entityDef;

    /**
     * 实体缓存
     */
    protected final EntityCaches<?, ?> entityCaches;

    /**
     * 是否运行中
     */
    protected volatile boolean running = false;

    /**
     * 构造方法
     *
     * @param entityDef    实体定义
     * @param entityCaches 实体缓存
     */
    public AbstractOrmPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches) {
        this.entityDef = entityDef;
        this.entityCaches = entityCaches;
    }

    @Override
    public void stop() {
        running = false;
        log.info("持久化器已停止: entity={}", entityDef.getClazz().getSimpleName());
    }

    /**
     * 是否运行中
     *
     * @return true 表示运行中
     */
    public boolean isRunning() {
        return running;
    }
}
