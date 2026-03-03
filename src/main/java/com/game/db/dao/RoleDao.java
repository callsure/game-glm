package com.game.db.dao;

import com.game.model.Role;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 角色数据访问对象
 * 继承自 BaseDao，提供角色数据访问功能
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于角色数据访问
 * - 继承复用：通过 BaseDao 复用通用 CRUD 方法
 * - 扩展性：可添加特定于 Role 的查询方法
 *
 * @author Harleysama
 */
@Slf4j
public class RoleDao extends BaseDao<Long, Role> {

    /**
     * 构造函数
     * 调用父类构造函数，传入 Role 类的 Class 对象
     */
    public RoleDao() {
        super(Role.class);
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
