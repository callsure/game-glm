package com.game.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 日期时间工具类（Java 8 时间 API 版本）
 * 提供日期时间的便捷操作方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于日期时间操作
 * - 不可变对象：所有方法返回新实例
 * - 线程安全：使用 Java 8 不可变时间类
 * <p>
 * 使用示例：
 * <pre>
 * long now = DateUtil.getSecondLevelMillis();
 * String dateStr = DateUtil.format(LocalDateTime.now());
 * LocalDateTime ldt = DateUtil.parse("2024-03-02 12:00:00");
 * </pre>
 *
 * @author Harleysama
 */
public class DateUtil {

    // ==================== 常量 ====================

    /**
     * 时间常量（毫秒）
     */
    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    /**
     * 一秒钟对应的纳秒数
     */
    public static final long NANO_PER_SECOND = 1_000_000_000L;

    /**
     * 默认时区
     */
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * 默认日期格式
     */
    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_SECOND = "yyyy-MM-dd HH:mm:ss";

    /**
     * 预定义格式化器（线程安全）
     */
    private static final DateTimeFormatter FORMAT_DATE_FORMATTER = DateTimeFormatter.ofPattern(FORMAT_DATE);
    private static final DateTimeFormatter FORMAT_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(FORMAT_MINUTE);
    private static final DateTimeFormatter FORMAT_SECOND_FORMATTER = DateTimeFormatter.ofPattern(FORMAT_SECOND);

    /**
     * 秒级精度的毫秒数（用于缓存时间戳）
     */
    private static volatile long secondLevelMillis;

    /**
     * 定时更新线程池
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "seconds-updater");
        thread.setDaemon(true);
        return thread;
    });

    static {
        secondLevelMillis = System.currentTimeMillis();
        // 每秒更新一次秒级时间戳
        scheduler.scheduleAtFixedRate(() -> secondLevelMillis = System.currentTimeMillis(), 1, 1, TimeUnit.SECONDS);
    }

    // ==================== 获取当前时间 ====================

    /**
     * 获取秒级精度的当前时间戳（毫秒）
     * 用于缓存等需要时间戳但不追求毫秒级精度的场景
     *
     * @return 秒级精度的毫秒数
     */
    public static long getSecondLevelMillis() {
        return secondLevelMillis;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前 Instant
     *
     * @return Instant
     */
    public static Instant nowInstant() {
        return Instant.now();
    }

    /**
     * 获取当前 LocalDateTime
     *
     * @return LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前 LocalDate
     *
     * @return LocalDate
     */
    public static LocalDate nowDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前 LocalTime
     *
     * @return LocalTime
     */
    public static LocalTime nowTime() {
        return LocalTime.now();
    }

    // ==================== 日期转换 ====================

    /**
     * Date 转 LocalDateTime
     *
     * @param date Date 对象
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE);
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime LocalDateTime
     * @return Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(DEFAULT_ZONE).toInstant());
    }

    /**
     * 时间戳转 LocalDateTime
     *
     * @param millis 时间戳（毫秒）
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_ZONE);
    }

    /**
     * LocalDateTime 转时间戳
     *
     * @param localDateTime LocalDateTime
     * @return 时间戳（毫秒）
     */
    public static long toMillis(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0;
        }
        return localDateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    // ==================== 日期加减 ====================

    /**
     * 日期增加天数
     *
     * @param datetime 原日期时间
     * @param days     增加的天数
     * @return 新日期时间
     */
    public static LocalDateTime plusDays(LocalDateTime datetime, long days) {
        return datetime.plusDays(days);
    }

    /**
     * 日期增加小时
     *
     * @param datetime 原日期时间
     * @param hours    增加的小时数
     * @return 新日期时间
     */
    public static LocalDateTime plusHours(LocalDateTime datetime, long hours) {
        return datetime.plusHours(hours);
    }

    /**
     * 日期增加分钟
     *
     * @param datetime 原日期时间
     * @param minutes  增加的分钟数
     * @return 新日期时间
     */
    public static LocalDateTime plusMinutes(LocalDateTime datetime, long minutes) {
        return datetime.plusMinutes(minutes);
    }

    /**
     * 日期增加秒
     *
     * @param datetime 原日期时间
     * @param seconds  增加的秒数
     * @return 新日期时间
     */
    public static LocalDateTime plusSeconds(LocalDateTime datetime, long seconds) {
        return datetime.plusSeconds(seconds);
    }

    // ==================== 日期比较 ====================

    /**
     * 判断两个日期是否为同一天
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return true 如果是同一天
     */
    public static boolean isSameDay(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.toLocalDate().equals(date2.toLocalDate());
    }

    /**
     * 判断两个时间戳是否为同一天
     *
     * @param timestamp1 时间戳1
     * @param timestamp2 时间戳2
     * @return true 如果是同一天
     */
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        return formatTime(timestamp1, "yyyyMMdd").equals(formatTime(timestamp2, "yyyyMMdd"));
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期之间的小时差
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期之间的分钟差
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    // ==================== 日期边界 ====================

    /**
     * 获取当天的零点时间
     *
     * @param datetime 日期时间
     * @return 零点时间
     */
    public static LocalDateTime getMidnight(LocalDateTime datetime) {
        return datetime.toLocalDate().atStartOfDay();
    }

    /**
     * 获取当天的最后时刻时间
     *
     * @param datetime 日期时间
     * @return 最后时刻时间（23:59:59.999999999）
     */
    public static LocalDateTime getEndOfDay(LocalDateTime datetime) {
        return datetime.toLocalDate().atTime(LocalTime.MAX);
    }

    /**
     * 获取今天的零点时间
     *
     * @return 零点时间
     */
    public static LocalDateTime todayStart() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * 获取今天的最后时刻时间
     *
     * @return 最后时刻时间
     */
    public static LocalDateTime todayEnd() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    // ==================== 格式化 ====================

    /**
     * 格式化日期时间为字符串
     *
     * @param datetime 日期时间
     * @param pattern  格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime datetime, String pattern) {
        if (datetime == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(pattern).format(datetime);
    }

    /**
     * 格式化日期为字符串
     *
     * @param date 日期
     * @return 格式化后的字符串（yyyy-MM-dd）
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return FORMAT_DATE_FORMATTER.format(date);
    }

    /**
     * 格式化日期时间为字符串（秒级精度）
     *
     * @param datetime 日期时间
     * @return 格式化后的字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(LocalDateTime datetime) {
        if (datetime == null) {
            return null;
        }
        return FORMAT_SECOND_FORMATTER.format(datetime);
    }

    /**
     * 格式化时间戳为字符串
     *
     * @param millis  时间戳（毫秒）
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatTime(long millis, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(toLocalDateTime(millis));
    }

    /**
     * 格式化时间戳为日期字符串
     *
     * @param millis 时间戳（毫秒）
     * @return 格式化后的字符串（yyyy-MM-dd）
     */
    public static String formatDate(long millis) {
        return FORMAT_DATE_FORMATTER.format(toLocalDateTime(millis));
    }

    /**
     * 格式化时间戳为日期时间字符串
     *
     * @param millis 时间戳（毫秒）
     * @return 格式化后的字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(long millis) {
        return FORMAT_SECOND_FORMATTER.format(toLocalDateTime(millis));
    }

    // ==================== 解析 ====================

    /**
     * 解析字符串为日期时间
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDateTime
     */
    public static LocalDateTime parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析字符串为日期时间（秒级精度）
     *
     * @param dateStr 日期字符串（yyyy-MM-dd HH:mm:ss）
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, FORMAT_SECOND_FORMATTER);
    }

    /**
     * 解析字符串为日期
     *
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, FORMAT_DATE_FORMATTER);
    }

    // ==================== 兼容旧 API ====================

    /**
     * 获取当前 Date（兼容旧代码）
     *
     * @return Date
     */
    public static Date nowAsDate() {
        return new Date();
    }

    /**
     * 日期增加天数（兼容旧代码）
     *
     * @param date 旧 Date 对象
     * @param days 增加的天数
     * @return 新 Date 对象
     */
    public static Date addDays(Date date, int days) {
        return toDate(toLocalDateTime(date).plusDays(days));
    }

    /**
     * 日期增加小时（兼容旧代码）
     *
     * @param date  旧 Date 对象
     * @param hours 增加的小时数
     * @return 新 Date 对象
     */
    public static Date addHours(Date date, int hours) {
        return toDate(toLocalDateTime(date).plusHours(hours));
    }

    /**
     * 日期增加分钟（兼容旧代码）
     *
     * @param date    旧 Date 对象
     * @param minutes 增加的分钟数
     * @return 新 Date 对象
     */
    public static Date addMinutes(Date date, int minutes) {
        return toDate(toLocalDateTime(date).plusMinutes(minutes));
    }

    /**
     * 判断两个 Date 是否为同一天（兼容旧代码）
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return true 如果是同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return isSameDay(toLocalDateTime(date1), toLocalDateTime(date2));
    }

    /**
     * 获取当天的零点时间（兼容旧代码）
     *
     * @param date Date 对象
     * @return 零点时间
     */
    public static Date getMidnight(Date date) {
        return toDate(getMidnight(toLocalDateTime(date)));
    }

    /**
     * 获取当天的最后时刻时间（兼容旧代码）
     *
     * @param date Date 对象
     * @return 最后时刻时间
     */
    public static Date getEndOfDay(Date date) {
        return toDate(getEndOfDay(toLocalDateTime(date)));
    }

    /**
     * 格式化日期为字符串（兼容旧代码）
     *
     * @param date   Date 对象
     * @param format 格式模式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        return format(toLocalDateTime(date), format);
    }

    /**
     * 格式化日期为 "yyyy-MM-dd HH:mm:ss" 格式（兼容旧代码）
     *
     * @param date Date 对象
     * @return 格式化后的字符串
     */
    public static String dateToStringWithSeconds(Date date) {
        if (date == null) {
            return null;
        }
        return formatDateTime(toLocalDateTime(date));
    }

    /**
     * 格式化日期为 "yyyy-MM-dd" 格式（兼容旧代码）
     *
     * @param date Date 对象
     * @return 格式化后的字符串
     */
    public static String dateToString(Date date) {
        if (date == null) {
            return null;
        }
        return formatDate(toLocalDateTime(date).toLocalDate());
    }

    /**
     * 格式化时间戳为字符串（兼容旧代码）
     *
     * @param millis 时间戳
     * @return 格式化后的字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String dateToStringWithSeconds(long millis) {
        return formatDateTime(millis);
    }

    /**
     * 格式化时间戳为字符串（兼容旧代码）
     *
     * @param millis 时间戳
     * @return 格式化后的字符串（yyyy-MM-dd）
     */
    public static String dateToString(long millis) {
        return formatDate(millis);
    }
}
