package com.game.model;

import com.game.db.model.anno.*;
import com.game.db.model.entity.IEntity;
import lombok.Data;

/**
 * 角色实体
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于角色数据
 * - 实现 IEntity 接口以支持 ORM 操作
 *
 * @author Harleysama
 */
@Data
@EntityCache(
        cache = @Cache(value = "default"),
        persister = @Persister(value = "default")
)
public class Role implements IEntity<Long> {

    /**
     * 角色ID（主键）
     */
    @Id
    private Long id;

    /**
     * 用户ID（外键）
     */
    @Index(ascending = true)
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
     * 最后登录时间
     */
    private Long lastLoginTime;

    /**
     * 版本号（用于并发控制）
     */
    private long vs;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public long gvs() {
        return vs;
    }

    @Override
    public void svs(long vs) {
        this.vs = vs;
    }
}
