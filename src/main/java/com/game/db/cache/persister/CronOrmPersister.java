package com.game.db.cache.persister;

import com.game.db.model.vo.EntityDef;
import com.game.db.cache.EntityCaches;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cron 定时持久化器
 * 按 Cron 表达式定义的规则持久化缓存数据
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于 Cron 定时持久化
 * - 策略模式：实现 IOrmPersister 接口
 * <p>
 * 注意：当前实现简化为定时间隔，完整实现需要 Cron 解析库
 *
 * @author Harleysama
 */
@Slf4j
public class CronOrmPersister extends AbstractOrmPersister {

    /**
     * 定时线程池
     */
    private ScheduledExecutorService scheduler;

    /**
     * 持久化间隔（秒）
     */
    private final long intervalSeconds;

    /**
     * 构造方法
     *
     * @param entityDef    实体定义
     * @param entityCaches 实体缓存
     */
    public CronOrmPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches) {
        super(entityDef, entityCaches);
        // 从 Cron 表达式中解析间隔时间，简化实现，默认 60 秒
        this.intervalSeconds = parseCronInterval(entityDef.getPersisterStrategy().getConfig());
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "persister-cron-" + entityDef.getClazz().getSimpleName());
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (running) {
                    entityCaches.persistAll();
                }
            } catch (Exception e) {
                log.error("Cron 持久化器执行异常: entity={}", entityDef.getClazz().getSimpleName(), e);
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        log.info("Cron 定时持久化器已启动: entity={}, interval={}s",
                entityDef.getClazz().getSimpleName(), intervalSeconds);
    }

    @Override
    public void stop() {
        super.stop();
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 解析 Cron 表达式间隔
     * 简化实现，实际项目应使用 CronUtils 等库
     *
     * @param cronExpression Cron 表达式
     * @return 间隔秒数
     */
    private long parseCronInterval(String cronExpression) {
        // 简化实现：从 Cron 表达式中解析分钟间隔
        // 完整实现需要使用 quartz-cron 等库
        try {
            // 尝试解析为数字
            return Long.parseLong(cronExpression);
        } catch (NumberFormatException e) {
            log.warn("无效的 Cron 表达式，使用默认值 60 秒: cron={}", cronExpression);
            return 60;
        }
    }
}
