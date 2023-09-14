package com.poldroc.rpc.framework.core.client;

import com.poldroc.rpc.framework.core.common.config.ClientConfig;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC客户端类
 * @author Poldroc
 * @date 2023/9/14
 */

@Slf4j
public class Client {

    /**
     * 客户端线程组
     */
    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    /**
     * 客户端配置对象
     */
    private ClientConfig clientConfig;

    /**
     *  获取客户端配置对象
     */
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    /**
     * 设置客户端配置对象
     */
    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    /**
     * 启动客户端
     * @throws InterruptedException
     */


}
