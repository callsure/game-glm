package com.game.event;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 游戏事件管理器
 * 负责事件的发布、订阅和分发，支持同步和异步执行
 * <p>
 * 设计原则：
 * - 单例模式：确保全局唯一的事件总线
 * - 线程安全：使用并发容器保证多线程环境下的安全性
 * - 开闭原则：支持动态注册监听器，无需修改核心代码
 *
 * @author Harleysama
 */
@Slf4j
public class EventManager {

    private static final EventManager INSTANCE = new EventManager();

    /**
     * 事件类型 -> 监听器列表 映射
     * 使用 CopyOnWriteArrayList 确保遍历时的线程安全
     */
    private final Map<Class<? extends GameEvent>, List<EventListenerWrapper>> listeners = new ConcurrentHashMap<>();

    /**
     * 异步事件执行线程池
     */
    private final ExecutorService asyncExecutor;

    /**
     * 私有构造函数
     */
    private EventManager() {
        // 初始化异步线程池（核心线程数 = CPU 核心数）
        int cores = Runtime.getRuntime().availableProcessors();
        this.asyncExecutor = Executors.newFixedThreadPool(cores, new EventThreadFactory());
        log.info("事件管理器初始化完成! 异步线程池大小: {}", cores);
    }

    /**
     * 获取单例实例
     */
    public static EventManager getInstance() {
        return INSTANCE;
    }

    /**
     * 发布事件
     * 同步调用所有监听器
     *
     * @param event 事件对象
     */
    public void publish(GameEvent event) {
        if (event == null) {
            log.warn("发布的事件为 null，忽略处理");
            return;
        }

        List<EventListenerWrapper> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null || eventListeners.isEmpty()) {
            log.debug("事件 {} 没有监听器", event.getClass().getSimpleName());
            return;
        }

        log.debug("发布事件: {}, 监听器数量: {}", event.getClass().getSimpleName(), eventListeners.size());

        for (EventListenerWrapper wrapper : eventListeners) {
            invokeListener(wrapper, event);
        }
    }

    /**
     * 发布事件（异步）
     * 异步监听器在独立线程池中执行，同步监听器在当前线程执行
     *
     * @param event 事件对象
     */
    public void publishAsync(GameEvent event) {
        if (event == null) {
            log.warn("发布的事件为 null，忽略处理");
            return;
        }

        List<EventListenerWrapper> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null || eventListeners.isEmpty()) {
            log.debug("事件 {} 没有监听器", event.getClass().getSimpleName());
            return;
        }

        log.debug("发布异步事件: {}, 监听器数量: {}", event.getClass().getSimpleName(), eventListeners.size());

        for (EventListenerWrapper wrapper : eventListeners) {
            if (wrapper.isAsync()) {
                // 异步执行
                asyncExecutor.execute(() -> invokeListener(wrapper, event));
            } else {
                // 同步执行
                invokeListener(wrapper, event);
            }
        }
    }

    /**
     * 调用监听器方法
     *
     * @param wrapper 监听器包装器
     * @param event   事件对象
     */
    private void invokeListener(EventListenerWrapper wrapper, GameEvent event) {
        try {
            long startTime = System.currentTimeMillis();
            wrapper.invoke(event);
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed > 100) {
                log.warn("监听器执行较慢: listener={}, event={},耗时={}ms",
                        wrapper.getMethod().getName(), event.getClass().getSimpleName(), elapsed);
            }

        } catch (Exception e) {
            log.error("监听器执行异常: listener={}, event={}",
                    wrapper.getMethod().getName(), event.getClass().getSimpleName(), e);
        }
    }

    /**
     * 注册监听器
     *
     * @param listener 监听器对象
     */
    public void register(Object listener) {
        if (listener == null) {
            log.warn("监听器对象为 null，忽略注册");
            return;
        }

        Class<?> listenerClass = listener.getClass();
        Method[] methods = listenerClass.getDeclaredMethods();

        int registeredCount = 0;

        for (Method method : methods) {
            // 检查是否有 @Subscribe 注解
            if (method.isAnnotationPresent(Subscribe.class)) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);

                // 验证方法签名：必须只有一个参数，且参数类型继承自 GameEvent
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    log.warn("监听器方法参数数量错误: {}.{} (需要1个参数)",
                            listenerClass.getSimpleName(), method.getName());
                    continue;
                }

                Class<?> parameterType = parameterTypes[0];
                if (!GameEvent.class.isAssignableFrom(parameterType)) {
                    log.warn("监听器方法参数类型错误: {}.{} (需要继承 GameEvent)",
                            listenerClass.getSimpleName(), method.getName());
                    continue;
                }

                @SuppressWarnings("unchecked")
                Class<? extends GameEvent> eventType = (Class<? extends GameEvent>) parameterType;

                // 创建监听器包装器
                EventListenerWrapper wrapper = EventListenerWrapper.of(
                        listener,
                        method,
                        annotation.async(),
                        annotation.priority(),
                        eventType
                );

                // 注册到监听器列表
                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(wrapper);

                // 按优先级排序
                listeners.get(eventType).sort(Comparator.comparingInt(EventListenerWrapper::getPriority));

                registeredCount++;
                log.info("注册监听器: event={}, method={}, async={}, priority={}",
                        eventType.getSimpleName(), method.getName(), annotation.async(), annotation.priority());
            }
        }

        if (registeredCount > 0) {
            log.info("监听器注册完成: class={}, methods={}", listenerClass.getSimpleName(), registeredCount);
        }
    }

    /**
     * 扫描指定包路径下的所有监听器并自动注册
     *
     * @param packageName 包名，例如 "com.game.listener"
     */
    public void scanAndRegister(String packageName) {
        log.info("开始扫描事件监听器: package={}", packageName);

        try {
            // 获取包下所有类
            Set<Class<?>> classes = getClasses(packageName);

            int registeredCount = 0;
            for (Class<?> clazz : classes) {
                try {
                    // 实例化监听器
                    Object listener = clazz.getDeclaredConstructor().newInstance();
                    register(listener);
                    registeredCount++;
                } catch (Exception e) {
                    log.warn("实例化监听器失败: {}", clazz.getName(), e);
                }
            }

            log.info("事件监听器扫描完成! 共注册 {} 个监听器类", registeredCount);

        } catch (Exception e) {
            log.error("扫描事件监听器失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取指定包下的所有类
     */
    private Set<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();

        // 获取包路径
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String protocol = url.getProtocol();

            if ("file".equals(protocol)) {
                // 处理文件系统路径
                String filePath = url.getFile();
                findClassesInDirectory(new File(filePath), packageName, classes);
            }
        }

        return classes;
    }

    /**
     * 在目录中递归查找类文件
     */
    private void findClassesInDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                findClassesInDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                // 加载类
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    log.warn("无法加载类: {}", className);
                }
            }
        }
    }

    /**
     * 注销监听器
     *
     * @param listener 监听器对象
     */
    public void unregister(Object listener) {
        if (listener == null) {
            return;
        }

        int removedCount = 0;
        for (List<EventListenerWrapper> listenerList : listeners.values()) {
            int beforeSize = listenerList.size();
            listenerList.removeIf(wrapper -> wrapper.getListener().equals(listener));
            removedCount += (beforeSize - listenerList.size());
        }

        if (removedCount > 0) {
            log.info("监听器注销完成: class={}, methods={}", listener.getClass().getSimpleName(), removedCount);
        }
    }

    /**
     * 获取指定事件的监听器数量
     *
     * @param eventType 事件类型
     * @return 监听器数量
     */
    public int getListenerCount(Class<? extends GameEvent> eventType) {
        List<EventListenerWrapper> eventListeners = listeners.get(eventType);
        return eventListeners == null ? 0 : eventListeners.size();
    }

    /**
     * 获取所有监听器总数
     *
     * @return 监听器总数
     */
    public int getTotalListenerCount() {
        return listeners.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * 关闭事件管理器
     * 停止异步线程池
     */
    public void shutdown() {
        log.info("事件管理器关闭中...");
        asyncExecutor.shutdown();
        log.info("事件管理器已关闭");
    }

    /**
     * 事件线程工厂
     * 用于创建自定义命名的线程
     */
    private static class EventThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "event-thread-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
