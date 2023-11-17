package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.exception.RpcException;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.filter.ServerFilter;
import com.poldroc.rpc.framework.core.server.ServiceWrapper;
import lombok.extern.slf4j.Slf4j;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

/**
 * 简单版本的token校验
 *
 * @author Poldroc
 * @date 2023/9/26
 */

@Slf4j
public class ServerTokenFilterImpl implements ServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        log.info("******** ServerTokenFilterImpl doFilter ********");
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if (CommonUtils.isEmpty(matchToken)) {
            return;
        }
        if (!CommonUtils.isEmpty(token) && token.equals(matchToken)) {
            return;
        } else {
            rpcInvocation.setRetry(0);
            rpcInvocation.setE(new RuntimeException("service token is illegal for service " + rpcInvocation.getTargetServiceName()));
            rpcInvocation.setResponse(null);
            //直接交给响应线程那边处理（响应线程在代理类内部的invoke函数中，那边会取出对应的uuid的值，然后判断）
            RESP_MAP.put(rpcInvocation.getUuid(), rpcInvocation);
            throw new RpcException(rpcInvocation);
        }
    }
}
