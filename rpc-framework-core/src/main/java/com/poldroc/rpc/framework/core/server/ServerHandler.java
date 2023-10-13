package com.poldroc.rpc.framework.core.server;

import com.alibaba.fastjson.JSON;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.*;

/**
 * RPC服务端处理器，服务端的接收数据之后的处理器
 * 非共享模式，不存在线程安全问题
 * 当数据抵达这个位置的时候，已经是以RpcProtocol的格式展现
 * @author Poldroc
 * @date 2023/9/13
 */
@ChannelHandler.Sharable // 标记该类的实例可以被多个Channel共享，这通常在不涉及状态共享的情况下使用，确保处理器的实例可以被多个通道安全地共享*/
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 处理客户端发送的数据
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ServerChannelReadData serverChannelReadData = new ServerChannelReadData();
        serverChannelReadData.setRpcProtocol((RpcProtocol) msg);
        serverChannelReadData.setChannelHandlerContext(ctx);
        SERVER_CHANNEL_DISPATCHER.add(serverChannelReadData);
    }


    /**
     * 处理异常情况
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
