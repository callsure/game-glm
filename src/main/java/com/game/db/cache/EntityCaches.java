package com.game.db.cache;

import com.game.db.OrmContext;
import com.game.db.cache.persister.PNode;
import com.game.db.model.entity.IEntity;
import com.game.db.model.vo.EntityDef;
import com.game.utils.AssertUtil;
import com.game.utils.CollUtil;
import com.game.utils.DateUtil;
import com.game.utils.StrUtil;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * 实体缓存实现类
 * 基于 Caffeine 实现的高性能缓存
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于实体缓存管理
 * - 依赖注入：通过 OrmContext 获取依赖
 * - 自动持久化：支持定时和 Cron 两种持久化策略
 *
 * @param <PK> 主键类型
 * @param <E>  实体类型
 * @author Harleysama
 */
@Slf4j
public class EntityCaches<PK extends Comparable<PK>, E extends IEntity<PK>> implements IEntityCaches<PK, E> {

    /**
     * 批量操作大小
     */
    private static final int BATCH_SIZE = 512;

    private final EntityDef entityDef;

    private final LoadingCache<PK, PNode<E>> cache;

    /**
     * 构造方法
     *
     * @param entityDef 实体定义
     */
    @SuppressWarnings("unchecked")
    public EntityCaches(EntityDef entityDef) {
        this.entityDef = entityDef;

        // 构建 Caffeine 缓存
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(entityDef.getExpireMillisecond(), TimeUnit.MILLISECONDS)
                .maximumSize(entityDef.getCacheSize())
                .initialCapacity(entityDef.getCacheSize() / 4)
                .removalListener((RemovalListener<PK, PNode<E>>) (pk, pnode, removalCause) -> {
                    if (pnode == null) {
                        return;
                    }
                    // 缓存失效之前，将数据写入数据库
                    if (pnode.getWriteToDbTime() == pnode.getModifiedTime()) {
                        return;
                    }
                    E entity = pnode.getEntity();
                    writeToDatabaseOnEvict(entity);
                })
                .build(new CacheLoader<PK, PNode<E>>() {
                    @Override
                    public @Nullable PNode<E> load(@NonNull PK pk) {
                        E entity = (E) OrmContext.getAccessor().load(pk, (Class<IEntity<?>>) entityDef.getClazz());

                        // 如果数据库中不存在则给一个默认值
                        if (entity == null) {
                            entity = (E) entityDef.newEntity(pk);
                        }
                        return new PNode<>(entity);
                    }
                });

        // 启动持久化器
        entityDef.getPersisterStrategy().getType().createPersister(entityDef, this).start();

        log.info("实体缓存已创建: entity={}, cacheSize={}, expireMs={}",
                entityDef.getClazz().getSimpleName(),
                entityDef.getCacheSize(),
                entityDef.getExpireMillisecond());
    }

    @Override
    public E load(PK pk) {
        AssertUtil.notNull(pk, "主键不能为空");
        try {
            return cache.get(pk).getEntity();
        } catch (Exception e) {
            log.error("加载缓存异常: entity={}, pk={}",
                    entityDef.getClazz().getSimpleName(), pk, e);
        } catch (Throwable t) {
            log.error("加载缓存错误: entity={}, pk={}",
                    entityDef.getClazz().getSimpleName(), pk, t);
        }

        // 返回默认值
        log.warn("无法加载缓存，返回默认值: entity={}, pk={}",
                entityDef.getClazz().getSimpleName(), pk);
        E entity = (E) entityDef.newEntity(pk);
        PNode<E> pnode = new PNode<>(entity);
        cache.put(pk, pnode);
        return entity;
    }

    @Override
    public void update(E entity) {
        AssertUtil.notNull(entity, "实体不能为空");

        PNode<E> currentPnode = cache.getIfPresent(entity.id());

        if (currentPnode == null) {
            currentPnode = new PNode<>(entity);
            cache.put(entity.id(), currentPnode);
        }

        // 检测并发写风险
        long pnodeThreadId = currentPnode.getThreadId();
        long currentThreadId = Thread.currentThread().getId();
        if (pnodeThreadId != currentThreadId) {
            if (pnodeThreadId == 0) {
                currentPnode.setThreadId(currentThreadId);
            } else {
                log.warn("[{}][id:{}]有并发写风险，第一次更新的线程[id:{}]，第二次更新的线程[id:{}]",
                        entity.getClass().getSimpleName(), entity.id(), pnodeThreadId, currentThreadId);
            }
        }

        // 更新修改时间（加100防止时间戳相同）
        currentPnode.setModifiedTime(DateUtil.getSecondLevelMillis() + 100);
    }

    @Override
    public void invalidate(PK pk) {
        AssertUtil.notNull(pk, "主键不能为空");
        cache.invalidate(pk);
        log.debug("缓存已失效: entity={}, pk={}",
                entityDef.getClazz().getSimpleName(), pk);
    }

    @Override
    public void persistAll() {
        try {
            Collection<@NonNull PNode<E>> allPnodes = cache.asMap().values();

            if (allPnodes.isEmpty()) {
                return;
            }

            List<E> updateList = new ArrayList<>();
            long currentTime = DateUtil.getSecondLevelMillis();

            for (PNode<E> pnode : allPnodes) {
                E entity = pnode.getEntity();
                // 检查是否需要持久化
                if (pnode.getModifiedTime() != pnode.getWriteToDbTime()) {
                    pnode.setWriteToDbTime(currentTime);
                    pnode.setModifiedTime(currentTime);
                    updateList.add(entity);
                    continue;
                }

                // 检查是否过期
                if (currentTime - pnode.getModifiedTime() >= entityDef.getExpireMillisecond()) {
                    invalidate(pnode.getEntity().id());
                }
            }

            // 执行批量更新
            if (updateList.isEmpty()) {
                return;
            }

            batchUpdate(updateList);
            updateList.clear();

        } catch (Exception e) {
            log.error("持久化异常: entity={}", entityDef.getClazz().getSimpleName(), e);
        } catch (Throwable t) {
            log.error("持久化错误: entity={}", entityDef.getClazz().getSimpleName(), t);
        }
    }

    @Override
    public List<E> allPresentCaches() {
        Collection<@NonNull PNode<E>> allPnodes = cache.asMap().values();

        if (allPnodes.isEmpty()) {
            return List.of();
        }
        return allPnodes.stream()
                .map(PNode::getEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void forEach(BiConsumer<PK, E> biConsumer) {
        cache.asMap().forEach((pk, pNode) -> biConsumer.accept(pk, pNode.getEntity()));
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public String recordStatus() {
        var stats = cache.stats();
        return StrUtil.format(
                "数据库[{}]缓存命中率[hitRate:{}]，命中次数[hitCount:{}]，加载次数[loadCount:{}]，缓存项被回收的总数[evictionCount:{}]",
                entityDef.getClazz().getSimpleName(),
                stats.hitRate(),
                stats.hitCount(),
                stats.loadCount(),
                stats.evictionCount()
        );
    }

    @Override
    public boolean save(E entity) {
        AssertUtil.notNull(entity, "实体不能为空");

        try {
            // 先执行数据库 upsert 操作
            boolean dbSuccess = OrmContext.getAccessor().save(entity);

            if (!dbSuccess) {
                log.warn("保存实体到数据库失败: entity={}, id={}",
                        entityDef.getClazz().getSimpleName(), entity.id());
                return false;
            }

            // 数据库操作成功后，更新缓存
            PNode<E> currentPnode = cache.getIfPresent(entity.id());

            if (currentPnode == null) {
                // 缓存中不存在，创建新的缓存节点
                currentPnode = new PNode<>(entity);
                cache.put(entity.id(), currentPnode);
            } else {
                // 缓存中已存在，更新实体数据
                currentPnode.setEntity(entity);
                // 更新修改时间
                currentPnode.setModifiedTime(DateUtil.getSecondLevelMillis());
            }

            log.debug("保存实体成功（数据库+缓存）: entity={}, id={}",
                    entityDef.getClazz().getSimpleName(), entity.id());
            return true;

        } catch (Exception e) {
            log.error("保存实体异常: entity={}, id={}",
                    entityDef.getClazz().getSimpleName(), entity.id(), e);
        } catch (Throwable t) {
            log.error("保存实体错误: entity={}, id={}",
                    entityDef.getClazz().getSimpleName(), entity.id(), t);
        }

        return false;
    }

    // ==================== 私有方法 ====================

    /**
     * 批量更新实体
     */
    @SuppressWarnings("unchecked")
    private void batchUpdate(List<E> updateList) {
        if (CollUtil.isEmpty(updateList)) {
            return;
        }

        // 分批处理
        int totalPages = (updateList.size() + BATCH_SIZE - 1) / BATCH_SIZE;

        for (int page = 0; page < totalPages; page++) {
            int fromIndex = page * BATCH_SIZE;
            int toIndex = Math.min(fromIndex + BATCH_SIZE, updateList.size());
            List<E> currentPageList = updateList.subList(fromIndex, toIndex);

            try {
                MongoCollection<E> collection = OrmContext.getOrmManager()
                        .getCollection((Class<E>) entityDef.getClazz());

                var batchList = currentPageList.stream()
                        .map(this::createReplaceModel)
                        .collect(Collectors.toList());

                var result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));

                if (result.getModifiedCount() != currentPageList.size()) {
                    log.warn("批量更新部分失败: entity={}, 期望={}, 实际={}",
                            entityDef.getClazz().getSimpleName(),
                            currentPageList.size(),
                            result.getModifiedCount());
                }
            } catch (Throwable t) {
                log.error("批量更新异常: entity={}, size={}",
                        entityDef.getClazz().getSimpleName(), currentPageList.size(), t);
            }
        }
    }

    /**
     * 创建替换模型
     */
    private ReplaceOneModel<E> createReplaceModel(E entity) {
        long version = entity.gvs();
        entity.svs(version + 1);

        Bson filter = eq("_id", entity.id());

        return new ReplaceOneModel<>(filter, entity);
    }

    /**
     * 缓存失效时写入数据库
     */
    @SuppressWarnings("unchecked")
    private void writeToDatabaseOnEvict(E entity) {
        try {
            MongoCollection<E> collection = OrmContext.getOrmManager()
                    .getCollection((Class<E>) entityDef.getClazz());

            long version = entity.gvs();
            entity.svs(version + 1);

            Bson filter = eq("_id", entity.id());
            UpdateResult result = collection.replaceOne(filter, entity);

            if (result.getModifiedCount() <= 0) {
                log.warn("缓存失效时更新失败: entity={}, id={}",
                        entityDef.getClazz().getSimpleName(), entity.id());
            }
        } catch (Exception e) {
            log.error("缓存失效时写入数据库异常: entity={}, id={}",
                    entityDef.getClazz().getSimpleName(), entity.id(), e);
        }
    }
}
