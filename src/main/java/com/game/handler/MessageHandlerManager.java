package com.game.handler;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理器管理器
 * 管理所有消息处理器的注册和分发
 * 支持注解扫描自动注册
 *
 * @author Harleysama
 */
@Slf4j
public class MessageHandlerManager {

    private static final MessageHandlerManager INSTANCE = new MessageHandlerManager();

    /**
     * 消息类型 -> 处理器 映射
     */
    private final Map<Integer, MessageHandler> handlers = new ConcurrentHashMap<>();

    private MessageHandlerManager() {
    }

    /**
     * 获取单例实例
     */
    public static MessageHandlerManager getInstance() {
        return INSTANCE;
    }

    /**
     * 扫描指定包路径下的所有处理器并自动注册
     *
     * @param packageName 包名，例如 "com.game.handler"
     */
    public void scanAndRegister(String packageName) {
        log.info("开始扫描消息处理器: package={}", packageName);

        try {
            // 获取包下所有类
            Set<Class<?>> classes = getClasses(packageName);

            int registeredCount = 0;
            for (Class<?> clazz : classes) {
                // 检查是否有 @GameHandler 注解
                if (clazz.isAnnotationPresent(GameHandler.class)) {
                    GameHandler annotation = clazz.getAnnotation(GameHandler.class);

                    // 验证是否实现了 MessageHandler 接口
                    if (!MessageHandler.class.isAssignableFrom(clazz)) {
                        log.warn("类 {} 有 @GameHandler 注解但未实现 MessageHandler 接口", clazz.getName());
                        continue;
                    }

                    // 实例化处理器
                    @SuppressWarnings("unchecked")
                    Class<? extends MessageHandler> handlerClass = (Class<? extends MessageHandler>) clazz;
                    MessageHandler handler = handlerClass.getDeclaredConstructor().newInstance();

                    // 使用注解中的 messageType 值注册
                    int messageType = annotation.messageType();
                    handlers.put(messageType, handler);

                    log.info("注册消息处理器: type={}, handler={}", messageType, handler.getClass().getSimpleName());
                    registeredCount++;
                }
            }

            log.info("消息处理器扫描完成! 共注册 {} 个处理器", registeredCount);

        } catch (Exception e) {
            log.error("扫描消息处理器失败: {}", e.getMessage(), e);
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
     * 获取处理器
     */
    public MessageHandler getHandler(int messageType) {
        return handlers.get(messageType);
    }

    /**
     * 移除处理器
     */
    public void removeHandler(int messageType) {
        handlers.remove(messageType);
        log.info("移除消息处理器: type={}", messageType);
    }

    /**
     * 获取已注册的处理器数量
     */
    public int getHandlerCount() {
        return handlers.size();
    }
}
