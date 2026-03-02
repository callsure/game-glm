package com.game.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类工具类
 * 提供类操作的便捷方法
 * <p>
 * 设计原则：
 * - 单一职责原则（SRP）：专注于类操作
 *
 * @author Harleysama
 */
public class ClassUtil {

    /**
     * 获取类加载器
     *
     * @return 类加载器
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 获取类名（不含包名）
     *
     * @param clazz 类
     * @return 类名
     */
    public static String getShortClassName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getShortClassName(clazz.getName());
    }

    /**
     * 获取类名（不含包名）
     *
     * @param className 完整类名
     * @return 类名
     */
    public static String getShortClassName(String className) {
        if (className == null) {
            return null;
        }
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return className.substring(lastDotIndex + 1);
        }
        return className;
    }

    /**
     * 获取包名
     *
     * @param clazz 类
     * @return 包名
     */
    public static String getPackageName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getPackageName(clazz.getName());
    }

    /**
     * 获取包名
     *
     * @param className 完整类名
     * @return 包名
     */
    public static String getPackageName(String className) {
        if (className == null) {
            return null;
        }
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return className.substring(0, lastDotIndex);
        }
        return "";
    }

    /**
     * 加载类
     *
     * @param className 类名
     * @param isInitialized 是否初始化
     * @return 类对象
     */
    public static Class<?> loadClass(String className, boolean isInitialized) {
        try {
            return Class.forName(className, isInitialized, getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("类未找到: " + className, e);
        }
    }

    /**
     * 加载类
     *
     * @param className 类名
     * @return 类对象
     */
    public static Class<?> loadClass(String className) {
        return loadClass(className, true);
    }

    /**
     * 扫描指定包下的所有类
     *
     * @param packageName 包名
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = getContextClassLoader();

        try {
            // 获取包下所有资源
            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // 从文件系统加载
                    String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                    findClassesInDirectory(new File(filePath), packageName, classes);
                } else if ("jar".equals(protocol)) {
                    // 从 JAR 文件加载
                    findClassesInJar(resource, packageName, classes);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("扫描包失败: " + packageName, e);
        }

        return classes;
    }

    /**
     * 从目录扫描类
     */
    private static void findClassesInDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(loadClass(className));
                } catch (Exception e) {
                    // 忽略加载失败的类
                }
            }
        }
    }

    /**
     * 从 JAR 文件扫描类
     */
    private static void findClassesInJar(URL jarUrl, String packageName, Set<Class<?>> classes) throws IOException {
        JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
        JarFile jarFile = jarConnection.getJarFile();
        String packagePath = packageName.replace('.', '/');

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                try {
                    classes.add(loadClass(className));
                } catch (Exception e) {
                    // 忽略加载失败的类
                }
            }
        }
    }

    /**
     * 判断是否为数组类型
     *
     * @param clazz 类
     * @return true 如果是数组
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz != null && clazz.isArray();
    }

    /**
     * 判断是否为基本类型
     *
     * @param clazz 类
     * @return true 如果是基本类型
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz != null && clazz.isPrimitive();
    }

    /**
     * 判断是否为简单类型（基本类型、包装类型、String）
     *
     * @param clazz 类
     * @return true 如果是简单类型
     */
    public static boolean isSimpleType(Class<?> clazz) {
        return isPrimitive(clazz)
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || Character.class.isAssignableFrom(clazz);
    }
}
