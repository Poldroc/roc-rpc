package com.poldroc.rpc.framework.core.server;

import com.poldroc.rpc.framework.core.common.RpcDecoder;
import com.poldroc.rpc.framework.core.common.RpcEncoder;
import com.poldroc.rpc.framework.core.common.config.PropertiesBootstrap;
import com.poldroc.rpc.framework.core.common.config.ServerConfig;
import com.poldroc.rpc.framework.core.common.event.RpcListenerLoader;
import com.poldroc.rpc.framework.core.common.utils.CommonUtils;
import com.poldroc.rpc.framework.core.filter.ServerFilter;
import com.poldroc.rpc.framework.core.filter.server.ServerFilterChain;
import com.poldroc.rpc.framework.core.registry.RegistryService;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import com.poldroc.rpc.framework.core.registry.zookeeper.AbstractRegister;
import com.poldroc.rpc.framework.core.registry.zookeeper.ZookeeperRegister;
import com.poldroc.rpc.framework.core.serialize.SerializeFactory;
import com.poldroc.rpc.framework.core.serialize.fastjson.FastJsonSerializeFactory;
import com.poldroc.rpc.framework.core.serialize.hessian.HessianSerializeFactory;
import com.poldroc.rpc.framework.core.serialize.jdk.JdkSerializeFactory;
import com.poldroc.rpc.framework.core.serialize.kryo.KryoSerializeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.EXTENSION_LOADER;
import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.*;
import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.*;
import static com.poldroc.rpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;


/**
 * RPC服务端，用于接收客户端的请求并返回响应结果
 *
 * @author Poldroc
 * @date 2023/9/12
 */

@Slf4j
public class Server {

    /**
     * 处理连接的主线程组
     */
    private static EventLoopGroup bossGroup = null;

    /**
     * 处理数据的工作线程组
     */
    private static EventLoopGroup workerGroup = null;

    private ServerConfig serverConfig;

    private static RpcListenerLoader rpcListenerLoader;

    private RegistryService registryService;

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    /**
     * 启动服务端
     *
     * @throws InterruptedException
     */
    public void startApplication() throws InterruptedException {

        // 一个用于处理连接的主线程组（bossGroup），一个用于处理数据的工作线程组（workerGroup）
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        // 设置服务器的相关配置
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 设置主从线程组
        bootstrap.group(bossGroup, workerGroup);
        // 设置通道类型
        bootstrap.channel(NioServerSocketChannel.class);

        // 设置TCP参数
        // 是否开启Nagle算法，true表示关闭，false表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                // 服务器可以排队等待处理的最大连接数为102
                .childOption(ChannelOption.SO_BACKLOG, 1024);
        // 设置套接字发送缓冲区大小大小为16KB，影响着服务器向客户端发送数据的性能
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                // 设置套接字接收缓冲区大小大小为16KB，影响着服务器接收客户端数据的性能
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                // 是否开启TCP底层心跳机制，true为开启
                .option(ChannelOption.SO_KEEPALIVE, true);

        // 设置通道初始化，用于设置每条连接的数据读写，业务处理逻辑
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                log.info("============ 初始化provider过程 ============");
                // 添加编码器、解码器和自定义的服务器处理器
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        this.batchExportUrl();
        // 绑定端口，同步等待成功
        bootstrap.bind(serverConfig.getServerPort()).sync();
        IS_STARTED = true;
    }

    public void initServerConfig() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
        SERVER_CONFIG = serverConfig;
        // 序列化技术初始化
        String serverSerialize = serverConfig.getServerSerialize();
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class> serializeFactoryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeFactoryClass = serializeFactoryClassMap.get(serverSerialize);
        if (serializeFactoryClass == null) {
            throw new RuntimeException("no match serialize type for " + serverSerialize);
        }
        SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeFactoryClass.newInstance();

        // 过滤链初始化
        EXTENSION_LOADER.loadExtension(ServerFilter.class);
        LinkedHashMap<String, Class> serverFilterClassMap = EXTENSION_LOADER_CLASS_CACHE.get(ServerFilter.class.getName());
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        for (String key : serverFilterClassMap.keySet()) {
            Class serverFilterClass = serverFilterClassMap.get(key);
            if (serverFilterClass == null) {
                throw new RuntimeException("no match serverFilter type for " + key);
            }
            serverFilterChain.addServerFilter((ServerFilter) serverFilterClass.newInstance());
        }
        SERVER_FILTER_CHAIN = serverFilterChain;
    }

    /**
     * 暴露服务信息
     *
     * @param serviceWrapper 服务包装类
     */
    public void exportService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceObj();
        // 判断服务是否实现了接口
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        // 判断服务是否只实现了一个接口
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        if (REGISTRY_SERVICE == null) {
            try {
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String, Class> registryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registryClass = registryClassMap.get(serverConfig.getRegisterType());
                REGISTRY_SERVICE = (AbstractRegister) registryClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }
        // 获取服务实现的接口
        Class interfaceClass = classes[0];
        // 将服务实现的接口名和服务实现类的映射关系存入缓存
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
        ServiceUrl serviceUrl = new ServiceUrl();
        serviceUrl.setServiceName(interfaceClass.getName());
        serviceUrl.setApplicationName(serverConfig.getApplicationName());
        // 设置服务提供者的IP地址和端口号
        serviceUrl.addParameter(HOST, CommonUtils.getIpAddress());
        serviceUrl.addParameter(PORT, String.valueOf(serverConfig.getServerPort()));
        serviceUrl.addParameter(GROUP, String.valueOf(serviceWrapper.getGroup()));
        serviceUrl.addParameter(LIMIT, String.valueOf(serviceWrapper.getLimit()));
        PROVIDER_URL_SET.add(serviceUrl);
        if (CommonUtils.isNotEmpty(serviceWrapper.getServiceToken())) {
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }
    }

    /**
     * 批量暴露服务信息
     * 将服务器上的服务注册到远程服务发现机制中，以供客户端发现和调用
     */
    public void batchExportUrl() {
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (ServiceUrl serviceUrl : PROVIDER_URL_SET) {
                    REGISTRY_SERVICE.register(serviceUrl);
                }
            }
        });
        task.start();
    }

    /**
     * 关闭服务端
     */
    public void close() {
        // 关闭主线程组
        bossGroup.shutdownGracefully();
        // 关闭工作线程组
        workerGroup.shutdownGracefully();
    }

/*    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        Server server = new Server();
        server.initServerConfig();
        rpcListenerLoader = new RpcListenerLoader();
        rpcListenerLoader.init();
        ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(new DataServiceImpl(), "dev");
        dataServiceServiceWrapper.setServiceToken("token-a");
        dataServiceServiceWrapper.setLimit(2);
        ServiceWrapper userServiceServiceWrapper = new ServiceWrapper(new UserServiceImpl(), "dev");
        userServiceServiceWrapper.setServiceToken("token-b");
        userServiceServiceWrapper.setLimit(2);
        server.exportService(dataServiceServiceWrapper);
        server.exportService(userServiceServiceWrapper);
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
    }*/
}
