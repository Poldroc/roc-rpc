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
import com.poldroc.rpc.framework.core.proxy.ProxyFactory;
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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
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
 *
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
    private RpcListenerLoader rpcListenerLoader;

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
    public RpcReference initClientApplication() throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // 客户端线程组
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        // 创建一个启动器
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 设置分隔符
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(clientConfig.getMaxServerRespDataSize(), delimiter));
                // 管道中初始化一些逻辑，这里包含了编解码器和服务端响应处理器
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
        // 初始化RPC监听器加载器
        rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        CLIENT_CONFIG = this.clientConfig;
        // spi扩展的加载部分
        this.initClientConfig();

        // 代理工厂
        EXTENSION_LOADER.loadExtension(ProxyFactory.class);
        String proxyType = clientConfig.getProxyType();
        LinkedHashMap<String, Class> classMap = EXTENSION_LOADER_CLASS_CACHE.get(ProxyFactory.class.getName());
        Class proxyClassType = classMap.get(proxyType);
        ProxyFactory proxyFactory = (ProxyFactory) proxyClassType.newInstance();
        return new RpcReference(proxyFactory);
    }

    /**
     * 启动服务之前需要预先订阅对应的服务
     *
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean) {
        log.info("doSubscribeService start ====> serviceBean Name:{}", serviceBean.getName());
        if (ABSTRACT_REGISTER == null) {
            try {
                // 使用自定义的SPI机制去加载配置
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String, Class> registerMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registerClass = registerMap.get(clientConfig.getRegisterType());
                // 通过反射创建注册中心对象
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
        URL_MAP.put(serviceBean.getName(), result);
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
            url.addParameter("servicePath", providerUrl.getServiceName() + "/provider");
            url.addParameter("providerIps", com.alibaba.fastjson.JSON.toJSONString(providerIps));
            // 客户端在此新增一个订阅的功能
            ABSTRACT_REGISTER.doAfterSubscribe(url);
        }
    }

    /**
     * 开启发送线程 专门从事将数据包发送给服务端，起到一个解耦的效果
     */
    public void startClient() {
        log.info("======== start client ========");
        // 创建一个异步发送线程
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    /**
     * 异步发送信息任务
     */
    class AsyncSendJob implements Runnable {


        public AsyncSendJob() {
        }

        /**
         * 重写run方法
         */
        @Override
        public void run() {
            while (true) {
                try {
                    // 阻塞模式
                    log.info("======== AsyncSendJob start ========");
                    RpcInvocation rpcInvocation = SEND_QUEUE.take();
                    log.info("rpcInvocation : {}", rpcInvocation);
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(rpcInvocation);
                    if (channelFuture != null) {
                        Channel channel = channelFuture.channel();
                        //如果出现服务端中断的情况需要兼容下
                        if (channel.isOpen()) {
                            RpcProtocol rpcProtocol = new RpcProtocol(CLIENT_SERIALIZE_FACTORY.serialize(rpcInvocation));
                            channel.writeAndFlush(rpcProtocol);
                        }
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                    log.error("[AsyncSendJob] run fail ", e);
                } /*finally {
                // 关闭客户端线程组
                clientGroup.shutdownGracefully();
            }*/
            }
        }
    }

    /**
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
