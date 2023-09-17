package com.poldroc.rpc.framework.core.common.event;

import io.netty.channel.ChannelFuture;
import lombok.Data;

/**
 * 通道事件包装类
 * @author Poldroc
 * @date 2023/9/16
 */
@Data
public class ChannelFutureWrapper {

    /**
     *  通道
     */
    private ChannelFuture channelFuture;

    private String host;

    private Integer port;
}
