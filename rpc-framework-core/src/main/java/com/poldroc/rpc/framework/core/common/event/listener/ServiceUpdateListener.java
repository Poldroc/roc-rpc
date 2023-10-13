package com.poldroc.rpc.framework.core.common.event.listener;

import com.poldroc.rpc.framework.core.client.ConnectionHandler;
import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.event.RpcUpdateEvent;
import com.poldroc.rpc.framework.core.common.event.data.SUrlChangeWrapper;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import com.poldroc.rpc.framework.core.registry.zookeeper.ProviderNodeInfo;
import com.poldroc.rpc.framework.core.router.Selector;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.ROUTER;

/**
 * 服务更新监听器
 * zk的服务提供者节点发生了变更 客户端需要更新本地的一个目标服务列表，避免向无用的服务发送请求
 *
 * @author Poldroc
 * @date 2023/9/16
 */
@Slf4j
public class ServiceUpdateListener implements RpcListener<RpcUpdateEvent> {

    @Override
    public void callBack(Object t) {
        // 获取到子节点的数据信息
        SUrlChangeWrapper sUrlChangeWrapper = (SUrlChangeWrapper) t;
        // 获取到连接信息
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(sUrlChangeWrapper.getServiceName());
        List<String> matchProviderUrl = sUrlChangeWrapper.getProviderUrl();
        Set<String> finalUrl = new HashSet<>();
        List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
        // 遍历连接信息
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            // 获取到老的服务提供者 URL
            String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            // 如果老的 URL 没有，说明已经被移除了
            if (!matchProviderUrl.contains(oldServerAddress)) {
                continue;
            } else {
                // 将仍然存在的连接信息添加到 finalChannelFutureWrappers 列表中，并将对应的 URL 添加到 finalUrl 集合中
                finalChannelFutureWrappers.add(channelFutureWrapper);
                finalUrl.add(oldServerAddress);
            }
        }
        // 检查是否有新的服务提供者 URL，如果有，创建新的连接信息
        List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
        for (String newProviderUrl : matchProviderUrl) {
            if (!finalUrl.contains(newProviderUrl)) {
                ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                String host = newProviderUrl.split(":")[0];
                Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                channelFutureWrapper.setPort(port);
                channelFutureWrapper.setHost(host);
                String urlStr = sUrlChangeWrapper.getNodeDataUrl().get(newProviderUrl);
                ProviderNodeInfo providerNodeInfo = ServiceUrl.buildURLFromUrlStr(urlStr);
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
                ChannelFuture channelFuture = null;
                try {
                    channelFuture = ConnectionHandler.createChannelFuture(host, port);
                    channelFutureWrapper.setChannelFuture(channelFuture);
                    newChannelFutureWrapper.add(channelFutureWrapper);
                    finalUrl.add(newProviderUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 将新的连接信息添加到 finalChannelFutureWrappers 列表中
        finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
        // 最终更新服务在这里
        CONNECT_MAP.put(sUrlChangeWrapper.getServiceName(), finalChannelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(sUrlChangeWrapper.getServiceName());
        ROUTER.refreshRouterArr(selector);
    }
}

