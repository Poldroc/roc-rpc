package com.poldroc.rpc.framework.core.registry.zookeeper;

import com.poldroc.rpc.framework.core.registry.RegistryService;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;

import java.util.List;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * 注册中心抽象类，对一些注册数据做统一的处理
 *
 * @author Poldroc
 * @date 2023/9/15
 */

public abstract class AbstractRegister implements RegistryService {

    /**
     * 注册服务
     *
     * @param sUrl
     */
    public void register(ServiceUrl sUrl) {
        PROVIDER_URL_SET.add(sUrl);
    }

    /**
     * 服务下线
     *
     * @param sUrl
     */
    public void unRegister(ServiceUrl sUrl) {
        PROVIDER_URL_SET.remove(sUrl);
    }

    /**
     * 消费方订阅服务
     *
     * @param sUrl
     */
    public void subscribe(ServiceUrl sUrl) {
        SUBSCRIBE_SERVICE_LIST.add(sUrl.getServiceName());
    }


    /**
     * 留给子类扩展
     *
     * @param sUrl
     */
    public abstract void doAfterSubscribe(ServiceUrl sUrl);


    /**
     * 留给子类扩展
     *
     * @param sUrl
     */
    public abstract void doBeforeSubscribe(ServiceUrl sUrl);


    /**
     * 留给子类扩展
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

    /**
     * 消费方取消订阅服务
     * @param url 服务url
     */
    @Override
    public void doUnSubscribe(ServiceUrl url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

}
