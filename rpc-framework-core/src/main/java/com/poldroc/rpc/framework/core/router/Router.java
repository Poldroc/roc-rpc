package com.poldroc.rpc.framework.core.router;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;

/**
 * 路由接口
 * @author Poldroc
 * @date 2023/9/20
 */

public interface Router {

    /**
     * 刷新路由表
     * 在客户端和服务提供者进行连接建立的环节触发
     * @param selector 选择器
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求到连接通道
     *
     * @param selector 选择器
     * @return ChannelFutureWrapper 连接通道
     */
    ChannelFutureWrapper select(Selector selector);


    /**
     * 更新权重信息
     *
     * @param sUrl 服务地址
     */
    void updateWeight(ServiceUrl sUrl);
}
