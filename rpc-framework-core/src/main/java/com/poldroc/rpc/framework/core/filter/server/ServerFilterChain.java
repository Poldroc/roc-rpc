package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.filter.ServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端过滤器链
 * 插入位置 com.poldroc.rpc.framework.core.server.ServerHandler#channelRead
 * @author Poldroc
 * @date 2023/9/25
 */

public class ServerFilterChain {

    private static List<ServerFilter> serverFilters = new ArrayList<>();

    public void addFilter(ServerFilter serverFilter) {
        serverFilters.add(serverFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation) {
        for (ServerFilter serverFilter : serverFilters) {
            serverFilter.doFilter(rpcInvocation);
        }
    }



}
