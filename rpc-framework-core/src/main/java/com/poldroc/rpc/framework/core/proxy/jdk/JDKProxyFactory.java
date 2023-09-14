package com.poldroc.rpc.framework.core.proxy.jdk;

import com.poldroc.rpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * jdk代理工厂，辅助客户端发起调用的代理对象
 * @author Poldroc
 * @date 2023/9/15
 */

public class JDKProxyFactory implements ProxyFactory {

    /**
     * 根据接口类型获取代理对象
     * @param clazz
     * @return
     * @param <T>
     * @throws Throwable
     */
    @Override
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new JDKClientInvocationHandler(clazz));
    }
}
