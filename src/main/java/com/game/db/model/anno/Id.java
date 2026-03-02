package com.game.db.model.anno;

import java.lang.annotation.*;

/**
 * 主键注解
 * 标注在实体类的字段上，表示该字段为主键
 * <p>
 * 使用规范：
 * - 每个实体类必须有且仅有一个 @Id 注解
 * - @Id 字段必须是 private 修饰
 * - 必须实现 id() 方法返回该字段的值
 *
 * @author Harleysama
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Id {
}
