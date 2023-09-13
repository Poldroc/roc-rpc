package com.poldroc.rpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * RPC请求的编码器
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
     * 协议的最大长度
     * 1024 为内容的最大长度
     */
    public static final int MAX_FRAME_LENGTH = 1024;

    /**
     * 解码器,考虑是否会有粘包拆包
     * @param ctx 上下文
     * @param byteBuf 用于读取数据
     * @param list 用于添加解码后的对象，传递给下一个handler处理
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 可读字节数小于基本长度，直接返回
        if (byteBuf.readableBytes() < BASE_LENGTH) {
            return;
        }else {
            // 防止接收到的数据过大，导致内存溢出
            if (byteBuf.readableBytes() > MAX_FRAME_LENGTH) {
                byteBuf.skipBytes(byteBuf.readableBytes());
            }
            // 标记当前读取位置
            int beginReader;
            while (true){
                // 获取当前读取位置
                beginReader = byteBuf.readerIndex();
                // 标记当前读取位置
                byteBuf.markReaderIndex();
                // 读取魔数
                if (byteBuf.readShort() == MAGIC_NUMBER) {
                    break;
                } else {
                    // 不是魔数开头，为非法客户端发来的数据，关闭连接
                    ctx.close();
                    return;
                }
            }
            // 读取内容长度，对应RpcProtocol中的contentLength
            int contentLength = byteBuf.readInt();
            // 判断可读字节数是否小于内容长度
            if (byteBuf.readableBytes() < contentLength) {
                // 说明数据还没到齐，重置读取位置
                byteBuf.readerIndex(beginReader);
                return;
            }
            // 读取内容，对应RpcProtocol中的content
            byte[] content = new byte[contentLength];
            byteBuf.readBytes(content);
            // 封装成RpcProtocol对象，添加到list中，传递给下一个handler处理
            RpcProtocol rpcProtocol = new RpcProtocol(content);
            list.add(rpcProtocol);
        }



    }
}
