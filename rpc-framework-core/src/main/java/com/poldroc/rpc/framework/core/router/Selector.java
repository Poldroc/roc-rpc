package com.poldroc.rpc.framework.core.router;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import lombok.Data;

@Data
public class Selector {
    /**
     * 服务命名
     * eg: com.sise.test.DataService
     */
    private String providerServiceName;

    /**
     * 经过二次筛选之后的future集合
     */
    private ChannelFutureWrapper[] channelFutureWrappers;

}
