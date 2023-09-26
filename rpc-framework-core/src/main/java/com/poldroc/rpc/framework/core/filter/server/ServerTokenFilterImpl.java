package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.filter.ServerFilter;
import com.poldroc.rpc.framework.core.server.ServiceWrapper;
import lombok.extern.slf4j.Slf4j;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;
import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.SERVICE_TOKEN;

/**
 * 简单版本的token校验
 * @author Poldroc
 * @date 2023/9/26
 */

@Slf4j
public class ServerTokenFilterImpl implements ServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        log.info("******** ServerTokenFilterImpl doFilter ********");
        String token = String.valueOf(rpcInvocation.getAttachments().get(SERVICE_TOKEN));
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = String.valueOf(serviceWrapper.getServiceToken());
        if (CommonUtils.isEmpty(matchToken)) {
            log.info("token is empty in {}", rpcInvocation.toString());
            return;
        }
        if (!CommonUtils.isEmpty(token) && token.equals(matchToken)) {
            log.info("token: {} is match in {}",token ,rpcInvocation.toString());
            return;
        }
        throw new RuntimeException("token is " + token + " , verify result is false!");
    }
}
