package com.game.utils;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 反射工具类
 * 提供反射操作的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于反射操作
 * - 简化常用反射操作
 *
 * @author Harleysama
 */
public abstract class ReflectionUtils {

    /**
     * 将 clazz 通过 filter 过滤，过滤后的 field 执行 callback 方法
     *
     * @param clazz         目标类
     * @param fieldFilter   字段过滤器
     * @param fieldCallback 字段回调方法
     */
    public static void filterFieldsInClass(Class<?> clazz, Predicate<Field> fieldFilter, Consumer<Field> fieldCallback) {
        Class<?> targetClass = clazz;
        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                filterField(field, fieldFilter, fieldCallback);
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    /**
     * 如果 field 符合 fieldFilter 过滤条件，则执行回调方法
     *
     * @param field         字段
     * @param fieldFilter   字段过滤器
     * @param fieldCallback 字段回调方法
     */
    public static void filterField(Field field, Predicate<Field> fieldFilter, Consumer<Field> fieldCallback) {
        if (fieldFilter != null && !fieldFilter.test(field)) {
            return;
        }
        fieldCallback.accept(field);
    }

    /**
     * 从 POJO 类中获取具有指定注解的字段，只获取子类的字段，不获取父类的字段
     *
     * @param clazz      目标类
     * @param annotation 注解类型
     * @return 字段数组，可能长度为0
     */
    public static Field[] getFieldsByAnnoInPOJOClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> list = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotation)) {
                list.add(field);
            }
        }
        return ArrayUtil.toArray(list, Field.class);
    }

    /**
     * 判断是否为简单的 POJO 类
     *
     * @param clazz 类
     * @return true 如果是 POJO 类
     */
    public static boolean isPojoClass(Class<?> clazz) {
        return clazz.getSuperclass().equals(Object.class);
    }

    /**
     * 断言为 POJO 类
     *
     * @param clazz 类
     * @throws IllegalArgumentException 如果不是 POJO 类
     */
    public static void assertIsPojoClass(Class<?> clazz) {
        if (!isPojoClass(clazz)) {
            throw new IllegalArgumentException("[class:" + clazz.getName() + "]不是简单的javabean（POJO类不能继承别的类，但是可以继承其它接口）");
        }
    }

    /**
     * 获取公共空构造函数
     *
     * @param clazz 类
     * @return 构造函数
     * @throws IllegalArgumentException 如果没有公共空构造函数
     */
    public static Constructor<?> publicEmptyConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new IllegalArgumentException("[class:" + clazz.getCanonicalName() + "] should have exactly one public zero-argument constructor");
            }
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("[class:" + clazz.getCanonicalName() + "] should have exactly one public zero-argument constructor", e);
        }
    }

    /**
     * 获取 class 中的普通 field 属性字段（非静态、非瞬态）
     *
     * @param clazz 类
     * @return 字段列表
     */
    public static List<Field> notStaticAndTransientFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(it -> !Modifier.isStatic(it.getModifiers()))
                .filter(it -> !Modifier.isTransient(it.getModifiers()))
                .filter(it -> !it.isAnnotationPresent(Transient.class))
                .collect(Collectors.toList());
    }

    /**
     * 标准 field 名称更加通用，前缀不能是 is
     *
     * @param field 字段
     * @throws IllegalArgumentException 如果字段名以 is 开头
     */
    public static void assertIsStandardFieldName(Field field) {
        String fieldName = field.getName();
        if (fieldName.startsWith("is")) {
            throw new IllegalArgumentException("to avoid different get or set method in different language, [field:" + field.getName() + "] can not be started with name of 'is' in class:[" + field.getDeclaringClass().getCanonicalName() + "]");
        }
    }
}
