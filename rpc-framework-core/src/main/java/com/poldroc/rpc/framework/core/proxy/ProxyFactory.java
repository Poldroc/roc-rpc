package com.poldroc.rpc.framework.core.proxy;


/**
 * 代理工厂
 * @author Poldroc
 * @date 2023/9/15
 */

public interface ProxyFactory {

    <T> T getProxy(final Class clazz) throws Throwable;
}