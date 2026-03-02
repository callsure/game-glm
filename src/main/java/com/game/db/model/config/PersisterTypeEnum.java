package com.game.db.model.config;

import com.game.db.cache.EntityCaches;
import com.game.db.cache.persister.IOrmPersister;
import com.game.db.model.vo.EntityDef;

/**
 * 持久化类型枚举
 * 定义支持的持久化策略类型
 * <p>
 * 支持的类型：
 * - QUEUE: 队列持久化（暂未实现）
 * - CRON: Cron 表达式定时持久化
 * - TIME: 定时间隔持久化
 * <p>
 * 设计原则：
 * - 策略模式：每种类型对应不同的持久化实现
 * - 开闭原则（OCP）：可扩展新的持久化类型
 * - 单一职责原则（SRP）：专注于持久化类型定义
 *
 * @author Harleysama
 */
public enum PersisterTypeEnum {

    /**
     * 队列持久化
     */
    QUEUE {
        @Override
        public String toString() {
            return "队列持久化";
        }

        @Override
        public IOrmPersister createPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches) {
            throw new UnsupportedOperationException("QUEUE 持久化器暂未实现");
        }
    },

    /**
     * Cron 表达式定时持久化
     */
    CRON {
        @Override
        public String toString() {
            return "Cron定时持久化";
        }

        @Override
        public IOrmPersister createPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches) {
            return new com.game.db.cache.persister.CronOrmPersister(entityDef, entityCaches);
        }
    },

    /**
     * 定时间隔持久化
     */
    TIME {
        @Override
        public String toString() {
            return "定时间隔持久化";
        }

        @Override
        public IOrmPersister createPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches) {
            return new com.game.db.cache.persister.TimeOrmPersister(entityDef, entityCaches);
        }
    };

    /**
     * 根据字符串获取持久化类型
     *
     * @param persisterType 持久化类型字符串
     * @return 持久化类型枚举
     * @throws IllegalArgumentException 无效的持久化类型
     */
    public static PersisterTypeEnum getPersisterType(String persisterType) {
        for (PersisterTypeEnum persister : values()) {
            if (persister.name().equalsIgnoreCase(persisterType)) {
                return persister;
            }
        }
        throw new IllegalArgumentException("无效的持久化类型[persisterType:" + persisterType + "]");
    }

    /**
     * 创建持久化器实例
     *
     * @param entityDef    实体定义
     * @param entityCaches 实体缓存
     * @return 持久器实例
     */
    public abstract IOrmPersister createPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches);
}
