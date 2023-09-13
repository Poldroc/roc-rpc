package com.poldroc.rpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC请求的编码器
 * 继承自MessageToByteEncoder，将RpcProtocol编码为ByteBuf
 * @author Poldroc
 * @date 2023/9/12
 */

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    /**
     * 编码器
     * @param ctx 上下文
     * @param msg 要编码的对象
     * @param out 用于写入编码后的数据
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getMagicNumber());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
    }
}
