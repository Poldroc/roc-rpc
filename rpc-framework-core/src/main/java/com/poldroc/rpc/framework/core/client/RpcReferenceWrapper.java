package com.poldroc.rpc.framework.core.client;

/**
 * RPC远程调用包装类，用于存储服务的接口类和服务的分组信息
 * @author Poldroc
 * @date 2023/9/23
 */

public class RpcReferenceWrapper<T> {

    private Class<T> aimClass;

    private String group;

    public Class<T> getAimClass() {
        return aimClass;
    }

    public void setAimClass(Class<T> aimClass) {
        this.aimClass = aimClass;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
