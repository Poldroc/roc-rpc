package com.poldroc.rpc.framework.core.common.cache;

import com.poldroc.rpc.framework.core.common.ChannelFuturePollingRef;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.config.ClientConfig;
import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.filter.client.ClientFilterChain;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import com.poldroc.rpc.framework.core.router.Router;
import com.poldroc.rpc.framework.core.serialize.SerializeFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 公用缓存 存储请求队列等公共信息
 * @author Poldroc
 * @date 2023/9/14
 */

public class CommonClientCache {

    /**
     * 请求队列 用于存储请求信息
     */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);

    /**
     * 响应结果集
     */
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();

    /**
     * provider名称 --> 该服务有哪些集群URL
     */
    public static List<ServiceUrl> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    /**
     * 服务提供者的地址
     */
    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>();

    /**
     * 服务提供者的地址
     */
    public static Set<String> SERVER_ADDRESS = new HashSet<>();
    /**
     * 每次进行远程调用的时候都是从这里面去选择服务提供者
     */
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();

    /**
     * 随机请求的map
     */
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();

    /**
     * 轮询请求的map
     */
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();

    /**
     * 路由器
     */
    public static Router ROUTER;

    /**
     * 客户端序列化工厂
     */
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;

    /**
     * 客户端配置
     */
    public static ClientConfig CLIENT_CONFIG;

    /**
     * 客户端过滤器链
     */
    public static ClientFilterChain CLIENT_FILTER_CHAIN ;

}
