package com.game.db.dao;

import com.game.db.OrmContext;
import com.game.db.cache.IEntityCaches;
import com.game.model.Role;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 角色数据访问对象
 * 使用新的 ORM 框架提供角色数据访问功能
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于角色数据访问
 * - 依赖注入：通过 OrmContext 获取缓存
 * - 简化实现：利用 ORM 框架的缓存和持久化功能
 *
 * @author Harleysama
 */
@Slf4j
public class RoleDao {

    /**
     * 获取角色缓存
     */
    @SuppressWarnings("unchecked")
    private IEntityCaches<Long, Role> getRoleCaches() {
        return (IEntityCaches<Long, Role>) OrmContext.getEntityCaches(Role.class);
    }

    /**
     * 根据ID加载角色
     *
     * @param id 角色ID
     * @return 角色对象
     */
    public Role load(Long id) {
        return getRoleCaches().load(id);
    }

    /**
     * 保存角色
     *
     * @param role 角色对象
     */
    public void save(Role role) {
        getRoleCaches().addLoad(role);
    }

    /**
     * 更新角色
     *
     * @param role 角色对象
     */
    public void update(Role role) {
        getRoleCaches().update(role);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    public void delete(Long id) {
        getRoleCaches().invalidate(id);
        OrmContext.getAccessor().delete(id, Role.class);
    }

    /**
     * 根据用户ID查找角色列表
     * 注意：此方法需要查询构建器支持，暂时简化实现
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<Role> findByUserId(Long userId) {
        // TODO: 实现查询构建器后完善此方法
        log.warn("findByUserId 方法需要查询构建器支持");
        return List.of();
    }
}
