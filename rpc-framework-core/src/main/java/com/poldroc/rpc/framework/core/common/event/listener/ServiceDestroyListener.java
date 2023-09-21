package com.poldroc.rpc.framework.core.common.event.listener;

import com.poldroc.rpc.framework.core.common.event.RpcDestroyEvent;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;
import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.REGISTRY_SERVICE;

/**
 * 服务注销 监听器
 * @author Poldroc
 * @date 2023/9/21
 */

public class ServiceDestroyListener implements RpcListener<RpcDestroyEvent> {

    @Override
    public void callBack(Object t) {
        for (ServiceUrl url : PROVIDER_URL_SET) {
            REGISTRY_SERVICE.unRegister(url);
        }
    }
}
