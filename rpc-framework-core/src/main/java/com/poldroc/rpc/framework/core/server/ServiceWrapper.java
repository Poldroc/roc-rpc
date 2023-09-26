package com.poldroc.rpc.framework.core.server;

import lombok.Data;

/**
 * RPC服务包装类，用于存储服务对象和服务分组信息
 * @author Poldroc
 * @date 2023/9/23
 */
@Data
public class ServiceWrapper {

    /**
     * 对外暴露的具体服务对象
     */
    private Object serviceObj;

    /**
     * 具体暴露服务的分组
     */
    private String group = "default";

    /**
     * 整个应用的token校验
     */
    private String serviceToken = "";

    /**
     * 限流策略
     */
    private Integer limit = -1;


    public ServiceWrapper(Object serviceObj) {
        this.serviceObj = serviceObj;
    }

    public ServiceWrapper(Object serviceObj, String group) {
        this.serviceObj = serviceObj;
        this.group = group;
    }

}
