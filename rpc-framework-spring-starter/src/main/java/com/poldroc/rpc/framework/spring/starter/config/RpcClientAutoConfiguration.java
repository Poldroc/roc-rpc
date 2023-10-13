package com.poldroc.rpc.framework.spring.starter.config;

import com.poldroc.rpc.framework.core.client.Client;
import com.poldroc.rpc.framework.core.client.ConnectionHandler;
import com.poldroc.rpc.framework.core.client.RpcReference;
import com.poldroc.rpc.framework.core.client.RpcReferenceWrapper;
import com.poldroc.rpc.framework.spring.starter.annotations.ARpcReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;

/**
 * rpc客户端的自动装配类
 *
 * @author Poldroc
 * @date 2023/10/7
 */
public class RpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {
    /**
     * 用于存储rpc服务的引用
     */
    private static RpcReference rpcReference = null;
    /**
     * 用于存储rpc客户端的配置
     */
    private static Client client = null;
    /**
     * 是否需要初始化客户端
     */
    private volatile boolean needInitClient = false;
    /**
     * 是否已经初始化客户端配置
     */
    private volatile boolean hasInitClientConfig = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientAutoConfiguration.class);

    /**
     * Bean初始化之后执行一些操作
     *
     * @param bean     bean
     * @param beanName bean的名称
     * @return bean
     * @throws BeansException bean异常
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取该Bean的所有字段（属性）
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            // 如果该字段被ARpcReference注解修饰
            if (field.isAnnotationPresent(ARpcReference.class)) {
                // 并且尚未初始化RPC客户端配置，则初始化RPC客户端配置
                if (!hasInitClientConfig) {
                    client = new Client();
                    try {
                        rpcReference = client.initClientApplication();
                    } catch (Exception e) {
                        LOGGER.error("[IRpcClientAutoConfiguration] postProcessAfterInitialization has error ", e);
                        throw new RuntimeException(e);
                    }
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                // 获取ARpcReference注解的信息
                ARpcReference aRpcReference = field.getAnnotation(ARpcReference.class);
                try {
                    // 设置该字段可访问
                    field.setAccessible(true);
                    // 获取该字段在bean对象中的值
                    Object refObj = field.get(bean);
                    RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(aRpcReference.group());
                    rpcReferenceWrapper.setServiceToken(aRpcReference.serviceToken());
                    rpcReferenceWrapper.setServiceUrl(aRpcReference.url());
                    rpcReferenceWrapper.setTimeOut(aRpcReference.timeOut());
                    // 失败重试次数
                    rpcReferenceWrapper.setRetry(aRpcReference.retry());
                    rpcReferenceWrapper.setAsync(aRpcReference.async());
                    // 获得代理对象
                    refObj = rpcReference.get(rpcReferenceWrapper);
                    // 将获取的RPC引用对象设置回Bean对象的字段中，以便应用程序可以通过这些字段访问RPC服务
                    field.set(bean, refObj);
                    // 订阅对应类型的服务，以便接收服务提供者的变更通知
                    client.doSubscribeService(field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return bean;
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (needInitClient && client != null) {
            LOGGER.info(" ================== [{}] started success ================== ", client.getClientConfig().getApplicationName());
            ConnectionHandler.setBootstrap(client.getBootstrap());
            // 连接RPC服务端
            client.doConnectServer();
            client.startClient();
        }
    }
}