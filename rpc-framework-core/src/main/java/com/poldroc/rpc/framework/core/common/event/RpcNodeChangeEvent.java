package com.poldroc.rpc.framework.core.common.event;

/**
 * RPC节点变化事件
 * @author Poldroc
 * @date 2023/9/21
 */

public class RpcNodeChangeEvent implements RpcEvent{

    private Object data;

    public RpcNodeChangeEvent(Object data) {
        this.data = data;
    }
    @Override
    public Object getData() {
        return data;
    }

    @Override
    public RpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
