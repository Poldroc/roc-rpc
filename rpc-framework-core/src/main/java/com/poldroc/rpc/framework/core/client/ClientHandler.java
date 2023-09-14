package com.poldroc.rpc.framework.core.client;


import com.alibaba.fastjson.JSON;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.RESP_MAP;

/**
 * 客户端处理器，用于处理客户端的请求
 * @author Poldroc
 * @date 2023/9/14
 */
@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {


    /**
     * 处理客户端发送的数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 客户端和服务端之间的数据都是以RpcProtocol对象作为基本协议进行的交互
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        // 得到传输参数的字节数组
        byte[] content = rpcProtocol.getContent();
        // 将字节数组转化为字符串
        String result = new String(content, 0, content.length);
        log.info("客户端接收到的数据为：{}", result);
        // RpcInvocation 通常包含了远程调用的参数和方法等信息
        RpcInvocation rpcInvocation = JSON.parseObject(result, RpcInvocation.class);
        // 通过之前发送的uuid，来注入对应的响应数据
        if(!RESP_MAP.containsKey(rpcInvocation.getUuid())){
            // 如果没有对应的响应数据
            throw new IllegalArgumentException("没有对应的响应数据");
        }

        // 将请求的响应结构放入一个Map集合中，key为uuid，value为响应数据
        // uuid在发送请求之前就已经初始化，所以只需要起一个线程在后台遍历这个map，查看对应的key是否有相应即可
        // uuid的放入在代理类中实现
        RESP_MAP.put(rpcInvocation.getUuid(),rpcInvocation);
        // 释放资源
        ReferenceCountUtil.release(msg);
    }

    /**
     * 异常处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}
