package com.poldroc.rpc.framework.core.common.event.data;

import lombok.Data;

import java.util.List;

/**
 * 服务更新事件包装类
 * @author Poldroc
 * @date 2023/9/16
 */
@Data
public class SUrlChangeWrapper {

    private String serviceName;

    private List<String> providerUrl;

    public String toString() {
        return "URLChangeWrapper{" +
                "serviceName='" + serviceName + '\'' +
                ", providerUrl=" + providerUrl +
                '}';
    }
}
