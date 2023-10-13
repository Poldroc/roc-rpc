package com.poldroc.rpc.framework.spring.starter.config;

import com.poldroc.rpc.framework.core.common.event.RpcListenerLoader;
import com.poldroc.rpc.framework.core.server.ApplicationShutdownHook;
import com.poldroc.rpc.framework.core.server.Server;
import com.poldroc.rpc.framework.core.server.ServiceWrapper;
import com.poldroc.rpc.framework.spring.starter.annotations.ARpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * rpc服务端的自动装配类
 * @author Poldroc
 * @date 2023/10/7
 */
public class RpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerAutoConfiguration.class);

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        // 获取带有ARpcService注解的Bean，这些Bean将被暴露为RPC服务
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(ARpcService.class);
        if (beanMap.size() == 0) {
            // 说明当前应用内部不需要对外暴露服务
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        server = new Server();
        server.initServerConfig();
        // 加载RPC监听器
        RpcListenerLoader rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            // 获取每个Bean的ARpcService注解，用于获取一些配置信息
            ARpcService aRpcService = bean.getClass().getAnnotation(ARpcService.class);
            ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(bean, aRpcService.group());
            dataServiceServiceWrapper.setServiceToken(aRpcService.serviceToken());
            dataServiceServiceWrapper.setLimit(aRpcService.limit());
            // RPC服务暴露给RPC框架，以便客户端可以调用
            server.exportService(dataServiceServiceWrapper);
            LOGGER.info(">>>>>>>>>>>>>>> [rpc] {} export success! >>>>>>>>>>>>>>> ",beanName);
        }
        long end = System.currentTimeMillis();
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
        LOGGER.info(" ================== [{}] started success in {}s ================== ",server.getServerConfig().getApplicationName(),((double)end-(double)begin)/1000);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private void printBanner(){
        System.out.println();
        System.out.println("==============================================");
        System.out.println("|||---------- Rpc Starting Now! ----------|||");
        System.out.println("==============================================");
        System.out.println("version: 1.0.0");
        System.out.println();
    }
}
