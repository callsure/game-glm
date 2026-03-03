package com.game.db.accessor;

import com.game.db.model.entity.IEntity;
import com.game.utils.CollUtil;
import com.game.utils.StrUtil;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.client.model.ReplaceOptions;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

/**
 * MongoDB 数据访问实现类
 * 实现 IAccessor 接口，提供具体的 MongoDB 操作
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于 MongoDB 数据访问
 * - 依赖注入：通过 OrmManager 获取 MongoCollection
 * - 批量操作优化：使用 bulkWrite 提高性能
 *
 * @author Harleysama
 */
@Slf4j
public class MongoAccessor implements IAccessor {

    /**
     * 获取集合的方法引用（由 OrmManager 提供）
     */
    private final java.util.function.Function<Class<? extends IEntity<?>>, MongoCollection<? extends IEntity<?>>> getCollectionFunction;

    public MongoAccessor(java.util.function.Function<Class<? extends IEntity<?>>, MongoCollection<? extends IEntity<?>>> getCollectionFunction) {
        this.getCollectionFunction = getCollectionFunction;
    }

    /**
     * 获取 MongoDB 集合
     *
     * @param entityClazz 实体类型
     * @param <E>         实体类型
     * @return MongoCollection
     */
    @SuppressWarnings("unchecked")
    private <E extends IEntity<?>> MongoCollection<E> getCollection(Class<E> entityClazz) {
        return (MongoCollection<E>) getCollectionFunction.apply(entityClazz);
    }

    @Override
    public <E extends IEntity<?>> boolean insert(E entity) {
        Class<E> entityClazz = (Class<E>) entity.getClass();
        MongoCollection<E> collection = getCollection(entityClazz);
        InsertOneResult result = collection.insertOne(entity);
        boolean success = result.getInsertedId() != null;
        if (success) {
            log.debug("插入实体成功: collection={}, id={}", getCollectionName(entityClazz), entity.id());
        }
        return success;
    }

    @Override
    public <E extends IEntity<?>> void batchInsert(List<E> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        Class<E> entityClazz = (Class<E>) entities.get(0).getClass();
        MongoCollection<E> collection = getCollection(entityClazz);
        collection.insertMany(entities);
        log.debug("批量插入实体成功: collection={}, count={}", getCollectionName(entityClazz), entities.size());
    }

    @Override
    public <E extends IEntity<?>> boolean update(E entity) {
        try {
            Class<E> entityClazz = (Class<E>) entity.getClass();
            MongoCollection<E> collection = getCollection(entityClazz);

            Bson filter = eq("_id", entity.id());

            UpdateResult result = collection.replaceOne(filter, entity);
            if (result.getModifiedCount() <= 0) {
                log.warn("更新失败: collection={}, id={}, 数据库中不存在或数据相同",
                        getCollectionName(entityClazz), entity.id());
                return false;
            }
            log.debug("更新实体成功: collection={}, id={}", getCollectionName(entityClazz), entity.id());
            return true;
        } catch (Throwable t) {
            log.error("更新实体异常", t);
        }
        return false;
    }

    @Override
    public <E extends IEntity<?>> void batchUpdate(List<E> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }

        try {
            Class<E> entityClazz = (Class<E>) entities.get(0).getClass();
            MongoCollection<E> collection = getCollection(entityClazz);

            List<ReplaceOneModel<E>> batchList = entities.stream()
                    .map(it -> new ReplaceOneModel<E>(eq("_id", it.id()), it))
                    .collect(Collectors.toList());

            BulkWriteResult result = collection.bulkWrite(batchList, new BulkWriteOptions().ordered(false));
            if (result.getModifiedCount() != entities.size()) {
                log.warn("批量更新部分失败: collection={}, 期望更新={}, 实际更新={}",
                        getCollectionName(entityClazz), entities.size(), result.getModifiedCount());
            } else {
                log.debug("批量更新实体成功: collection={}, count={}", getCollectionName(entityClazz), entities.size());
            }
        } catch (Throwable t) {
            log.error("批量更新实体异常", t);
        }
    }

    @Override
    public <E extends IEntity<?>> boolean delete(E entity) {
        Class<E> entityClazz = (Class<E>) entity.getClass();
        MongoCollection<E> collection = getCollection(entityClazz);
        DeleteResult result = collection.deleteOne(eq("_id", entity.id()));
        boolean success = result.getDeletedCount() > 0;
        if (success) {
            log.debug("删除实体成功: collection={}, id={}", getCollectionName(entityClazz), entity.id());
        }
        return success;
    }

    @Override
    public <E extends IEntity<?>> boolean delete(Object pk, Class<E> entityClazz) {
        MongoCollection<E> collection = getCollection(entityClazz);
        DeleteResult result = collection.deleteOne(eq("_id", pk));
        boolean success = result.getDeletedCount() > 0;
        if (success) {
            log.debug("根据主键删除实体成功: collection={}, id={}", getCollectionName(entityClazz), pk);
        }
        return success;
    }

    @Override
    public <E extends IEntity<?>> void batchDelete(List<E> entities) {
        if (CollUtil.isEmpty(entities)) {
            return;
        }
        Class<E> entityClazz = (Class<E>) entities.get(0).getClass();
        MongoCollection<E> collection = getCollection(entityClazz);
        List<?> ids = entities.stream().map(IEntity::id).collect(Collectors.toList());
        collection.deleteMany(in("_id", ids));
        log.debug("批量删除实体成功: collection={}, count={}", getCollectionName(entityClazz), entities.size());
    }

    @Override
    public <E extends IEntity<?>> void batchDelete(List<?> pks, Class<E> entityClazz) {
        MongoCollection<E> collection = getCollection(entityClazz);
        collection.deleteMany(in("_id", pks));
        log.debug("根据主键列表批量删除成功: collection={}, count={}", getCollectionName(entityClazz), pks.size());
    }

    @Override
    public <E extends IEntity<?>> E load(Object pk, Class<E> entityClazz) {
        MongoCollection<E> collection = getCollection(entityClazz);
        List<E> result = new ArrayList<>(1);
        collection.find(eq("_id", pk)).forEach(result::add);
        if (CollUtil.isEmpty(result)) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public <E extends IEntity<?>> boolean save(E entity) {
        try {
            Class<E> entityClazz = (Class<E>) entity.getClass();
            MongoCollection<E> collection = getCollection(entityClazz);

            Bson filter = eq("_id", entity.id());

            // 使用 upsert 选项：存在则更新，不存在则插入
            ReplaceOptions options = new ReplaceOptions().upsert(true);

            com.mongodb.client.result.UpdateResult result = collection.replaceOne(filter, entity, options);

            boolean success = result.getModifiedCount() > 0 || result.getUpsertedId() != null;
            if (success) {
                if (result.getUpsertedId() != null) {
                    log.debug("保存实体（插入）成功: collection={}, id={}", getCollectionName(entityClazz), entity.id());
                } else {
                    log.debug("保存实体（更新）成功: collection={}, id={}", getCollectionName(entityClazz), entity.id());
                }
            }
            return success;
        } catch (Throwable t) {
            log.error("保存实体异常", t);
        }
        return false;
    }

    @Override
    public <E extends IEntity<?>> E findByIndex(String indexField, Object value, Class<E> entityClazz) {
        MongoCollection<E> collection = getCollection(entityClazz);
        List<E> result = new ArrayList<>(1);
        collection.find(eq(indexField, value)).forEach(result::add);
        if (CollUtil.isEmpty(result)) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public <E extends IEntity<?>> List<E> findListByIndex(String indexField, Object value, Class<E> entityClazz) {
        MongoCollection<E> collection = getCollection(entityClazz);
        List<E> result = new ArrayList<>();
        collection.find(eq(indexField, value)).forEach(result::add);
        return result;
    }

    /**
     * 获取集合名称（用于日志）
     *
     * @param entityClazz 实体类型
     * @return 集合名称
     */
    private String getCollectionName(Class<?> entityClazz) {
        return StrUtil.removeSufAndLowerFirst(entityClazz.getSimpleName(), "Entity");
    }
}
