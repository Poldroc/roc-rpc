package com.poldroc.rpc.framework.core.proxy;


import com.poldroc.rpc.framework.core.client.RpcReferenceWrapper;

/**
 * 代理工厂
 * @author Poldroc
 * @date 2023/9/15
 */

public interface ProxyFactory {

    <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;
}