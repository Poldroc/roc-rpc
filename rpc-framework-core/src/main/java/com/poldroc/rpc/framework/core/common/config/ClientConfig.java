package com.poldroc.rpc.framework.core.common.config;

/**
 *
 * @author Poldroc
 * @date 2023/9/14
 */

public class ClientConfig {

    private Integer port;

    /**
     *
     */
    private String serverAddr;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
