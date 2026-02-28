package com.game.handler;

import com.game.db.UserDao;
import com.game.model.User;
import com.game.net.Session;
import com.game.net.SessionManager;
import com.game.protocol.generated.AuthProto;
import com.game.protocol.generated.CommonProto;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录处理器
 * 处理用户登录请求
 *
 * @author Harleysama
 */
@Slf4j
@GameHandler(messageType = CommonProto.MessageType.LOGIN_VALUE)
public class LoginHandler extends AbstractMessageHandler<AuthProto.LoginRequest> {

    private final UserDao userDao = new UserDao();

    @Override
    protected com.google.protobuf.Parser<AuthProto.LoginRequest> getParser() {
        return AuthProto.LoginRequest.parser();
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, Session session, AuthProto.LoginRequest message) throws Exception {
        String username = message.getUsername();
        String password = message.getPassword();

        log.info("用户登录请求: username={}", username);

        // 查询用户
        User user = userDao.findByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            sendError(ctx, CommonProto.ErrorCode.AUTH_FAILED, "用户名或密码错误");
            return;
        }

        // 验证密码（实际项目应该使用加密密码）
        if (!user.getPassword().equals(password)) {
            log.warn("密码错误: {}", username);
            sendError(ctx, CommonProto.ErrorCode.AUTH_FAILED, "用户名或密码错误");
            return;
        }

        // 绑定用户到会话
        SessionManager.bindUser(ctx.channel(), user.getId());

        // 生成token（实际项目应该使用JWT等）
        String token = generateToken(user.getId());

        // 构建角色列表
        AuthProto.LoginResponse.Builder responseBuilder = AuthProto.LoginResponse.newBuilder()
                .setUserId(user.getId())
                .setToken(token)
                .setExpireTime(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000); // 7天有效期

        // TODO: 加载用户角色列表
        // responseBuilder.addAllRoles(roleList);

        sendResponse(ctx, CommonProto.MessageType.LOGIN_RESP_VALUE, responseBuilder.build());

        log.info("用户登录成功: userId={}, username={}", user.getId(), username);
    }

    /**
     * 生成Token
     */
    private String generateToken(Long userId) {
        // 简单实现，实际项目应该使用JWT
        return "token_" + userId + "_" + System.currentTimeMillis();
    }
}
