package com.poldroc.rpc.framework.core.filter;

import com.poldroc.rpc.framework.core.common.RpcInvocation;

/**
 * 服务端过滤器
 * @author Poldroc
 * @date 2023/9/25
 */

public interface ServerFilter extends Filter{

    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);

}
