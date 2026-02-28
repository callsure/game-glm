package com.game.model;

import lombok.Data;

/**
 * 角色实体
 *
 * @author Harleysama
 */
@Data
public class Role {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 等级
     */
    private Integer level = 1;

    /**
     * 职业
     */
    private Integer profession;

    /**
     * 经验值
     */
    private Long exp = 0L;

    /**
     * 金币
     */
    private Long gold = 1000L;

    /**
     * 钻石
     */
    private Long diamond = 100L;

    // ========== 基础属性 ==========

    /**
     * 生命值
     */
    private Integer hp = 100;

    /**
     * 最大生命值
     */
    private Integer maxHp = 100;

    /**
     * 魔法值
     */
    private Integer mp = 50;

    /**
     * 最大魔法值
     */
    private Integer maxMp = 50;

    /**
     * 攻击力
     */
    private Integer attack = 10;

    /**
     * 防御力
     */
    private Integer defense = 5;

    /**
     * 速度
     */
    private Integer speed = 10;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;

    /**
     * 当前地图ID
     */
    private Integer mapId = 1;

    /**
     * X坐标
     */
    private Float x = 0f;

    /**
     * Y坐标
     */
    private Float y = 0f;

    /**
     * Z坐标
     */
    private Float z = 0f;

    /**
     * 旋转角度
     */
    private Float rotation = 0f;
}
