package com.poldroc.rpc.framework.core.client;

import com.poldroc.rpc.framework.core.proxy.ProxyFactory;

/**
 * RPC的引用类，用于获取代理对象，发起调用
 * @author Poldroc
 * @date 2023/9/14
 */
public class RpcReference {

    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    /**
     * 根据接口类型获取代理对象
     * @param rpcReferenceWrapper
     * @param <T>
     * @return
     */
    public <T> T get(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }
}