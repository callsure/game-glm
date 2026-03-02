package com.game.db.model.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * 实体缓存注解
 * 标注在实体类上，配置缓存和持久化策略
 * <p>
 * 使用规范：
 * - 所有需要持久化的实体类必须标注此注解
 * - 必须实现 IEntity 接口
 * - 缓存和持久化策略在配置文件中定义
 *
 * @author Harleysama
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EntityCache {

    /**
     * 缓存策略配置
     */
    Cache cache() default @Cache;

    /**
     * 持久化策略配置
     */
    Persister persister() default @Persister;
}
