package com.poldroc.rpc.framework.core.filter.client;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.filter.ClientFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.GROUP;

/**
 * 服务分组过滤器
 * @author Poldroc
 * @date 2023/9/26
 */
@Slf4j
public class GroupFilterImpl implements ClientFilter {


    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        log.info("******** GroupFilterImpl doFilter ********");
        String group = String.valueOf(rpcInvocation.getAttachments().get(GROUP));
        // 过滤掉不属于该分组的服务提供者
        src.removeIf(channelFutureWrapper -> !channelFutureWrapper.getGroup().equals(group));
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}
