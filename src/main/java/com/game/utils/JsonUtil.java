package com.game.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON 序列化工具类
 * 基于 Jackson 实现
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于 JSON 序列化
 * - 线程安全：ObjectMapper 线程安全
 * - 简化 API：提供便捷的静态方法
 * <p>
 * 使用示例：
 * <pre>
 * // 对象转 JSON
 * String json = JsonUtil.toJson(user);
 *
 * // JSON 转对象
 * User user = JsonUtil.toJava(json, User.class);
 *
 * // JSON 转集合
 * List<User> users = JsonUtil.toJavaList(json, User.class);
 * </pre>
 *
 * @author Harleysama
 */
@Slf4j
public class JsonUtil {

    /**
     * ObjectMapper 实例（线程安全）
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 配置序列化
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 配置反序列化
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 其他配置
        mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
        mapper.configure(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL, true);

        // 注册 Java 8 时间模块
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 获取 ObjectMapper 实例
     * 用于自定义序列化需求
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }

    // ==================== 序列化 ====================

    /**
     * 对象转 JSON 字符串
     *
     * @param object 对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("对象转 JSON 失败", e);
            throw new RuntimeException("对象转 JSON 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 对象转 JSON 字节数组
     *
     * @param object 对象
     * @return JSON 字节数组
     */
    public static byte[] toBytes(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return mapper.writeValueAsBytes(object);
        } catch (Exception e) {
            log.error("对象转 JSON 字节数组失败", e);
            throw new RuntimeException("对象转 JSON 字节数组失败: " + e.getMessage(), e);
        }
    }

    // ==================== 反序列化 ====================

    /**
     * JSON 字符串转对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 对象实例
     */
    public static <T> T toJava(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        if (clazz.equals(String.class)) {
            return clazz.cast(json);
        }
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON 转对象失败: json={}, clazz={}", json, clazz.getName(), e);
            throw new RuntimeException("JSON 转对象失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字符串转对象（支持泛型）
     *
     * @param json     JSON 字符串
     * @param typeRef 类型引用
     * @param <T>      泛型类型
     * @return 对象实例
     */
    public static <T> T toJava(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("JSON 转对象失败: json={}, typeRef={}", json, typeRef.getType().getTypeName(), e);
            throw new RuntimeException("JSON 转对象失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字节数组转对象
     *
     * @param bytes JSON 字节数组
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 对象实例
     */
    public static <T> T toJava(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, clazz);
        } catch (Exception e) {
            log.error("JSON 字节数组转对象失败: clazz={}", clazz.getName(), e);
            throw new RuntimeException("JSON 字节数组转对象失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字节数组转对象（支持泛型）
     *
     * @param bytes    JSON 字节数组
     * @param typeRef  类型引用
     * @param <T>      泛型类型
     * @return 对象实例
     */
    public static <T> T toJava(byte[] bytes, TypeReference<T> typeRef) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, typeRef);
        } catch (Exception e) {
            log.error("JSON 字节数组转对象失败: typeRef={}", typeRef.getType().getTypeName(), e);
            throw new RuntimeException("JSON 字节数组转对象失败: " + e.getMessage(), e);
        }
    }

    // ==================== 集合反序列化 ====================

    /**
     * JSON 字符串转 List
     *
     * @param json  JSON 字符串
     * @param clazz 元素类型
     * @param <T>   元素泛型类型
     * @return List 对象
     */
    public static <T> List<T> toJavaList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("JSON 转 List 失败: json={}, clazz={}", json, clazz.getName(), e);
            throw new RuntimeException("JSON 转 List 失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字节数组转 List
     *
     * @param bytes JSON 字节数组
     * @param clazz 元素类型
     * @param <T>   元素泛型类型
     * @return List 对象
     */
    public static <T> List<T> toJavaList(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("JSON 字节数组转 List 失败: clazz={}", clazz.getName(), e);
            throw new RuntimeException("JSON 字节数组转 List 失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字符串转 Set
     *
     * @param json  JSON 字符串
     * @param clazz 元素类型
     * @param <E>   元素泛型类型
     * @return Set 对象
     */
    public static <E> Set<E> toJavaSet(String json, Class<E> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(Set.class, clazz));
        } catch (Exception e) {
            log.error("JSON 转 Set 失败: json={}, clazz={}", json, clazz.getName(), e);
            throw new RuntimeException("JSON 转 Set 失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字符串转 Map
     *
     * @param json      JSON 字符串
     * @param keyType   键类型
     * @param valueType 值类型
     * @param <K>       键泛型类型
     * @param <V>       值泛型类型
     * @return Map 对象
     */
    public static <K, V> Map<K, V> toJavaMap(String json, Class<K> keyType, Class<V> valueType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, keyType, valueType));
        } catch (Exception e) {
            log.error("JSON 转 Map 失败: json={}, keyType={}, valueType={}", json, keyType.getName(), valueType.getName(), e);
            throw new RuntimeException("JSON 转 Map 失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 字节数组转 Map
     *
     * @param bytes     JSON 字节数组
     * @param keyType   键类型
     * @param valueType 值类型
     * @param <K>       键泛型类型
     * @param <V>       值泛型类型
     * @return Map 对象
     */
    public static <K, V> Map<K, V> toJavaMap(byte[] bytes, Class<K> keyType, Class<V> valueType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, mapper.getTypeFactory().constructMapType(Map.class, keyType, valueType));
        } catch (Exception e) {
            log.error("JSON 字节数组转 Map 失败: keyType={}, valueType={}", keyType.getName(), valueType.getName(), e);
            throw new RuntimeException("JSON 字节数组转 Map 失败: " + e.getMessage(), e);
        }
    }

    // ==================== 对象转换 ====================

    /**
     * 对象类型转换
     *
     * @param object 源对象
     * @param clazz  目标类型
     * @param <T>    目标泛型类型
     * @return 转换后的对象
     */
    public static <T> T convertValue(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        try {
            return mapper.convertValue(object, clazz);
        } catch (Exception e) {
            log.error("对象类型转换失败: clazz={}, object={}", clazz.getName(), object, e);
            throw new RuntimeException("对象类型转换失败: " + e.getMessage(), e);
        }
    }

    // ==================== 高级功能 ====================

    /**
     * 注册多态类型的子类
     * 用于处理多态序列化
     *
     * @param classes 子类类型数组
     */
    public static void registerSubtypes(Class<?>... classes) {
        mapper.registerSubtypes(classes);
    }

    /**
     * 更新 ObjectMapper 配置
     * 用于自定义配置
     *
     * @param consumer 配置消费者
     */
    public static void configureMapper(java.util.function.Consumer<ObjectMapper> consumer) {
        consumer.accept(mapper);
    }
}
