package com.poldroc.rpc.framework.core.common.exceptiom;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * rpc异常类
 * @author Poldroc
 * @date 2023/10/5
 */

@Data
@AllArgsConstructor
public class RpcException extends RuntimeException {
    private RpcInvocation rpcInvocation;
}
