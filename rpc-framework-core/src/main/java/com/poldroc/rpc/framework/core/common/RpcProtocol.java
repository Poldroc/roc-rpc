package com.poldroc.rpc.framework.core.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

/**
 * RPC自定义协议
 * @author Poldroc
 * @date 2023/9/13
 */

@Data
public class RpcProtocol implements Serializable {
    private static final long serialVersionUID = 4359452337712339597L;

    /**
     * 用于标识协议，在做服务通讯的时候定义的一个安全检测，确认当前请求的协议是否合法。
     */
    private short magicNumber = MAGIC_NUMBER;

    /**
     * 协议传输核心数据的长度
     */
    private int contentLength;

    /**
     * 协议传输核心数据
     * 主要是请求的服务名称，请求服务的方法名称，请求参数内容
     * 统一封装成一个RpcInvocation对象
     */
    private byte[] content;

    public RpcProtocol(byte[] content) {
        this.contentLength = content.length;
        this.content = content;
    }

    @Override
    public String toString() {
        return "RpcProtocol{" +
                "contentLength=" + contentLength +
                ", content=" + Arrays.toString(content) +
                '}';
    }

}
