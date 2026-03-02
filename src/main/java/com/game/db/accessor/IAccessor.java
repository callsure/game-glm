package com.game.db.accessor;

import com.game.db.model.entity.IEntity;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 数据访问接口
 * 定义实体的 CRUD 操作
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：定义最小必要方法
 * - 依赖倒置原则（DIP）：高层依赖抽象接口
 * - 单一职责原则（SRP）：专注于数据访问操作
 *
 * @author Harleysama
 */
public interface IAccessor {

    /**
     * 插入单个实体
     *
     * @param entity 实体对象
     * @param <E>    实体类型
     * @return 是否插入成功
     */
    <E extends IEntity<?>> boolean insert(E entity);

    /**
     * 批量插入实体
     *
     * @param entities 实体列表
     * @param <E>      实体类型
     */
    <E extends IEntity<?>> void batchInsert(List<E> entities);

    /**
     * 更新单个实体
     *
     * @param entity 实体对象
     * @param <E>    实体类型
     * @return 是否更新成功
     */
    <E extends IEntity<?>> boolean update(E entity);

    /**
     * 批量更新实体
     *
     * @param entities 实体列表
     * @param <E>      实体类型
     */
    <E extends IEntity<?>> void batchUpdate(List<E> entities);

    /**
     * 删除单个实体
     *
     * @param entity 实体对象
     * @param <E>    实体类型
     * @return 是否删除成功
     */
    <E extends IEntity<?>> boolean delete(E entity);

    /**
     * 根据主键删除实体
     *
     * @param pk          主键值
     * @param entityClazz 实体类型
     * @param <E>         实体类型
     * @return 是否删除成功
     */
    <E extends IEntity<?>> boolean delete(Object pk, Class<E> entityClazz);

    /**
     * 批量删除实体
     *
     * @param entities 实体列表
     * @param <E>      实体类型
     */
    <E extends IEntity<?>> void batchDelete(List<E> entities);

    /**
     * 根据主键列表批量删除
     *
     * @param pks         主键值列表
     * @param entityClazz 实体类型
     * @param <E>         实体类型
     */
    <E extends IEntity<?>> void batchDelete(List<?> pks, Class<E> entityClazz);

    /**
     * 根据主键加载实体
     *
     * @param pk          主键值
     * @param entityClazz 实体类型
     * @param <E>         实体类型
     * @return 实体对象，不存在返回 null
     */
    @Nullable
    <E extends IEntity<?>> E load(Object pk, Class<E> entityClazz);
}
