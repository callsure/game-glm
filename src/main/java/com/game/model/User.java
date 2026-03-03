package com.game.model;

import com.game.db.model.anno.*;
import com.game.db.model.entity.IEntity;
import lombok.Data;

/**
 * 用户实体
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于用户数据
 * - 实现 IEntity 接口以支持 ORM 操作
 *
 * @author Harleysama
 */
@Data
@EntityCache(
        cache = @Cache(value = "default"),
        persister = @Persister(value = "default")
)
public class User implements IEntity<Long> {

    /**
     * 用户ID（主键）
     */
    @Id
    private Long id;

    /**
     * 用户名
     */
    @Index(ascending = true, unique = true)
    private String username;

    /**
     * 创建时间
     */
    private Long createTime;

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
