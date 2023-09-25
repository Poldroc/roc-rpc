package com.poldroc.rpc.framework.core.common;

import java.util.concurrent.atomic.AtomicLong;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.SERVICE_ROUTER_MAP;

/**
 * 实现轮训效果
 * @author Poldroc
 * @date 2023/9/20
 */

public class ChannelFuturePollingRef {

    private AtomicLong referenceTimes = new AtomicLong(0);


    public ChannelFutureWrapper getChannelFutureWrapper(ChannelFutureWrapper[] arr){
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }
}