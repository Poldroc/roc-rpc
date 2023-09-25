package com.poldroc.rpc.framework.core.filter.client;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.filter.ClientFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.SERVICE_URL;

/**
 * ip直连过滤器
 * @author Poldroc
 * @date 2023/9/26
 */
@Slf4j
public class DirectInvokeFilterImpl implements ClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        log.info("******** DirectInvokeFilterImpl doFilter ********");
        String url = (String) rpcInvocation.getAttachments().get(SERVICE_URL);
        if (CommonUtils.isEmpty(url)) {
            log.info("url is empty in {}", rpcInvocation.toString());
            return;
        }
        // 过滤掉不属于该ip的服务提供者
        src.removeIf(channelFutureWrapper -> !(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()).equals(url));
        if(CommonUtils.isEmptyList(src)){
            throw new RuntimeException("no match provider url for "+ url);
        }

    }
}
