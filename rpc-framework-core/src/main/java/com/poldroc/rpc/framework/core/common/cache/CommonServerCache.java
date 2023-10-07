package com.poldroc.rpc.framework.core.common.cache;

import com.poldroc.rpc.framework.core.common.ServerServiceSemaphoreWrapper;
import com.poldroc.rpc.framework.core.common.config.ServerConfig;
import com.poldroc.rpc.framework.core.dispatcher.ServerChannelDispatcher;
import com.poldroc.rpc.framework.core.filter.server.ServerAfterFilterChain;
import com.poldroc.rpc.framework.core.filter.server.ServerBeforeFilterChain;
import com.poldroc.rpc.framework.core.registry.RegistryService;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import com.poldroc.rpc.framework.core.serialize.SerializeFactory;
import com.poldroc.rpc.framework.core.server.ServiceWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 服务缓存 存储服务端信息
 * @author Poldroc
 * @date 2023/9/15
 */

public class CommonServerCache {

    /**
     * 服务端缓存，保存服务实现的接口名和服务实现类的映射关系
     */
    public static final Map<String,Object> PROVIDER_CLASS_MAP = new HashMap<>();

    /**
     * 服务端缓存，保存服务url
     */
    public static final Set<ServiceUrl> PROVIDER_URL_SET = new HashSet<>();

    /**
     * 注册中心服务
     */
    public static RegistryService REGISTRY_SERVICE;

    /**
     * 服务端序列化工厂
     */
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

    /**
     * 服务端配置
     */
    public static ServerConfig SERVER_CONFIG;

    /**
     * 服务端前置过滤器链
     */
    public static ServerBeforeFilterChain SERVER_BEFORE_FILTER_CHAIN;

    /**
     * 服务端后置过滤器链
     */
    public static ServerAfterFilterChain SERVER_AFTER_FILTER_CHAIN;

    /**
     * 服务端缓存，保存服务实现类的包装类
     */
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();

    /**
     * 服务端是否已经启动
     */
    public static Boolean IS_STARTED = false;

    /**
     * 服务端通道分发器
     */
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();

    /**
     * 服务限流器缓存，保存服务端限流器
     */
    public static final Map<String, ServerServiceSemaphoreWrapper> SERVER_SERVICE_SEMAPHORE_MAP = new ConcurrentHashMap<>(64);
}
