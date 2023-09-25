package com.poldroc.rpc.framework.core.proxy.jdk;

import com.poldroc.rpc.framework.core.client.RpcReferenceWrapper;
import com.poldroc.rpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * jdk代理工厂，辅助客户端发起调用的代理对象
 * @author Poldroc
 * @date 2023/9/15
 */

public class JDKProxyFactory implements ProxyFactory {

    /**
     * 根据传入的RpcReferenceWrapper对象获取代理对象
     * @param rpcReferenceWrapper
     * @return
     * @param <T>
     * @throws Throwable
     */
    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(), new Class[]{rpcReferenceWrapper.getAimClass()},
                new JDKClientInvocationHandler(rpcReferenceWrapper));
    }
}
