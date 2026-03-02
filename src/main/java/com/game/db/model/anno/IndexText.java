package com.game.db.model.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * 文本索引注解
 * 标注在实体类的字段上，为该字段创建文本搜索索引
 * <p>
 * 使用规范：
 * - 一个集合只能有一个文本索引
 * - 用于全文搜索场景
 *
 * @author Harleysama
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface IndexText {
}
