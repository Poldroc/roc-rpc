package com.poldroc.rpc.framework.core.common.event;

import lombok.Data;

/**
 * 节点更新事件
 * @author Poldroc
 * @date 2023/9/16
 */
@Data
public class RpcUpdateEvent implements RpcEvent{

    private Object data;

    public RpcUpdateEvent(Object data) {
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
