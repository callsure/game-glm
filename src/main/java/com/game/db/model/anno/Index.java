package com.game.db.model.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * 索引注解
 * 标注在实体类的字段上，为该字段创建数据库索引
 * <p>
 * 使用规范：
 * - 可为任意字段创建索引
 * - 支持唯一索引和 TTL 索引
 * - TTL 索引字段类型必须是 Date 或 List<Date>
 *
 * @author Harleysama
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Index {

    /**
     * 是否升序索引
     */
    boolean ascending() default true;

    /**
     * 是否唯一索引
     */
    boolean unique() default false;

    /**
     * TTL 过期时间（秒）
     * 默认 -1 表示不开启 TTL
     * 开启后字段类型必须是 Date 或 List<Date>
     */
    long ttlExpireAfterSeconds() default -1L;
}
