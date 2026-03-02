package com.game.db.cache.persister;

import com.game.db.model.vo.EntityDef;
import com.game.db.cache.EntityCaches;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时间隔持久化器
 * 按固定时间间隔持久化缓存数据
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于定时间隔持久化
 * - 策略模式：实现 IOrmPersister 接口
 *
 * @author Harleysama
 */
@Slf4j
public class TimeOrmPersister extends AbstractOrmPersister {

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
    public TimeOrmPersister(EntityDef entityDef, EntityCaches<?, ?> entityCaches) {
        super(entityDef, entityCaches);
        // 从配置中解析间隔时间，默认 60 秒
        this.intervalSeconds = parseInterval(entityDef.getPersisterStrategy().getConfig());
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "persister-time-" + entityDef.getClazz().getSimpleName());
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (running) {
                    entityCaches.persistAll();
                }
            } catch (Exception e) {
                log.error("持久化器执行异常: entity={}", entityDef.getClazz().getSimpleName(), e);
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        log.info("定时间隔持久化器已启动: entity={}, interval={}s",
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
     * 解析间隔时间
     *
     * @param config 配置字符串（数字表示秒数）
     * @return 间隔秒数
     */
    private long parseInterval(String config) {
        try {
            return Long.parseLong(config);
        } catch (NumberFormatException e) {
            log.warn("无效的间隔配置，使用默认值 60 秒: config={}", config);
            return 60;
        }
    }
}
