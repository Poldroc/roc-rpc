package com.poldroc.rpc.framework.core.registry.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 服务提供者节点信息
 * @author Poldroc
 * @date 2023/9/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNodeInfo {

    private String applicationName;

    private String serviceName;

    private String address;

    private Integer weight;

    private String registryTime;

    private String group;

    @Override
    public String toString() {
        return "ProviderNodeInfo{" +
                "applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                ", weight=" + weight +
                ", registryTime='" + registryTime + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
