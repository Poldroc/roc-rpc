package com.poldroc.rpc.framework.core.common.event.listener;
/**
 * 监听器接口
 * @author Poldroc
 * @date 2023/9/16
 */
public interface RpcListener<T> {

    /**
     * 回调函数
     * @param t
     */
    void callBack(Object t);
}
