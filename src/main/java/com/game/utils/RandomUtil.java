package com.game.utils;

import java.util.Random;

/**
 * 随机工具类
 * 提供随机数生成的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于随机数生成
 * - 线程安全：使用 ThreadLocalRandom
 *
 * @author Harleysama
 */
public class RandomUtil {

    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 生成随机整数
     *
     * @return 随机整数
     */
    public static int randomInt() {
        return RANDOM.nextInt();
    }

    /**
     * 生成指定范围内的随机整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     * @return 随机整数
     */
    public static int randomInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return min + RANDOM.nextInt(max - min);
    }

    /**
     * 生成随机整数（0 到 bound）
     *
     * @param bound 上界（不包含）
     * @return 随机整数
     */
    public static int randomInt(int bound) {
        return RANDOM.nextInt(bound);
    }

    /**
     * 生成随机长整数
     *
     * @return 随机长整数
     */
    public static long randomLong() {
        return RANDOM.nextLong();
    }

    /**
     * 生成指定范围内的随机长整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     * @return 随机长整数
     */
    public static long randomLong(long min, long max) {
        if (min >= max) {
            return min;
        }
        return min + (long) (RANDOM.nextDouble() * (max - min));
    }

    /**
     * 生成随机浮点数
     *
     * @return 随机浮点数 [0.0, 1.0)
     */
    public static double randomDouble() {
        return RANDOM.nextDouble();
    }

    /**
     * 生成指定范围内的随机浮点数
     *
     * @param min 最小值（包含）
     * @param max 最大值（不包含）
     * @return 随机浮点数
     */
    public static double randomDouble(double min, double max) {
        if (min >= max) {
            return min;
        }
        return min + RANDOM.nextDouble() * (max - min);
    }

    /**
     * 生成随机布尔值
     *
     * @return 随机布尔值
     */
    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    /**
     * 生成随机字节数组
     *
     * @param length 长度
     * @return 字节数组
     */
    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * 生成随机字符串（数字和小写字母）
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(length, "abcdefghijklmnopqrstuvwxyz0123456789");
    }

    /**
     * 生成随机字符串（指定字符集）
     *
     * @param length 长度
     * @param chars  字符集
     * @return 随机字符串
     */
    public static String randomString(int length, String chars) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 从列表中随机选择一个元素
     *
     * @param list 列表
     * @param <T>   元素类型
     * @return 随机元素，列表为空返回 null
     */
    public static <T> T randomElement(java.util.List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(RANDOM.nextInt(list.size()));
    }

    /**
     * 从数组中随机选择一个元素
     *
     * @param array 数组
     * @param <T>    元素类型
     * @return 随机元素，数组为空返回 null
     */
    public static <T> T randomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[RANDOM.nextInt(array.length)];
    }

    /**
     * 随机打乱数组
     *
     * @param array 数组
     * @param <T>    元素类型
     */
    public static <T> void shuffle(T[] array) {
        if (array == null || array.length <= 1) {
            return;
        }
        for (int i = array.length - 1; i > 0; i--) {
            int index = RANDOM.nextInt(i + 1);
            T temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    /**
     * 随机打乱列表
     *
     * @param list 列表
     * @param <T>  元素类型
     */
    public static <T> void shuffle(java.util.List<T> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        for (int i = list.size() - 1; i > 0; i--) {
            int index = RANDOM.nextInt(i + 1);
            list.set(i, list.set(index, list.get(i)));
        }
    }
}
