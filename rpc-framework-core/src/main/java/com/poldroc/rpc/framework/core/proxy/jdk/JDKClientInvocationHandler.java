package com.poldroc.rpc.framework.core.proxy.jdk;

import com.poldroc.rpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * 各代理工厂都统一使用
 * 核心任务就是将需要调用的方法名称、服务名称，参数统统都封装好到RpcInvocation当中，然后塞入到一个队列里，并且等待服务端的数据返回
 * @author Poldroc
 * @date 2023/9/15
 */

public class JDKClientInvocationHandler implements InvocationHandler {

    /**
     * 用于锁定当前对象
     */
    private final static Object OBJECT = new Object();

    /**
     * 接口类型
     */
    private Class<?> clazz;

    public JDKClientInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetServiceName(clazz.getName());
        rpcInvocation.setTargetMethod(method.getName());
        // 注入uuid，用于标识请求
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        // 将请求信息放入发送队列
        SEND_QUEUE.add(rpcInvocation);
        // 客户端请求超时判断
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < 3 * 1000) {
            // 从响应结果集中获取响应结果
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                // 如果是RpcInvocation类型，说明是服务端返回的响应结果，直接返回
                return ((RpcInvocation)object).getResponse();
            }
        }
        // 如果超时，抛出异常
        throw new TimeoutException("client wait server's response timeout!");
    }
}
