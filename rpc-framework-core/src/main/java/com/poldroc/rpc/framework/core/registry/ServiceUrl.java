package com.poldroc.rpc.framework.core.registry;

import lombok.Data;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务url
 * 将RPC的主要配置都封装在了里面
 * 所有重要的配置后期都是基于这个类去进行存储的
 * @author Poldroc
 * @date 2023/9/15
 */
@Data
public class ServiceUrl {

    /**
     * 服务应用名称
     */
    private String applicationName;

    /**
     * 注册到节点到服务名称，例如：com.test.UserService
     */
    private String serviceName;

    /**
     * 这里面可以自定义不限进行扩展
     * 分组
     * 权重
     * 服务提供者的地址
     * 服务提供者的端口
     */
    private Map<String, String> parameters = new HashMap<>();

    public void addParameter(String key, String value) {
        this.parameters.putIfAbsent(key, value);
    }

    /**
     * 将URL转换为写入zk的provider节点下的一段字符串
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(ServiceUrl url) {
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(ServiceUrl url) {
        String host = url.getParameters().get("host");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     *
     * @param providerNodeStr
     * @return
     */
//    public static ProviderNodeInfo buildURLFromUrlStr(String providerNodeStr) {
//        String[] items = providerNodeStr.split("/");
//        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
//        providerNodeInfo.setServiceName(items[2]);
//        providerNodeInfo.setAddress(items[4]);
//        return providerNodeInfo;
//    }
//





}
