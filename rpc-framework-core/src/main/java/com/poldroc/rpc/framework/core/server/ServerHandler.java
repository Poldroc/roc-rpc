package com.poldroc.rpc.framework.core.server;

import com.alibaba.fastjson.JSON;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;

/**
 * RPC服务端处理器，服务端的接收数据之后的处理器
 * @author Poldroc
 * @date 2023/9/13
 */

public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 处理客户端发送的数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 服务端接收数据的时候统一以 RpcProtocol 协议的格式接收
        // 当数据抵达这个位置的时候，已经是以RpcProtocol的格式展现
        RpcProtocol rpcProtocol = (RpcProtocol) msg;

        String contentJson = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
        RpcInvocation rpcInvocation = JSON.parseObject(contentJson, RpcInvocation.class);

        // 从缓存中获取目标对象
        Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
        // 获取目标对象的所有方法
        Method[] methods = aimObject.getClass().getDeclaredMethods();
        Object result = null;

        // 遍历所有方法，找到目标方法，找到与客户端请求的目标方法名匹配的方法
        for(Method method : methods) {
            if(method.getName().equals(rpcInvocation.getTargetMethod())){
                // 如果目标方法的返回值为void，则直接调用目标方法
                if(method.getReturnType().equals(Void.TYPE)){
                    // 动态调用方法
                    method.invoke(aimObject, rpcInvocation.getArgs());
                }else{
                    // 如果目标方法的返回值不为void，则调用目标方法，并将返回值赋值给result
                    result = method.invoke(aimObject, rpcInvocation.getArgs());
                }
                break;
            }
        }

        // 将方法执行返回的结果set到rpcInvocation中
        rpcInvocation.setResponse(result);
        // 将响应数据转化为 JSON 字符串，并将其转换为字节数组
        RpcProtocol respRpcProtocol = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes());

        // 将响应数据以RpcProtocol协议的格式发送给客户端
        ctx.writeAndFlush(respRpcProtocol);
    }


    /**
     * 处理异常情况
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()){
            ctx.close();
        }
    }
}
