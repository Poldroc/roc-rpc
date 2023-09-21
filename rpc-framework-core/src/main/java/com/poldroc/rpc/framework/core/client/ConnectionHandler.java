package com.poldroc.rpc.framework.core.client;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.router.Selector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.*;

/**
 * 连接处理器
 * 当注册中心的节点新增或者移除或者权重变化的时候，这个类主要负责对内存中的url做变更
 * @author Poldroc
 * @date 2023/9/16
 */
@Slf4j
public class ConnectionHandler {
    /**
     * 核心的连接处理器
     * 专门用于负责和服务端构建连接通信
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道 元操作，既要处理连接，还要统一将连接进行内存存储管理
     *
     * @param providerIp
     * @return
     * @throws InterruptedException
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if (bootstrap == null) {
            throw new RuntimeException("bootstrap can not be null");
        }
        //格式错误类型的信息
        if(!providerIp.contains(":")){
            log.error("providerIp format error");
            return;
        }
        // 将服务提供者的 IP 地址拆分成 IP 和端口号
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        Integer port = Integer.parseInt(providerAddress[1]);
        // 到底这个channelFuture里面是什么
        // 使用 bootstrap 对象建立与服务提供者的连接，这是一个同步操作，会等待连接建立完成
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerURLInfo = URL_MAP.get(providerServiceName).get(providerIp);
        // 创建 ChannelFutureWrapper 对象，将来可以从这个对象中获取与服务端的连接
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setWeight(Integer.valueOf(providerURLInfo.substring(providerURLInfo.lastIndexOf(";")+1)));
        // 将服务提供者的 IP 地址添加到 SERVER_ADDRESS 集合中，用于跟踪已连接的服务提供者
        SERVER_ADDRESS.add(providerIp);
        // 获取与特定服务名称关联的连接信息列表
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        // 如果列表为空，则创建一个新的空列表
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            channelFutureWrappers = new ArrayList<>();
        }
        // 将新建立的连接信息添加到与服务名称关联的连接信息列表中，并将更新后的列表存储回 CONNECT_MAP 中
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
        // 为服务提供者构建一个 Selector 对象，Selector 对象中存储了该服务提供者对应的服务名称
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        // 刷新路由信息
        ROUTER.refreshRouterArr(selector);
    }

    /**
     * 构建ChannelFuture
     * 对象表示与指定 IP 和端口的服务提供者的连接
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture createChannelFuture(String ip,Integer port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        return channelFuture;
    }

    /**
     * 断开连接
     *
     * @param providerServiceName
     * @param providerIp
     */
    public static void disConnect(String providerServiceName, String providerIp) {
        // 移除指定服务提供者的 IP 地址，表示连接已断开
        SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isNotEmptyList(channelFutureWrappers)) {
            Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.iterator();
            // 遍历连接信息列表，找到与指定 IP 地址和端口号相匹配的连接信息，并将其从列表中移除，表示断开连接
            while (iterator.hasNext()) {
                ChannelFutureWrapper channelFutureWrapper = iterator.next();
                if (providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 默认走随机策略获取ChannelFuture
     *
     * @param providerServiceName
     * @return
     */
    public static ChannelFuture getChannelFuture(String providerServiceName) {
        // 获取与指定服务名称关联的连接信息列表
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        // 随机获取一个与服务提供者的连接，并返回
        // ChannelFuture channelFuture = channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        return ROUTER.select(selector).getChannelFuture();
    }
}
