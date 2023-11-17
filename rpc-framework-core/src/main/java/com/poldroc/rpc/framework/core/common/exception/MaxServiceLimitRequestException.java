package com.poldroc.rpc.framework.core.common.exception;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
/**
 * 服务限流异常类
 * @author Poldroc
 * @date 2023/10/5
 */

public class MaxServiceLimitRequestException extends RpcException{
    public MaxServiceLimitRequestException(RpcInvocation rpcInvocation) {
        super(rpcInvocation);
    }
}
