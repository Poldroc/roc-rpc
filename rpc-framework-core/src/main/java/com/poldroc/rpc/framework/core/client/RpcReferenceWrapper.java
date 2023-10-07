package com.poldroc.rpc.framework.core.client;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.*;

/**
 * RPC远程调用包装类，用于存储服务的接口类和服务的分组信息
 *
 * @author Poldroc
 * @date 2023/9/23
 */
public class RpcReferenceWrapper<T> {

    private Class<T> aimClass;

    private Map<String, Object> attatchments = new ConcurrentHashMap<>();

    public Class<T> getAimClass() {
        return aimClass;
    }

    public void setAimClass(Class<T> aimClass) {
        this.aimClass = aimClass;
    }

    public boolean isAsync() {
        return Boolean.valueOf(String.valueOf(attatchments.get(ASYNC)));
    }

    public void setAsync(boolean async) {
        this.attatchments.put(ASYNC, async);
    }

    public String getServiceUrl() {
        return String.valueOf(attatchments.get(SERVICE_URL));
    }

    public void setServiceUrl(String Serviceurl) {
        attatchments.put(SERVICE_URL, Serviceurl);
    }

    public void setTimeOut(int timeOut) {
        attatchments.put(TIME_OUT, timeOut);
    }

    public String getTimeOUt() {
        return String.valueOf(attatchments.get(TIME_OUT));
    }

    public String getServiceToken() {
        return String.valueOf(attatchments.get(SERVICE_TOKEN));
    }

    public void setServiceToken(String serviceToken) {
        attatchments.put(SERVICE_TOKEN, serviceToken);
    }

    public String getGroup() {
        return String.valueOf(attatchments.get(GROUP));
    }

    public void setGroup(String group) {
        attatchments.put(GROUP, group);
    }

    public Map<String, Object> getAttatchments() {
        return attatchments;
    }

    public void setAttatchments(Map<String, Object> attatchments) {
        this.attatchments = attatchments;
    }
}
