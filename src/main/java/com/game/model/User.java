package com.game.model;

import lombok.Data;

/**
 * 用户实体
 *
 * @author Harleysama
 */
@Data
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;
}
