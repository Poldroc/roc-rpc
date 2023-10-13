package com.poldroc.rpc.framework.core.common.event.data;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 服务更新事件包装类
 * @author Poldroc
 * @date 2023/9/16
 */
@Data
@ToString
public class SUrlChangeWrapper {

    private String serviceName;

    private List<String> providerUrl;

    /**
     * 记录每个ip下边的url详细信息，包括权重，分组等
      */

    private Map<String,String> nodeDataUrl;

}
