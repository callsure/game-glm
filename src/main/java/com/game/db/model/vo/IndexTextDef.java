package com.game.db.model.vo;

import com.game.db.model.anno.IndexText;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 文本索引定义类
 * 封装字段的文本索引元数据信息
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于文本索引元数据管理
 * - 验证约束：字段必须是 private 修饰
 *
 * @author Harleysama
 */
public class IndexTextDef {

    /**
     * 索引字段
     */
    private final Field field;

    /**
     * 构造方法
     *
     * @param field      索引字段
     * @param indexText  文本索引注解
     * @throws IllegalArgumentException 字段不是 private 修饰时抛出
     */
    public IndexTextDef(Field field, IndexText indexText) {
        if (field == null) {
            throw new IllegalArgumentException("字段不能为空");
        }

        // 验证字段是否被 private 修饰
        if (!Modifier.isPrivate(field.getModifiers())) {
            throw new IllegalArgumentException("[" + field.getName() + "] 没有被 private 修饰");
        }

        this.field = field;
        this.field.setAccessible(true);
    }

    public Field getField() {
        return field;
    }
}
