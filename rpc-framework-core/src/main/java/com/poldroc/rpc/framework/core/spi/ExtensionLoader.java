package com.poldroc.rpc.framework.core.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义SPI扩展加载器
 *
 * @author Poldroc
 * @date 2023/9/28
 */
public class ExtensionLoader {
    /**
     * 扩展加载器目录前缀
     */
    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    /**
     * 扩展加载器缓存
     * key: 扩展加载器接口
     * value: 扩展加载器实例
     */
    public static Map<String, LinkedHashMap<String, Class>> EXTENSION_LOADER_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 加载指定接口的扩展加载器
     *
     * @param clazz 扩展加载器接口
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        // 参数校验
        if (clazz == null) {
            throw new IllegalArgumentException("class is null!");
        }

        // 获取扩展加载器配置文件路径
        String spiFilePath = EXTENSION_LOADER_DIR_PREFIX + clazz.getName();

        // 获取类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();

        // 获取所有匹配的扩展文件的URL枚举
        Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);

        // 遍历所有匹配的扩展文件
        while (enumeration.hasMoreElements()) {
            // 获取下一个扩展文件的URL
            URL url = enumeration.nextElement();

            // 获取扩展文件的输入流
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());

            // 获取扩展文件的缓冲读取器
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // 读取扩展文件的每一行
            String line;
            // 存储扩展类信息
            LinkedHashMap<String, Class> classMap = new LinkedHashMap<>();

            // 遍历扩展文件的每一行
            while ((line = bufferedReader.readLine()) != null) {
                // 如果配置中加入了#开头则表示忽略该类无需进行加载
                if (line.startsWith("#")) {
                    continue;
                }
                // 按照=分割扩展类信息
                String[] lineArr = line.split("=");
                // 获取扩展类名
                String implClassName = lineArr[0];
                // 获取扩展接口名
                String interfaceName = lineArr[1];
                // 将扩展类信息存入classMap
                classMap.put(implClassName, Class.forName(interfaceName));
            }

            // 只会触发class文件的加载，而不会触发对象的实例化
            if (EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())) {
                // 支持开发者自定义配置，将新加载的信息合并到已有缓存中
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                // 将新加载的信息存入缓存
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }

}
