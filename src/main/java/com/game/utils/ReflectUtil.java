package com.game.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 反射工具类
 * 提供反射操作的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于反射操作
 * - 异常处理：统一处理反射异常
 *
 * @author Harleysama
 */
public class ReflectUtil {

    /**
     * 获取字段值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("获取字段值失败: " + fieldName, e);
        }
    }

    /**
     * 设置字段值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @param value     值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null) {
            return;
        }
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field == null) {
                throw new RuntimeException("字段不存在: " + fieldName);
            }
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("设置字段值失败: " + fieldName, e);
        }
    }

    /**
     * 设置字段值（使用 Field 对象）
     *
     * @param obj   对象
     * @param field 字段
     * @param value 值
     */
    public static void setFieldValue(Object obj, Field field, Object value) {
        if (obj == null || field == null) {
            return;
        }
        try {
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("设置字段值失败: " + field.getName(), e);
        }
    }

    /**
     * 获取字段
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段，不存在返回 null
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 尝试从父类获取
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getField(superClass, fieldName);
            }
            return null;
        }
    }

    /**
     * 获取字段（设置可访问）
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段，不存在返回 null
     */
    public static Field getAccessibleField(Class<?> clazz, String fieldName) {
        Field field = getField(clazz, fieldName);
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * 调用方法
     *
     * @param obj          对象
     * @param methodName   方法名
     * @param parameterTypes 参数类型
     * @param args         参数
     * @return 方法返回值
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        if (obj == null) {
            return null;
        }
        try {
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败: " + methodName, e);
        }
    }

    /**
     * 调用无参方法
     *
     * @param obj        对象
     * @param methodName 方法名
     * @return 方法返回值
     */
    public static Object invokeMethod(Object obj, String methodName) {
        return invokeMethod(obj, methodName, new Class[0], new Object[0]);
    }

    /**
     * 调用静态方法
     *
     * @param clazz        类
     * @param methodName   方法名
     * @param parameterTypes 参数类型
     * @param args         参数
     * @return 方法返回值
     */
    public static Object invokeStatic(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("调用静态方法失败: " + methodName, e);
        }
    }

    /**
     * 调用静态方法（无参）
     *
     * @param clazz      类
     * @param methodName 方法名
     * @return 方法返回值
     */
    public static Object invokeStatic(Class<?> clazz, String methodName) {
        return invokeStatic(clazz, methodName, new Class[0], new Object[0]);
    }

    /**
     * 创建新实例
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 新实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建实例失败: " + clazz.getName(), e);
        }
    }

    /**
     * 设置字段可访问
     *
     * @param field 字段
     */
    public static void setAccessible(Field field) {
        if (field != null) {
            field.setAccessible(true);
        }
    }

    /**
     * 设置方法可访问
     *
     * @param method 方法
     */
    public static void setAccessible(Method method) {
        if (method != null) {
            method.setAccessible(true);
        }
    }

    /**
     * 判断是否为静态方法
     *
     * @param method 方法
     * @return true 如果是静态方法
     */
    public static boolean isStatic(Method method) {
        return method != null && Modifier.isStatic(method.getModifiers());
    }

    /**
     * 判断是否为静态字段
     *
     * @param field 字段
     * @return true 如果是静态字段
     */
    public static boolean isStatic(Field field) {
        return field != null && Modifier.isStatic(field.getModifiers());
    }

    /**
     * 判断是否为 public 方法
     *
     * @param method 方法
     * @return true 如果是 public
     */
    public static boolean isPublic(Method method) {
        return method != null && Modifier.isPublic(method.getModifiers());
    }

    /**
     * 判断是否为 public 字段
     *
     * @param field 字段
     * @return true 如果是 public
     */
    public static boolean isPublic(Field field) {
        return field != null && Modifier.isPublic(field.getModifiers());
    }

    /**
     * 判断是否为 private 字段
     *
     * @param field 字段
     * @return true 如果是 private
     */
    public static boolean isPrivate(Field field) {
        return field != null && Modifier.isPrivate(field.getModifiers());
    }

    /**
     * 获取类的所有方法
     *
     * @param clazz 类
     * @return 方法数组
     */
    public static Method[] getMethods(Class<?> clazz) {
        if (clazz == null) {
            return new Method[0];
        }
        return clazz.getMethods();
    }

    /**
     * 调用方法（使用 Method 对象）
     *
     * @param obj    对象
     * @param method 方法
     * @param args   参数
     * @return 方法返回值
     */
    public static Object invoke(Object obj, Method method, Object... args) {
        if (obj == null || method == null) {
            return null;
        }
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败: " + method.getName(), e);
        }
    }

    /**
     * 调用静态方法（使用 Method 对象）
     *
     * @param method 方法
     * @param args   参数
     * @return 方法返回值
     */
    public static Object invokeStatic(Method method, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("调用静态方法失败: " + method.getName(), e);
        }
    }
}
