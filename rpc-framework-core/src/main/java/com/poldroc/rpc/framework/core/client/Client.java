package com.poldroc.rpc.framework.core.client;

import com.alibaba.fastjson2.JSON;
import com.poldroc.rpc.framework.core.common.RpcDecoder;
import com.poldroc.rpc.framework.core.common.RpcEncoder;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import com.poldroc.rpc.framework.core.common.config.ClientConfig;
import com.poldroc.rpc.framework.core.common.config.PropertiesBootstrap;
import com.poldroc.rpc.framework.core.common.event.RpcListenerLoader;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.filter.ClientFilter;
import com.poldroc.rpc.framework.core.filter.client.ClientFilterChain;
import com.poldroc.rpc.framework.core.proxy.jdk.JDKProxyFactory;
import com.poldroc.rpc.framework.core.registry.RegistryService;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import com.poldroc.rpc.framework.core.registry.zookeeper.AbstractRegister;
import com.poldroc.rpc.framework.core.registry.zookeeper.ZookeeperRegister;
import com.poldroc.rpc.framework.core.router.RandomRouterImpl;
import com.poldroc.rpc.framework.core.router.RotateRouterImpl;
import com.poldroc.rpc.framework.core.router.Router;
import com.poldroc.rpc.framework.core.serialize.SerializeFactory;
import com.poldroc.rpc.framework.core.serialize.fastjson.FastJsonSerializeFactory;
import com.poldroc.rpc.framework.core.serialize.hessian.HessianSerializeFactory;
import com.poldroc.rpc.framework.core.serialize.jdk.JdkSerializeFactory;
import com.poldroc.rpc.framework.core.serialize.kryo.KryoSerializeFactory;
import com.poldroc.rpc.framework.interfaces.DataService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.*;
import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.*;
import static com.poldroc.rpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

/**
 * RPC客户端类
 * 客户端首先需要通过一个代理工厂获取被调用对象的代理对象，
 * 然后通过代理对象将数据放入发送队列，
 * 最后会有一个异步线程将发送队列内部的数据一个个地发送给到服务端，
 * 并且等待服务端响应对应的数据结果。
 * @author Poldroc
 * @date 2023/9/14
 */

@Slf4j
public class Client {

    /**
     * 客户端线程组
     */
    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    /**
     * 客户端配置对象
     */
    private ClientConfig clientConfig;

    /**
     * 服务端监听器加载器
     */
    private RpcListenerLoader RpcListenerLoader;

    /**
     * netty配置对象
     */
    private Bootstrap bootstrap = new Bootstrap();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    /**
     * 获取客户端配置对象
     */
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    /**
     * 设置客户端配置对象
     */
    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    /**
     * 启动客户端
     *
     * @throws InterruptedException
     */
    public RpcReference initClientApplication() throws InterruptedException {
        // 客户端线程组
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        // 创建一个启动器
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 管道中初始化一些逻辑，这里包含了编解码器和服务端响应处理器
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
        // 初始化RPC监听器加载器
        RpcListenerLoader = new RpcListenerLoader();
        RpcListenerLoader.init();
        // 从本地加载客户端配置
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        return new RpcReference(new JDKProxyFactory());
    }

    /**
     * 启动服务之前需要预先订阅对应的dubbo服务
     *
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean) {
        log.info("doSubscribeService start ====> serviceBean Name:{}", serviceBean.getName());
        if (ABSTRACT_REGISTER == null) {
            try {
                // //使用自定义的SPI机制去加载配置
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String, Class> registerMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registerClass =  registerMap.get(clientConfig.getRegisterType());
                //
                ABSTRACT_REGISTER = (AbstractRegister) registerClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
        ServiceUrl url = new ServiceUrl();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter(HOST, CommonUtils.getIpAddress());
        Map<String, String> result = ABSTRACT_REGISTER.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(),result);
        // 把客户端的信息注册到注册中心
        ABSTRACT_REGISTER.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     * 客户端和服务提供端建立连接的时候，会触发
     */
    public void doConnectServer() {
        log.info("======== doConnectServer start ========");
        // 遍历名为 SUBSCRIBE_SERVICE_LIST 的服务列表，这些服务列表是之前使用 doSubscribeService 方法订阅的服务
        for (ServiceUrl providerUrl : SUBSCRIBE_SERVICE_LIST) {
            // 从注册中心获取其 IP 地址列表
            List<String> providerIps = ABSTRACT_REGISTER.getProviderIps(providerUrl.getServiceName());
            for (String providerIp : providerIps) {
                try {
                    // 循环遍历每个 IP 地址，调用 ConnectionHandler.connect 方法来与服务提供者建立连接
                    ConnectionHandler.connect(providerUrl.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    log.error("[doConnectServer] connect fail ", e);
                }
            }
            ServiceUrl url = new ServiceUrl();
            // url.setServiceName(providerServiceName);
            url.addParameter("servicePath",providerUrl.getServiceName()+"/provider");
            url.addParameter("providerIps", com.alibaba.fastjson.JSON.toJSONString(providerIps));
            //客户端在此新增一个订阅的功能
            ABSTRACT_REGISTER.doAfterSubscribe(url);
        }
    }

    /**
     * 开启发送线程 专门从事将数据包发送给服务端，起到一个解耦的效果
     *
     */
    private void startClient() {
        log.info("======== start client ========");
        // 创建一个异步发送线程
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    /**
     * 异步发送信息任务
     */
    class AsyncSendJob implements Runnable {

        /**
         * 通道的异步操作
         */
        private ChannelFuture channelFuture;

        /**
         * 构造函数
         *
         * @param channelFuture 通道的异步操作
         */
        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public AsyncSendJob() {
        }

        /**
         * 重写run方法
         */
        @Override
        public void run() {
            try {
                // 阻塞模式
                // 从发送队列中取出 RpcInvocation 对象（需要发送的远程调用信息）
                RpcInvocation data = SEND_QUEUE.take();
                // 将 RpcInvocation 封装成 RpcProtocol 对象，并发送给服务端
                String json = JSON.toJSONString(data);
                RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                channelFuture.channel().writeAndFlush(rpcProtocol);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } /*finally {
                // 关闭客户端线程组
                clientGroup.shutdownGracefully();
            }*/
        }
    }

    /**
     *
     * SPI机制加载配置
     */
    private void initClientConfig() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        log.info("======== init client config ========");
        // 初始化路由策略
        EXTENSION_LOADER.loadExtension(Router.class);
        String routerStrategy = clientConfig.getRouterStrategy();
        LinkedHashMap<String, Class> routerMap = EXTENSION_LOADER_CLASS_CACHE.get(Router.class.getName());
        Class routerClass = routerMap.get(routerStrategy);
        if (routerClass == null) {
            throw new RuntimeException("no match routerStrategy for " + routerStrategy);
        }
        ROUTER = (Router) routerClass.newInstance();

        // 初始化序列化框架
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        String clientSerialize = clientConfig.getClientSerialize();
        LinkedHashMap<String, Class> serializeMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeFactoryClass = serializeMap.get(clientSerialize);
        if (serializeFactoryClass == null) {
            throw new RuntimeException("no match serialize type for " + clientSerialize);
        }
        CLIENT_SERIALIZE_FACTORY = (SerializeFactory) serializeFactoryClass.newInstance();

        // 初始化过滤链
        EXTENSION_LOADER.loadExtension(ClientFilter.class);
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        LinkedHashMap<String, Class> clientFilterMap = EXTENSION_LOADER_CLASS_CACHE.get(ClientFilter.class.getName());
        for (String implClassName : clientFilterMap.keySet()) {
            Class clientFilterClass = clientFilterMap.get(implClassName);
            if (clientFilterClass == null) {
                throw new RuntimeException("no match clientFilter for " + implClassName);
            }
            clientFilterChain.addClientFilter((ClientFilter) clientFilterClass.newInstance());
        }
        CLIENT_FILTER_CHAIN = clientFilterChain;
    }


/*
    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        RpcReference rpcReference = client.initClientApplication();
        client.initClientConfig();
        DataService dataService = rpcReference.get(DataService.class);
        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        client.doConnectServer();
        client.startClient();
        for (int i = 0; i < 100; i++) {
            try {
                String result = dataService.sendData("test");
                System.out.println(result);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
*/

}
