package com.poldroc.rpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * RPC请求的编码器
 *
 * @author Poldroc
 * @date 2023/9/12
 */

public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议的开头部分的标准长度
     * 2 为2个字节的魔数；4 为4个字节的内容长度
     */
    public final int BASE_LENGTH = 2 + 4;


    /**
     * 解码器,考虑是否会有粘包拆包
     *
     * @param ctx     上下文
     * @param byteBuf 用于读取数据
     * @param out     用于添加解码后的对象，传递给下一个handler处理
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        // 可读字节数小于基本长度，直接返回
        if (byteBuf.readableBytes() < BASE_LENGTH) {
            if (!(byteBuf.readShort() == MAGIC_NUMBER)) {
                ctx.close();
                return;
            }
            int length = byteBuf.readInt();
            if (byteBuf.readableBytes() < length) {
                //数据包有异常
                ctx.close();
                return;
            }
            byte[] body = new byte[length];
            byteBuf.readBytes(body);
            RpcProtocol rpcProtocol = new RpcProtocol(body);
            out.add(rpcProtocol);
        }
    }
}
