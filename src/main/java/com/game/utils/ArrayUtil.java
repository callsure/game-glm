package com.game.utils;

/**
 * 数组工具类
 * 提供数组操作的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于数组操作
 * - 空值安全：所有方法都处理 null 情况
 *
 * @author Harleysama
 */
public class ArrayUtil {

    /**
     * 空数组
     */
    private static final Object[] EMPTY_ARRAY = new Object[0];

    /**
     * 判断数组是否为空
     *
     * @param array 数组
     * @return true 如果为 null 或长度为0
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否非空
     *
     * @param array 数组
     * @return true 如果不为 null 且长度大于0
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 判断数组是否为空（byte 数组版本）
     *
     * @param array byte 数组
     * @return true 如果为 null 或长度为0
     */
    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否非空（byte 数组版本）
     *
     * @param array byte 数组
     * @return true 如果不为 null 且长度大于0
     */
    public static boolean isNotEmpty(byte[] array) {
        return !isEmpty(array);
    }

    /**
     * 获取数组长度
     *
     * @param array 数组
     * @return 数组长度，为 null 返回 0
     */
    public static int length(Object[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * 判断元素是否在数组中
     *
     * @param array   数组
     * @param element 元素
     * @param <T>     数组类型
     * @return true 如果包含
     */
    public static <T> boolean contains(T[] array, T element) {
        if (isEmpty(array)) {
            return false;
        }
        for (T item : array) {
            if (item == null) {
                if (element == null) {
                    return true;
                }
            } else {
                if (item.equals(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将 List 转为数组
     *
     * @param list     List
     * @param type     数组类型
     * @param <T>      元素类型
     * @return 数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(java.util.List<T> list, Class<T> type) {
        if (list == null) {
            return (T[]) java.lang.reflect.Array.newInstance(type, 0);
        }
        return list.toArray((T[]) java.lang.reflect.Array.newInstance(type, list.size()));
    }

    /**
     * 获取空数组
     *
     * @param type 数组类型
     * @param <T>  元素类型
     * @return 空数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> type) {
        return (T[]) java.lang.reflect.Array.newInstance(type, 0);
    }
}
