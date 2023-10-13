package com.poldroc.rpc.framework.core.registry;

import com.poldroc.rpc.framework.core.registry.zookeeper.ProviderNodeInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.*;

/**
 * 服务url
 * 将RPC的主要配置都封装在了里面
 * 所有重要的配置后期都是基于这个类去进行存储的
 * @author Poldroc
 * @date 2023/9/15
 */
@Data
@Slf4j
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
        String host = url.getParameters().get(HOST);
        String port = url.getParameters().get(PORT);
        String group = url.getParameters().get(GROUP);
        String weight = url.getParameters().get(WEIGHT);
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis() + ";" + weight + ";" + group).getBytes(), StandardCharsets.UTF_8);

    }

    /**
     * 将URL转换为写入zk的consumer节点下的一段字符串
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(ServiceUrl url) {
        String host = url.getParameters().get(HOST);
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     *
     * @param providerNodeStr
     * @return
     */
    public static ProviderNodeInfo buildURLFromUrlStr(String providerNodeStr) {
        String[] items = providerNodeStr.split(";");
        log.info("providerNodeStr : {}", providerNodeStr);
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setApplicationName(items[0]);
        providerNodeInfo.setServiceName(items[1]);
        providerNodeInfo.setAddress(items[2]);
        providerNodeInfo.setRegistryTime(items[3]);
        providerNodeInfo.setWeight(Integer.valueOf(items[4]));
        providerNodeInfo.setGroup(String.valueOf(items[5]));
        return providerNodeInfo;
    }
}
