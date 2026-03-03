package com.game.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.game.db.dao.DaoManager;
import com.game.db.dao.RoleDao;
import com.game.db.dao.UserDao;
import com.game.model.Role;
import com.game.model.User;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 登录 Actor
 * 处理用户登录相关的业务逻辑
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于登录业务逻辑
 * - 异步处理：Actor 消息异步处理，不阻塞网络线程
 * - 状态封装：登录状态完全封装在 Actor 内部
 * - 无副作用：Actor 内部操作不影响外部状态
 * - 依赖注入：通过 DaoManager 获取单例 Dao 实例
 *
 * @author Harleysama
 */
@Slf4j
public class LoginActor extends AbstractBehavior<LoginCommand> {

    /**
     * 用户 DAO
     * 通过 DaoManager 获取单例实例
     */
    private final UserDao userDao = DaoManager.getInstance().getUserDao();

    /**
     * 角色 DAO
     * 通过 DaoManager 获取单例实例
     */
    private final RoleDao roleDao = DaoManager.getInstance().getRoleDao();

    /**
     * Token 有效期（7天）
     */
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 私有构造函数
     *
     * @param context Actor 上下文
     */
    private LoginActor(ActorContext<LoginCommand> context) {
        super(context);
        log.info("登录 Actor 启动");
    }

    /**
     * 创建 LoginActor 行为
     *
     * @return Behavior
     */
    public static Behavior<LoginCommand> create() {
        return Behaviors.setup(LoginActor::new);
    }

    @Override
    public Receive<LoginCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(LoginCommand.ExecuteLogin.class, this::onExecuteLogin)
                .onMessage(LoginCommand.CreateUser.class, this::onCreateUser)
                .onMessage(LoginCommand.LoadUserRoles.class, this::onLoadUserRoles)
                .onSignal(akka.actor.typed.PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * Actor 停止时的处理
     */
    private Behavior<LoginCommand> onPostStop() {
        log.info("登录 Actor 停止");
        return this;
    }

    /**
     * 处理登录请求
     * 核心登录流程：
     * 1. 查询用户是否存在
     * 2. 不存在则创建新用户
     * 3. 更新最后登录时间
     * 4. 加载用户角色列表
     * 5. 生成Token并返回响应
     *
     * @param msg 登录请求消息
     * @return Behavior
     */
    private Behavior<LoginCommand> onExecuteLogin(LoginCommand.ExecuteLogin msg) {
        String username = msg.username();
        String password = msg.password();
        long sessionId = msg.sessionId();

        log.info("处理登录请求: username={}, sessionId={}", username, sessionId);

        try {
            // 1. 查询用户，不存在则创建新用户
            User user = userDao.findByUsername(username);
            if (user == null) {
                log.info("用户不存在，创建新用户: username={}", username);
                user = userDao.createUser(username, password);
            } else {
                // 更新最后登录时间
                user.setLastLoginTime(System.currentTimeMillis());
                userDao.update(user);
                log.info("用户登录成功: userId={}, username={}", user.getId(), username);
            }

            // 2. 加载用户角色列表
            List<Role> roleList = roleDao.findByUserId(user.getId());
            log.info("用户角色数量: userId={}, roleCount={}", user.getId(), roleList.size());

            // 3. 生成Token
            String token = generateToken(user.getId());

            // 4. 发送登录成功响应
            long expireTime = System.currentTimeMillis() + TOKEN_EXPIRE_TIME;
            msg.replyTo().tell(new LoginCommand.LoginSuccess(
                    user.getId(),
                    user.getUsername(),
                    token,
                    expireTime,
                    roleList,
                    user.getLastLoginTime()
            ));

            log.info("登录响应发送成功: userId={}, username={}, roleCount={}",
                    user.getId(), username, roleList.size());

        } catch (Exception e) {
            log.error("登录处理异常: username={}", username, e);
            msg.replyTo().tell(new LoginCommand.LoginFailure(
                    "LOGIN_ERROR",
                    "登录失败: " + e.getMessage()
            ));
        }

        return this;
    }

    /**
     * 处理创建用户请求
     *
     * @param msg 创建用户消息
     * @return Behavior
     */
    private Behavior<LoginCommand> onCreateUser(LoginCommand.CreateUser msg) {
        String username = msg.username();
        String password = msg.password();

        log.info("创建用户请求: username={}", username);

        try {
            // 检查用户是否已存在
            User existingUser = userDao.findByUsername(username);
            if (existingUser != null) {
                log.warn("用户已存在: username={}", username);
                msg.replyTo().tell(new LoginCommand.LoginFailure(
                        "USER_EXISTS",
                        "用户名已存在"
                ));
                return this;
            }

            // 创建新用户
            User user = userDao.createUser(username, password);
            msg.replyTo().tell(new LoginCommand.UserCreated(user));

            log.info("用户创建成功: userId={}, username={}", user.getId(), username);

        } catch (Exception e) {
            log.error("创建用户异常: username={}", username, e);
            msg.replyTo().tell(new LoginCommand.LoginFailure(
                    "CREATE_USER_ERROR",
                    "创建用户失败: " + e.getMessage()
            ));
        }

        return this;
    }

    /**
     * 处理加载用户角色请求
     *
     * @param msg 加载角色消息
     * @return Behavior
     */
    private Behavior<LoginCommand> onLoadUserRoles(LoginCommand.LoadUserRoles msg) {
        Long userId = msg.userId();

        log.info("加载用户角色: userId={}", userId);

        try {
            List<Role> roles = roleDao.findByUserId(userId);
            msg.replyTo().tell(new LoginCommand.RolesLoaded(roles));

            log.info("角色加载成功: userId={}, roleCount={}", userId, roles.size());

        } catch (Exception e) {
            log.error("加载角色异常: userId={}", userId, e);
            msg.replyTo().tell(new LoginCommand.LoginFailure(
                    "LOAD_ROLES_ERROR",
                    "加载角色失败: " + e.getMessage()
            ));
        }

        return this;
    }

    /**
     * 生成Token
     * 实际项目应该使用JWT等专业认证方案
     *
     * @param userId 用户ID
     * @return Token字符串
     */
    private String generateToken(Long userId) {
        // 简单实现，实际项目应该使用JWT
        return "token_" + userId + "_" + System.currentTimeMillis();
    }
}
