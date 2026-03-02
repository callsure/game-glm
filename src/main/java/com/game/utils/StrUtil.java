package com.game.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 字符串工具类
 * 提供字符串操作的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于字符串操作
 * - 空值安全：所有方法都处理 null 情况
 *
 * @author Harleysama
 */
public class StrUtil {

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 点号
     */
    public static final String DOT = ".";

    /**
     * 横线
     */
    public static final String DASHED = "-";

    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";

    /**
     * 斜线
     */
    public static final String SLASH = "/";

    /**
     * 反斜线
     */
    public static final String BACKSLASH = "\\";

    /**
     * 空格
     */
    public static final String SPACE = " ";

    /**
     * 换行符
     */
    public static final String NEWLINE = "\n";

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return true 如果为 null 或空字符串
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return true 如果不为 null 且非空字符串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白
     *
     * @param str 字符串
     * @return true 如果为 null 或全为空白字符
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str 字符串
     * @return true 如果不为 null 且不全为空白字符
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 如果字符串为空，返回默认值
     *
     * @param str        字符串
     * @param defaultStr 默认值
     * @return 字符串或默认值
     */
    public static String emptyIfNull(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 如果字符串为空，返回空字符串
     *
     * @param str 字符串
     * @return 字符串或空字符串
     */
    public static String emptyIfNull(String str) {
        return str == null ? EMPTY : str;
    }

    /**
     * 如果字符串为空白，返回默认值
     *
     * @param str        字符串
     * @param defaultStr 默认值
     * @return 字符串或默认值
     */
    public static String blankIfNull(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    /**
     * 将字符串首字母大写
     *
     * @param str 字符串
     * @return 首字母大写后的字符串
     */
    public static String upperFirst(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 将字符串首字母小写
     *
     * @param str 字符串
     * @return 首字母小写后的字符串
     */
    public static String lowerFirst(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 格式化字符串
     *
     * @param format 格式字符串
     * @param args   参数
     * @return 格式化后的字符串
     */
    public static String format(String format, Object... args) {
        if (format == null) {
            return EMPTY;
        }
        if (args == null || args.length == 0) {
            return format;
        }
        return String.format(format, args);
    }

    /**
     * 去除字符串两端空白
     *
     * @param str 字符串
     * @return 去除两端空白后的字符串
     */
    public static String trim(String str) {
        if (str == null) {
            return null;
        }
        return str.trim();
    }

    /**
     * 判断字符串是否相等
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return true 如果相等（都为 null 也返回 true）
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 判断字符串是否相等（忽略大小写）
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return true 如果忽略大小写后相等
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 判断字符串是否包含指定字符串
     *
     * @param str       字符串
     * @param searchStr 搜索字符串
     * @return true 如果包含
     */
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.contains(searchStr);
    }

    /**
     * 判断字符串是否包含指定字符
     *
     * @param str        字符串
     * @param searchChar 搜索字符
     * @return true 如果包含
     */
    public static boolean contains(String str, char searchChar) {
        if (str == null) {
            return false;
        }
        return str.indexOf(searchChar) >= 0;
    }

    /**
     * 字符串拼接
     *
     * @param strs 字符串数组
     * @return 拼接后的字符串
     */
    public static String join(String... strs) {
        if (strs == null || strs.length == 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            if (str != null) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * 字符串拼接（使用分隔符）
     *
     * @param delimiter 分隔符
     * @param strs      字符串数组
     * @return 拼接后的字符串
     */
    public static String join(String delimiter, String... strs) {
        if (strs == null || strs.length == 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (strs[i] != null) {
                sb.append(strs[i]);
            }
            if (i < strs.length - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * 字符串拼接（使用分隔符，集合版本）
     *
     * @param delimiter 分隔符
     * @param strs      字符串集合
     * @return 拼接后的字符串
     */
    public static String join(String delimiter, Iterable<String> strs) {
        if (strs == null) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String str : strs) {
            if (str != null) {
                if (!first) {
                    sb.append(delimiter);
                }
                sb.append(str);
                first = false;
            }
        }
        return sb.toString();
    }

    /**
     * 将字节数组转为字符串
     *
     * @param bytes   字节数组
     * @param charset 字符集
     * @return 字符串
     */
    public static String str(byte[] bytes, Charset charset) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, charset);
    }

    /**
     * 将字节数组转为字符串（UTF-8）
     *
     * @param bytes 字节数组
     * @return 字符串
     */
    public static String str(byte[] bytes) {
        return str(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串转为字节数组
     *
     * @param str     字符串
     * @param charset 字符集
     * @return 字节数组
     */
    public static byte[] bytes(String str, Charset charset) {
        if (str == null) {
            return null;
        }
        return str.getBytes(charset);
    }

    /**
     * 将字符串转为字节数组（UTF-8）
     *
     * @param str 字符串
     * @return 字节数组
     */
    public static byte[] bytes(String str) {
        return bytes(str, StandardCharsets.UTF_8);
    }

    /**
     * 移除字符串后缀
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 移除后缀后的字符串
     */
    public static String removeSuffix(String str, String suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return str;
        }
        if (str.endsWith(suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    /**
     * 移除字符串前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 移除前缀后的字符串
     */
    public static String removePrefix(String str, String prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return str;
        }
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }
/**     * 移除指定后缀并将首字母小写     * <p>     * 用于将类名转换为集合名，例如：UserEntity -> user     *     * @param str    字符串     * @param suffix 后缀     * @return 移除后缀并首字母小写后的字符串     */    public static String removeSufAndLowerFirst(String str, String suffix) {        String result = removeSuffix(str, suffix);        return lowerFirst(result);    }
}
