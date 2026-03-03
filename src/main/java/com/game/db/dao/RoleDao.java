package com.game.db.dao;

import com.game.db.OrmContext;
import com.game.db.accessor.IAccessor;
import com.game.model.Role;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 角色数据访问对象
 * 继承自 BaseDao，提供角色数据访问功能
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于角色数据访问
 * - 单例模式：确保全局唯一的 RoleDao 实例
 * - 继承复用：通过 BaseDao 复用通用 CRUD 方法
 * <p>
 * 获取实例：
 * <pre>
 * RoleDao roleDao = DaoManager.getInstance().getRoleDao();
 * </pre>
 *
 * @author Harleysama
 */
@Slf4j
public class RoleDao extends BaseDao<Long, Role> {

    /**
     * 私有构造函数
     * 防止外部直接创建实例，通过 DaoManager 统一管理
     */
    private RoleDao() {
        super(Role.class);
    }

    /**
     * 根据用户ID查找角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<Role> findByUserId(Long userId) {
        IAccessor accessor = OrmContext.getAccessor();
        return accessor.findListByIndex("userId", userId, Role.class);
    }

    /**
     * 为用户创建新角色
     *
     * @param userId    用户ID
     * @param name      角色名称
     * @param profession 职业
     * @return 新创建的角色对象
     */
    public Role createRole(Long userId, String name, Integer profession) {
        // 生成新的角色ID（实际项目应该使用分布式ID生成器）
        Long roleId = System.currentTimeMillis();

        Role role = new Role();
        role.setId(roleId);
        role.setUserId(userId);
        role.setName(name);
        role.setLevel(1);
        role.setProfession(profession);
        role.setExp(0L);
        role.setGold(1000L);
        role.setDiamond(100L);
        role.setLastLoginTime(System.currentTimeMillis());
        role.setVs(0);

        // 保存到数据库和缓存
        save(role);

        log.info("创建新角色: roleId={}, userId={}, name={}, profession={}", roleId, userId, name, profession);
        return role;
    }
}
