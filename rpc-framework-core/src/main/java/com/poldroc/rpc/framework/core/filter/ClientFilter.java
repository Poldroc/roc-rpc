package com.poldroc.rpc.framework.core.filter;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.RpcInvocation;

import java.util.List;

/**
 * 客户端过滤器
 * @author Poldroc
 * @date 2023/9/25
 */

public interface ClientFilter extends Filter{
    /**
     * 执行过滤链
     *
     * @param src
     * @param rpcInvocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
