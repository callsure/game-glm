package com.game.actor;

import akka.actor.typed.ActorRef;
import com.game.model.Role;
import com.game.model.User;

import java.util.List;

/**
 * 登录命令接口
 * 定义登录 Actor 可以接收的所有命令
 * <p>
 * 设计原则：
 * - 接口隔离原则（ISP）：细分命令类型
 * - 不可变性：所有命令都是不可变记录类
 * - 响应模式：使用 ActorRef 实现请求-响应模式
 *
 * @author Harleysama
 */
public interface LoginCommand extends GameCommand {

    /**
     * 执行登录请求
     * 处理用户登录逻辑：验证用户、加载角色、生成Token
     */
    record ExecuteLogin(
            String username,
            String password,
            long sessionId,
            ActorRef<LoginResponse> replyTo
    ) implements LoginCommand {
    }

    /**
     * 创建用户请求
     * 当用户不存在时创建新用户
     */
    record CreateUser(
            String username,
            String password,
            ActorRef<LoginResponse> replyTo
    ) implements LoginCommand {
    }

    /**
     * 加载用户角色列表请求
     */
    record LoadUserRoles(
            Long userId,
            ActorRef<LoginResponse> replyTo
    ) implements LoginCommand {
    }

    // ==================== 响应消息 ====================

    /**
     * 登录响应
     * 登录成功后返回的用户信息和角色列表
     */
    record LoginSuccess(
            Long userId,
            String username,
            String token,
            Long expireTime,
            List<Role> roles,
            Long lastLoginTime
    ) implements LoginResponse {
    }

    /**
     * 登录失败响应
     */
    record LoginFailure(
            String errorCode,
            String errorMessage
    ) implements LoginResponse {
    }

    /**
     * 用户创建成功响应
     */
    record UserCreated(
            User user
    ) implements LoginResponse {
    }

    /**
     * 角色列表加载成功响应
     */
    record RolesLoaded(
            List<Role> roles
    ) implements LoginResponse {
    }

    /**
     * 登录响应接口
     * 所有响应消息都需要实现此接口
     */
    interface LoginResponse extends GameCommand {
    }
}
