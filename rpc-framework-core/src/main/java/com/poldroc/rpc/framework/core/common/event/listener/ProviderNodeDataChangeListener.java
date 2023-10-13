package com.poldroc.rpc.framework.core.common.event.listener;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.event.RpcNodeChangeEvent;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import com.poldroc.rpc.framework.core.registry.zookeeper.ProviderNodeInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.ROUTER;

/**
 * 服务节点数据变化监听器
 * @author Poldroc
 * @date 2023/9/21
 */
@Slf4j
public class ProviderNodeDataChangeListener implements RpcListener<RpcNodeChangeEvent> {
    @Override
    public void callBack(Object t) {
        log.info("[callBack回调] 服务节点数据发生变化，变化的数据为：{}", t);
        ProviderNodeInfo providerNodeInfo = ((ProviderNodeInfo) t);
        List<ChannelFutureWrapper> channelFutureWrappers =  CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost()+":"+channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())){
                channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
                // 修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                ServiceUrl url = new ServiceUrl();
                url.setServiceName(providerNodeInfo.getServiceName());
                // 更新权重
                ROUTER.updateWeight(url);
                break;
            }
        }
    }
}
