package com.game.handler;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;
import com.game.actor.GameActorSystem;
import com.game.actor.LoginCommand;
import com.game.model.Role;
import com.game.net.Session;
import com.game.net.SessionManager;
import com.game.protocol.generated.AuthProto;
import com.game.protocol.generated.CommonProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * 登录处理器
 * 处理用户登录请求，通过 Akka Actor 系统异步处理登录逻辑
 * <p>
 * 登录流程：
 * 1. Netty Handler 接收登录请求
 * 2. 通过 Akka Ask 模式向 LoginActor 发送登录命令
 * 3. LoginActor 处理业务逻辑（验证用户、加载角色、生成Token）
 * 4. 返回登录响应并通过 Netty 发送给客户端
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于 Netty 消息处理
 * - 依赖倒置原则（DIP）：依赖 Akka Actor 抽象层
 * - 异步处理：不阻塞 Netty IO 线程
 * - 响应式编程：使用 CompletionStage 处理异步响应
 *
 * @author Harleysama
 */
@Slf4j
@GameHandler(messageType = CommonProto.MessageType.LOGIN_VALUE)
public class LoginHandler extends AbstractMessageHandler<AuthProto.LoginRequest> {

    /**
     * Ask 超时时间（秒）
     */
    private static final int ASK_TIMEOUT_SECONDS = 5;

    /**
     * LoginActor 引用缓存
     */
    private static ActorRef<LoginCommand> loginActorRef;

    @Override
    protected com.google.protobuf.Parser<AuthProto.LoginRequest> getParser() {
        return AuthProto.LoginRequest.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, AuthProto.LoginRequest message) {
        String username = message.getUsername();
        String password = message.getPassword();

        // 使用 Channel ID 作为会话标识
        long sessionId = ctx.channel().id().asLongText().hashCode();

        log.info("用户登录请求: username={}, sessionId={}", username, sessionId);

        try {
            // 获取 LoginActor 引用
            ActorRef<LoginCommand> loginActor = getLoginActor();

            // 使用 Ask 模式发送登录请求
            CompletionStage<LoginCommand.LoginResponse> responseFuture = AskPattern.ask(
                    loginActor,
                    replyTo -> new LoginCommand.ExecuteLogin(username, password, sessionId, replyTo),
                    Duration.ofSeconds(ASK_TIMEOUT_SECONDS),
                    GameActorSystem.getSystem().scheduler()
            );

            // 异步处理响应
            responseFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    log.error("登录处理异常: username={}", username, throwable);
                    sendError(ctx, CommonProto.ErrorCode.SERVER_ERROR, "登录处理超时");
                    return;
                }

                if (response == null) {
                    log.error("登录响应为空: username={}", username);
                    sendError(ctx, CommonProto.ErrorCode.SERVER_ERROR, "登录响应为空");
                    return;
                }

                // 处理不同类型的响应
                if (response instanceof LoginCommand.LoginSuccess success) {
                    handleLoginSuccess(ctx, username, success);
                } else if (response instanceof LoginCommand.LoginFailure failure) {
                    log.error("登录失败: username={}, errorCode={}, errorMessage={}",
                            username, failure.errorCode(), failure.errorMessage());
                    sendError(ctx, CommonProto.ErrorCode.AUTH_FAILED, failure.errorMessage());
                }
            });

        } catch (Exception e) {
            log.error("登录请求处理异常: username={}", username, e);
            sendError(ctx, CommonProto.ErrorCode.SERVER_ERROR, "登录请求处理失败");
        }
    }

    /**
     * 处理登录成功
     *
     * @param ctx      Channel 上下文
     * @param username 用户名
     * @param success  登录成功响应
     */
    private void handleLoginSuccess(ChannelHandlerContext ctx, String username, LoginCommand.LoginSuccess success) {
        try {
            // 绑定用户到会话
            SessionManager.bindUser(ctx.channel(), success.userId());

            // 构建角色列表
            List<AuthProto.RoleInfo> roleInfoList = success.roles().stream()
                    .map(this::convertToRoleInfo)
                    .collect(Collectors.toList());

            // 构建登录响应
            AuthProto.LoginResponse response = AuthProto.LoginResponse.newBuilder()
                    .setUserId(success.userId())
                    .setToken(success.token())
                    .setExpireTime(success.expireTime())
                    .addAllRoles(roleInfoList)
                    .build();

            sendResponse(ctx, CommonProto.MessageType.LOGIN_RESP_VALUE, response);

            log.info("登录响应发送成功: userId={}, username={}, roleCount={}",
                    success.userId(), username, roleInfoList.size());
        } catch (Exception e) {
            log.error("发送登录响应异常: username={}", username, e);
        }
    }

    /**
     * 获取 LoginActor 引用
     * 使用懒加载 + 缓存模式
     *
     * @return LoginActor 引用
     */
    private ActorRef<LoginCommand> getLoginActor() {
        if (loginActorRef == null) {
            synchronized (LoginHandler.class) {
                if (loginActorRef == null) {
                    // 从 GameActorSystem 获取 LoginActor 引用
                    loginActorRef = GameActorSystem.getLoginActor();
                    log.info("LoginActor 引用已缓存");
                }
            }
        }
        return loginActorRef;
    }

    /**
     * 转换Role实体为RoleInfo协议对象
     *
     * @param role 角色实体
     * @return 角色信息协议对象
     */
    private AuthProto.RoleInfo convertToRoleInfo(Role role) {
        return AuthProto.RoleInfo.newBuilder()
                .setRoleId(role.getId())
                .setName(role.getName())
                .setLevel(role.getLevel())
                .setProfession(role.getProfession())
                .setLastLoginTime(role.getLastLoginTime() != null ? role.getLastLoginTime() : 0L)
                .build();
    }
}
