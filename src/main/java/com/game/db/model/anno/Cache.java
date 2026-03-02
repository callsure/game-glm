package com.game.db.model.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * 缓存策略注解
 * 在 @EntityCache 中使用，指定缓存策略名称
 *
 * @author Harleysama
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Cache {

    /**
     * 缓存策略名称
     * 对应配置文件中定义的缓存策略
     */
    String value() default "default";
}
