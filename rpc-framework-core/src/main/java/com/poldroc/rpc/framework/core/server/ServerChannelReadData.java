package com.poldroc.rpc.framework.core.server;

import com.poldroc.rpc.framework.core.common.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * 处理服务器通道上的数据读取
 * @author Poldroc
 * @date 2023/10/4
 */
@Data
public class ServerChannelReadData {

    private RpcProtocol rpcProtocol;

    private ChannelHandlerContext channelHandlerContext;


}
