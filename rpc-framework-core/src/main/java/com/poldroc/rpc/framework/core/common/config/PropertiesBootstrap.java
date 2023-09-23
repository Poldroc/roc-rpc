package com.poldroc.rpc.framework.core.common.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.JDK_SERIALIZE_TYPE;

/**
 * 将properties的配置转换成本地的一个Map结构
 * @author Poldroc
 * @date 2023/9/18
 */
@Slf4j
public class PropertiesBootstrap {
    private volatile boolean configIsReady;

    public static final String SERVER_PORT = "rpc.serverPort";
    public static final String REGISTER_ADDRESS = "rpc.registerAddr";
    public static final String APPLICATION_NAME = "rpc.applicationName";
    public static final String PROXY_TYPE = "rpc.proxyType";
    public static final String ROUTER_TYPE = "rpc.router";
    public static final String SERVER_SERIALIZE_TYPE = "rpc.serverSerialize";
    public static final String CLIENT_SERIALIZE_TYPE = "rpc.clientSerialize";


    public static ServerConfig loadServerConfigFromLocal() {
        log.info("======== loadServerConfigFromLocal ========");
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadServerConfigFromLocal fail,e is {}", e);
        }
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setServerPort(PropertiesLoader.getPropertiesInteger(SERVER_PORT));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        serverConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        serverConfig.setServerSerialize(PropertiesLoader.getPropertiesStrDefault(SERVER_SERIALIZE_TYPE,JDK_SERIALIZE_TYPE));
        return serverConfig;
    }


    public static ClientConfig loadClientConfigFromLocal(){
        log.info("======== loadClientConfigFromLocal ========");
        try {
            PropertiesLoader.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("loadClientConfigFromLocal fail,e is {}", e);
        }
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setApplicationName(PropertiesLoader.getPropertiesStr(APPLICATION_NAME));
        clientConfig.setRegisterAddr(PropertiesLoader.getPropertiesStr(REGISTER_ADDRESS));
        clientConfig.setProxyType(PropertiesLoader.getPropertiesStr(PROXY_TYPE));
        clientConfig.setRouterStrategy(PropertiesLoader.getPropertiesStr(ROUTER_TYPE));
        clientConfig.setClientSerialize(PropertiesLoader.getPropertiesStrDefault(CLIENT_SERIALIZE_TYPE,JDK_SERIALIZE_TYPE));
        return clientConfig;
    }
}
