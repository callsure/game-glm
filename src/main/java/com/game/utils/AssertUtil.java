package com.game.utils;

/**
 * 断言工具类
 * 提供断言检查的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于断言检查
 * - 快速失败：断言失败立即抛出异常
 *
 * @author Harleysama
 */
public class AssertUtil {

    /**
     * 断言对象不为 null
     *
     * @param obj       对象
     * @param errorMsg  错误消息
     * @param errorArgs 错误消息参数
     * @throws IllegalArgumentException 如果对象为 null
     */
    public static void notNull(Object obj, String errorMsg, Object... errorArgs) {
        if (obj == null) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言对象不为 null
     *
     * @param obj 对象
     * @throws IllegalArgumentException 如果对象为 null
     */
    public static void notNull(Object obj) {
        notNull(obj, "[Assertion failed] - argument must not be null");
    }

    /**
     * 断言表达式为真
     *
     * @param expression 布尔表达式
     * @param errorMsg   错误消息
     * @param errorArgs  错误消息参数
     * @throws IllegalArgumentException 如果表达式为假
     */
    public static void isTrue(boolean expression, String errorMsg, Object... errorArgs) {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言表达式为真
     *
     * @param expression 布尔表达式
     * @throws IllegalArgumentException 如果表达式为假
     */
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - expression must be true");
    }

    /**
     * 断言表达式为假
     *
     * @param expression 布尔表达式
     * @param errorMsg   错误消息
     * @param errorArgs  错误消息参数
     * @throws IllegalArgumentException 如果表达式为真
     */
    public static void isFalse(boolean expression, String errorMsg, Object... errorArgs) {
        if (expression) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言表达式为假
     *
     * @param expression 布尔表达式
     * @throws IllegalArgumentException 如果表达式为真
     */
    public static void isFalse(boolean expression) {
        isFalse(expression, "[Assertion failed] - expression must be false");
    }

    /**
     * 断言字符串不为空
     *
     * @param str       字符串
     * @param errorMsg  错误消息
     * @param errorArgs 错误消息参数
     * @throws IllegalArgumentException 如果字符串为空
     */
    public static void notEmpty(String str, String errorMsg, Object... errorArgs) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言字符串不为空
     *
     * @param str 字符串
     * @throws IllegalArgumentException 如果字符串为空
     */
    public static void notEmpty(String str) {
        notEmpty(str, "[Assertion failed] - String argument must not be empty");
    }

    /**
     * 断言集合不为空
     *
     * @param collection 集合
     * @param errorMsg   错误消息
     * @param errorArgs  错误消息参数
     * @throws IllegalArgumentException 如果集合为空
     */
    public static void notEmpty(java.util.Collection<?> collection, String errorMsg, Object... errorArgs) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 集合
     * @throws IllegalArgumentException 如果集合为空
     */
    public static void notEmpty(java.util.Collection<?> collection) {
        notEmpty(collection, "[Assertion failed] - Collection argument must not be empty");
    }

    /**
     * 断言数组不为空
     *
     * @param array     数组
     * @param errorMsg  错误消息
     * @param errorArgs 错误消息参数
     * @throws IllegalArgumentException 如果数组为空
     */
    public static void notEmpty(Object[] array, String errorMsg, Object... errorArgs) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言数组不为空
     *
     * @param array 数组
     * @throws IllegalArgumentException 如果数组为空
     */
    public static void notEmpty(Object[] array) {
        notEmpty(array, "[Assertion failed] - Array argument must not be empty");
    }

    /**
     * 断言对象为指定类型
     *
     * @param obj       对象
     * @param type      类型
     * @param errorMsg  错误消息
     * @param errorArgs 错误消息参数
     * @throws IllegalArgumentException 如果对象不是指定类型
     */
    public static void isInstanceOf(Object obj, Class<?> type, String errorMsg, Object... errorArgs) {
        notNull(type, "Type must not be null");
        if (obj != null && !type.isInstance(obj)) {
            throw new IllegalArgumentException(format(errorMsg, errorArgs));
        }
    }

    /**
     * 断言对象为指定类型
     *
     * @param obj  对象
     * @param type 类型
     * @throws IllegalArgumentException 如果对象不是指定类型
     */
    public static void isInstanceOf(Object obj, Class<?> type) {
        isInstanceOf(obj, type, "[Assertion failed] - Object must be instance of " + type.getName());
    }

    /**
     * 格式化错误消息
     *
     * @param pattern 模式
     * @param args   参数
     * @return 格式化后的消息
     */
    private static String format(String pattern, Object... args) {
        if (pattern == null) {
            return "";
        }
        if (args == null || args.length == 0) {
            return pattern;
        }
        try {
            return String.format(pattern, args);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(pattern);
            for (Object arg : args) {
                int index = sb.indexOf("{}");
                if (index >= 0) {
                    sb.replace(index, index + 2, String.valueOf(arg));
                }
            }
            return sb.toString();
        }
    }
}
