package com.poldroc.rpc.framework.core.common.event;
/**
 * rpc事件接口
 * @author Poldroc
 * @date 2023/9/16
 */

public interface RpcEvent {

    Object getData();

    RpcEvent setData(Object data);
}
