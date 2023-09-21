package com.poldroc.rpc.framework.core.common.event;

public class RpcDestroyEvent implements RpcEvent{

    private Object data;

    public RpcDestroyEvent(Object data) {
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
