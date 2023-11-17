package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.ServerServiceSemaphoreWrapper;
import com.poldroc.rpc.framework.core.common.annotations.SPI;
import com.poldroc.rpc.framework.core.common.exception.MaxServiceLimitRequestException;
import com.poldroc.rpc.framework.core.filter.ServerFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;
/**
 * 请求数据在执行实际业务函数之前需要会经过前置过滤器的逻辑，
 * 而限流组件则是在前置过滤器的最后一环，主要负责tryAcquire环节
 * @author Poldroc
 * @date 2023/10/7
 */

@SPI("before")
@Slf4j
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        // 从缓存中提取semaphore对象
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        // 尝试获取信号量
        boolean tryResult = semaphore.tryAcquire();
        // 如果获取失败，说明当前服务已经达到最大并发数，直接抛出异常
        if (!tryResult) {
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException rpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(rpcException);
            throw rpcException;
        }
    }
}
