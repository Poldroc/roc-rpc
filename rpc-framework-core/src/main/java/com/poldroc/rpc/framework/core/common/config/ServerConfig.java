package com.poldroc.rpc.framework.core.common.config;

import lombok.Data;
/**
 * 服务端配置
 * @author Poldroc
 * @date 2023/9/13
 */

@Data
public class ServerConfig {

    /**
     * 服务端口
     */
    private Integer serverPort;


    private String registerAddr;

    private String registerType;
    /**
     * 服务名称
     */
    private String applicationName;

    /**
     * 服务端序列化方式 hession2,kryo,jdk,fastjson
     */
    private String serverSerialize;


    /**
     * 服务端业务线程数目
     */
    private Integer serverBizThreadNums;

    /**
     * 服务端接收队列的大小
     */
    private Integer serverQueueSize;

    /**
     * 限制服务端最大所能接受的数据包体积
     */
    private Integer maxServerRequestData;

    /**
     * 服务端最大连接数
     */
    private Integer maxConnections;

}
