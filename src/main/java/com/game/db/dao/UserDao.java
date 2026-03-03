package com.game.db.dao;

import com.game.db.OrmContext;
import com.game.db.accessor.IAccessor;
import com.game.model.User;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户数据访问对象
 * 继承自 BaseDao，提供用户数据访问功能
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于用户数据访问
 * - 单例模式：确保全局唯一的 UserDao 实例
 * - 继承复用：通过 BaseDao 复用通用 CRUD 方法
 * <p>
 * 获取实例：
 * <pre>
 * UserDao userDao = DaoManager.getInstance().getUserDao();
 * </pre>
 *
 * @author Harleysama
 */
@Slf4j
public class UserDao extends BaseDao<Long, User> {

    /**
     * 私有构造函数
     * 防止外部直接创建实例，通过 DaoManager 统一管理
     */
    private UserDao() {
        super(User.class);
    }

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户对象，不存在返回 null
     */
    public User findByUsername(String username) {
        IAccessor accessor = OrmContext.getAccessor();
        return accessor.findByIndex("username", username, User.class);
    }

    /**
     * 创建新用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 新创建的用户对象
     */
    public User createUser(String username, String password) {
        // 生成新的用户ID（实际项目应该使用分布式ID生成器）
        Long userId = System.currentTimeMillis();

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setCreateTime(System.currentTimeMillis());
        user.setLastLoginTime(System.currentTimeMillis());
        user.setVs(0);

        // 保存到数据库和缓存
        save(user);

        log.info("创建新用户: userId={}, username={}", userId, username);
        return user;
    }
}
