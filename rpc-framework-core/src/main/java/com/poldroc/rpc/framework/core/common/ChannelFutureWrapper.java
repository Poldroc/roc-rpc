package com.poldroc.rpc.framework.core.common;

import io.netty.channel.ChannelFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通道事件包装类
 * @author Poldroc
 * @date 2023/9/16
 */
@Data
@AllArgsConstructor
public class ChannelFutureWrapper {

    /**
     *  通道
     */
    private ChannelFuture channelFuture;

    private String host;

    private Integer port;

    private Integer weight;

    private String group;


    public ChannelFutureWrapper(String host, Integer port,Integer weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
    }


    public ChannelFutureWrapper() {
    }

}
