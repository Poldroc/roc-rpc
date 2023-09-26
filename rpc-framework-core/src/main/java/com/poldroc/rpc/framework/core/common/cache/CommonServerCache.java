package com.poldroc.rpc.framework.core.common.cache;

import com.poldroc.rpc.framework.core.common.config.ServerConfig;
import com.poldroc.rpc.framework.core.filter.server.ServerFilterChain;
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
     * 服务端过滤器链
     */
    public static ServerFilterChain SERVER_FILTER_CHAIN;

    /**
     * 服务端缓存，保存服务实现类的包装类
     */
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();
}
