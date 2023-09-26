package com.poldroc.rpc.framework.core.filter.client;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.filter.ClientFilter;

import java.util.ArrayList;
import java.util.List;
/**
 * 客户端过滤器链
 * 插入位置 com.poldroc.rpc.framework.core.client.ConnectionHandler#getChannelFuture
 * @author Poldroc
 * @date 2023/9/26
 */

public class ClientFilterChain {

    private static List<ClientFilter> clientFilterList = new ArrayList<>();

    public void addClientFilter(ClientFilter iClientFilter) {
        clientFilterList.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (ClientFilter clientFilter : clientFilterList) {
            clientFilter.doFilter(src, rpcInvocation);
        }
    }
}
