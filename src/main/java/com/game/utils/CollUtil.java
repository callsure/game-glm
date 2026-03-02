package com.game.utils;

import java.util.*;

/**
 * 集合工具类
 * 提供集合操作的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于集合操作
 * - 空值安全：所有方法都处理 null 情况
 *
 * @author Harleysama
 */
public class CollUtil {

    /**
     * 判断集合是否为空
     *
     * @param collection 集合
     * @return true 如果为 null 或空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否非空
     *
     * @param collection 集合
     * @return true 如果不为 null 且非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断 Map 是否为空
     *
     * @param map Map
     * @return true 如果为 null 或空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否非空
     *
     * @param map Map
     * @return true 如果不为 null 且非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断数组是否为空
     *
     * @param array 数组
     * @return true 如果为 null 或空
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否非空
     *
     * @param array 数组
     * @return true 如果不为 null 且非空
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 如果集合为空，返回默认集合
     *
     * @param collection       集合
     * @param defaultCollection 默认集合
     * @param <T>              元素类型
     * @return 集合或默认集合
     */
    public static <T> Collection<T> emptyIfNull(Collection<T> collection, Collection<T> defaultCollection) {
        return isEmpty(collection) ? defaultCollection : collection;
    }

    /**
     * 如果集合为空，返回空集合
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 集合或空集合
     */
    public static <T> List<T> emptyIfNull(List<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

    /**
     * 新建 ArrayList
     *
     * @param <T> 元素类型
     * @return ArrayList
     */
    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * 新建 ArrayList 并指定初始容量
     *
     * @param initialCapacity 初始容量
     * @param <T>             元素类型
     * @return ArrayList
     */
    public static <T> ArrayList<T> newArrayList(int initialCapacity) {
        return new ArrayList<>(initialCapacity);
    }

    /**
     * 新建 HashSet
     *
     * @param <T> 元素类型
     * @return HashSet
     */
    public static <T> HashSet<T> newHashSet() {
        return new HashSet<>();
    }

    /**
     * 新建 HashMap
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return HashMap
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * 获取集合的第一个元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 第一个元素，不存在返回 null
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.iterator().next();
    }

    /**
     * 获取集合的最后一个元素
     *
     * @param list 列表
     * @param <T>  元素类型
     * @return 最后一个元素，不存在返回 null
     */
    public static <T> T getLast(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * 将数组转为列表
     *
     * @param array 数组
     * @param <T>   元素类型
     * @return 列表
     */
    @SafeVarargs
    public static <T> List<T> toList(T... array) {
        if (array == null || array.length == 0) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(array.length);
        Collections.addAll(list, array);
        return list;
    }
}
