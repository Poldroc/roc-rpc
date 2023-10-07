package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.ServerServiceSemaphoreWrapper;
import com.poldroc.rpc.framework.core.common.annotations.SPI;
import com.poldroc.rpc.framework.core.filter.ServerFilter;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;
/**
 * 当业务核心逻辑执行完毕之后，会进入到后置过滤器中，这里面可以执行relase操作
 * @author Poldroc
 * @date 2023/10/7
 */

@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }
}
