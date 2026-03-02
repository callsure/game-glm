package com.game.db.model.anno;

import com.game.db.cache.IEntityCaches;

import java.lang.annotation.*;

/**
 * 实体缓存注入注解
 * 标注在 Manager 类的字段上，自动注入对应的 IEntityCaches
 * <p>
 * 使用规范：
 * - 字段类型必须是 IEntityCaches<?, E>
 * - 注入的实体缓存必须是已注册的实体
 * <p>
 * 示例：
 * <pre>
 * {@code
 * public class UserManager {
 *     @EntityCachesInjection
 *     private IEntityCaches<Long, User> userCaches;
 * }
 * }
 * </pre>
 *
 * @author Harleysama
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EntityCachesInjection {
}
