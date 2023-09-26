package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.filter.ServerFilter;
import lombok.extern.slf4j.Slf4j;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * 服务端日志过滤器实现类
 *
 * @author Poldroc
 * @date 2023/9/26
 */
@Slf4j
public class ServerLogFilterImpl implements ServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        log.info("******** ServerLogFilterImpl doFilter ********");
        log.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -----> " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
