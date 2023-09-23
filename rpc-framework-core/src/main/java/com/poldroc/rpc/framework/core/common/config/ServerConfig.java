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

    /**
     * 服务名称
     */
    private String applicationName;

    /**
     * 服务端序列化方式 hession2,kryo,jdk,fastjson
     */
    private String serverSerialize;

}
