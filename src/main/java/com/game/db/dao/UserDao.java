package com.game.db.dao;

import com.game.db.OrmContext;
import com.game.db.cache.IEntityCaches;
import com.game.model.User;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户数据访问对象
 * 使用新的 ORM 框架提供用户数据访问功能
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于用户数据访问
 * - 依赖注入：通过 OrmContext 获取缓存
 * - 简化实现：利用 ORM 框架的缓存和持久化功能
 *
 * @author Harleysama
 */
@Slf4j
public class UserDao {

    /**
     * 获取用户缓存
     */
    @SuppressWarnings("unchecked")
    private IEntityCaches<Long, User> getUserCaches() {
        return (IEntityCaches<Long, User>) OrmContext.getEntityCaches(User.class);
    }

    /**
     * 根据ID加载用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    public User load(Long id) {
        return getUserCaches().load(id);
    }

    /**
     * 保存用户
     *
     * @param user 用户对象
     */
    public void save(User user) {
        getUserCaches().addLoad(user);
    }

    /**
     * 更新用户
     *
     * @param user 用户对象
     */
    public void update(User user) {
        getUserCaches().update(user);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    public void delete(Long id) {
        getUserCaches().invalidate(id);
        OrmContext.getAccessor().delete(id, User.class);
    }

    /**
     * 根据用户名查找用户
     * 注意：此方法需要查询构建器支持，暂时简化实现
     *
     * @param username 用户名
     * @return 用户对象，不存在返回null
     */
    public User findByUsername(String username) {
        // TODO: 实现查询构建器后完善此方法
        log.warn("findByUsername 方法需要查询构建器支持");
        return null;
    }
}
