package com.poldroc.rpc.framework.core.common.config;

import com.poldroc.rpc.framework.core.common.utils.CommonUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件加载类
 * @author Poldroc
 * @date 2023/9/18
 */

public class PropertiesLoader {
    private static Properties properties;

    private static Map<String, String> propertiesMap = new HashMap<>();

    private static String DEFAULT_PROPERTIES_FILE = "F:/JavaTotal/rpc-framework/rpc-framework-core/src/main/resources/rpc.properties";

    //todo 如果这里直接使用static修饰是否可以？
    public static void loadConfiguration() throws IOException {
        if(properties!=null){
            return;
        }
        properties = new Properties();
        FileInputStream in = null;
        in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
        properties.load(in);
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static String getPropertiesStr(String key) {
        if (putKeyToMap(key)) {
            return null;
        }
        return String.valueOf(propertiesMap.get(key));
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesInteger(String key) {
        if (putKeyToMap(key)) {
            return null;
        }
        return Integer.valueOf(propertiesMap.get(key));
    }

    private static boolean putKeyToMap(String key) {
        if (properties == null) {
            return true;
        }
        if (CommonUtils.isEmpty(key)) {
            return true;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return false;
    }
}
