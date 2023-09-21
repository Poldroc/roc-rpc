package com.poldroc.rpc.framework.core.common.event;

import com.poldroc.rpc.framework.core.common.event.listener.ProviderNodeDataChangeListener;
import com.poldroc.rpc.framework.core.common.event.listener.RpcListener;
import com.poldroc.rpc.framework.core.common.event.listener.ServiceDestroyListener;
import com.poldroc.rpc.framework.core.common.event.listener.ServiceUpdateListener;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发送事件的监听器加载器
 *
 * @author Poldroc
 * @date 2023/9/16
 */
@Slf4j
public class RpcListenerLoader {
    private static List<RpcListener> rpcListenerList = new ArrayList<>();

    /**
     * 创建了一个线程池（ExecutorService）用于处理事件。这里创建了一个固定大小为2的线程池
     */
    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    /**
     * 监听器注册
     *
     * @param rpcListener
     */
    public static void registerListener(RpcListener rpcListener) {
        rpcListenerList.add(rpcListener);
    }


    public void init() {
        registerListener(new ServiceUpdateListener());
        registerListener(new ServiceDestroyListener());
        registerListener(new ProviderNodeDataChangeListener());
    }


    /**
     * 获取给定对象实现的接口的泛型类型参数 T
     *
     * @param o 给定对象
     * @return
     */
    public static Class<?> getInterfaceT(Object o) {
        // 获取对象实现的接口
        Type[] types = o.getClass().getGenericInterfaces();
        // 获取接口的泛型参数
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        // 获取泛型参数的类型
        Type type = parameterizedType.getActualTypeArguments()[0];
        // 如果泛型参数的实际类型是一个类，返回这个类
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    /**
     * 同步事件处理，可能会堵塞
     * 查找匹配事件类型的监听器，并调用它们的回调方法处理事件数据
     *
     * @param rpcEvent
     */
    public static void sendSyncEvent(RpcEvent rpcEvent) {
        log.info("======== sendSyncEvent ========");
        if (CommonUtils.isEmptyList(rpcListenerList)) {
            return;
        }
        for (RpcListener<?> rpcListener : rpcListenerList) {
            Class<?> type = getInterfaceT(rpcListener);
            if (type.equals(rpcEvent.getClass())) {
                try {
                    log.info("sendSyncEvent callBack: {} ", rpcEvent.getData());
                    rpcListener.callBack(rpcEvent.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 将RPC事件发送给注册的RPC监听器
     *
     * @param rpcEvent RPC事件
     */
    public static void sendEvent(RpcEvent rpcEvent) {
        log.info("======== sendEvent ========");
        // 检查rpcListenerList是否为空或为空列表
        if (CommonUtils.isEmptyList(rpcListenerList)) {
            return;
        }
        // 遍历注册的监听器列表
        for (RpcListener<?> rpcListener : rpcListenerList) {
            // 获取监听器的泛型类型参数
            Class<?> type = getInterfaceT(rpcListener);
            // 如果监听器的泛型类型参数与RPC事件的类型相同
            if (type != null && type.equals(rpcEvent.getClass())) {
                // 将事件放入线程池中执行
                eventThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.info("sendEvent callBack: {} ", rpcEvent.getData());
                            // 调用监听器的回调方法处理事件数据
                            rpcListener.callBack(rpcEvent.getData());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
