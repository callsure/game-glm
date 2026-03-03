package com.game.db.dao;

import com.game.db.OrmContext;
import com.game.db.cache.IEntityCaches;
import com.game.db.model.entity.IEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * DAO 基类
 * 提供通用的数据访问方法，减少重复代码
 * <p>
 * 设计原则：
 * - DRY（Don't Repeat Yourself）：提取公共方法避免重复
 * - 模板方法模式：定义算法骨架，子类可扩展特定行为
 * - 泛型设计：支持不同实体类型的数据访问
 * <p>
 * 使用示例：
 * <pre>
 * public class UserDao extends BaseDao<Long, User> {
 *     public UserDao() {
 *         super(User.class);
 *     }
 *
 *     // 可添加特定于 User 的查询方法
 *     public User findByUsername(String username) {
 *         // 自定义查询逻辑
 *     }
 * }
 * </pre>
 *
 * @param <PK> 主键类型，必须可比较
 * @param <E>  实体类型
 * @author Harleysama
 */
@Slf4j
public abstract class BaseDao<PK extends Comparable<PK>, E extends IEntity<PK>> {

    /**
     * 实体类的 Class 对象，用于反射和缓存获取
     */
    private final Class<E> entityClass;

    /**
     * 构造函数
     * 子类必须调用此构造函数并传入实体类的 Class 对象
     *
     * @param entityClass 实体类的 Class 对象
     */
    protected BaseDao(Class<E> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("实体类 Class 对象不能为 null");
        }
        this.entityClass = entityClass;
    }

    /**
     * 获取实体类 Class 对象
     *
     * @return 实体类 Class 对象
     */
    protected Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * 获取实体缓存
     * 使用泛型擦除和类型转换确保类型安全
     *
     * @return 实体缓存对象
     */
    @SuppressWarnings("unchecked")
    protected IEntityCaches<PK, E> getEntityCaches() {
        IEntityCaches<?, ?> caches = OrmContext.getEntityCaches(entityClass);
        return (IEntityCaches<PK, E>) caches;
    }

    /**
     * 根据主键加载实体
     * <p>
     * 此方法会先从缓存中查找，若缓存不存在则从数据库加载
     *
     * @param pk 主键
     * @return 实体对象，不存在返回 null
     */
    public E load(PK pk) {
        if (pk == null) {
            log.warn("加载实体失败：主键不能为 null, 实体类: {}", entityClass.getSimpleName());
            return null;
        }
        return getEntityCaches().load(pk);
    }

    /**
     * 保存实体到缓存
     * <p>
     * 实体会被添加到缓存中，等待持久化器异步写入数据库
     *
     * @param entity 实体对象
     */
    public void save(E entity) {
        if (entity == null) {
            log.warn("保存实体失败：实体对象不能为 null, 实体类: {}", entityClass.getSimpleName());
            return;
        }
        getEntityCaches().addLoad(entity);
    }

    /**
     * 更新缓存中的实体
     * <p>
     * 更新后的实体会在下次持久化时写入数据库
     *
     * @param entity 实体对象
     */
    public void update(E entity) {
        if (entity == null) {
            log.warn("更新实体失败：实体对象不能为 null, 实体类: {}", entityClass.getSimpleName());
            return;
        }
        getEntityCaches().update(entity);
    }

    /**
     * 删除指定主键的实体
     * <p>
     * 此方法会：
     * 1. 从缓存中移除实体
     * 2. 从数据库中删除实体
     *
     * @param pk 主键
     */
    public void delete(PK pk) {
        if (pk == null) {
            log.warn("删除实体失败：主键不能为 null, 实体类: {}", entityClass.getSimpleName());
            return;
        }
        // 先从缓存中移除
        getEntityCaches().invalidate(pk);
        // 再从数据库删除
        OrmContext.getAccessor().delete(pk, entityClass);
    }
}
