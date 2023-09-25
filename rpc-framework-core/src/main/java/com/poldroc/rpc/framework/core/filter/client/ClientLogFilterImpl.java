package com.poldroc.rpc.framework.core.filter.client;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.filter.ClientFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;
/**
 * 客户端日志过滤器
 * @author Poldroc
 * @date 2023/9/25
 */

@Slf4j
public class ClientLogFilterImpl implements ClientFilter {

    /**
     * 执行过滤链
     * 记录当前客户端程序调用了哪个具体的service方法
     * @param src
     * @param rpcInvocation
     */
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        log.info("******** ClientLogFilterImpl doFilter ********");
        rpcInvocation.getAttachments().put("c_app_name", CLIENT_CONFIG.getApplicationName());
        log.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -----> " + rpcInvocation.getTargetServiceName());
    }
}
