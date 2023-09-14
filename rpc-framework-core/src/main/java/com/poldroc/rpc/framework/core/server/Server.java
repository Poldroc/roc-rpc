package com.poldroc.rpc.framework.core.server;

import com.poldroc.rpc.framework.core.common.RpcDecoder;
import com.poldroc.rpc.framework.core.common.RpcEncoder;
import com.poldroc.rpc.framework.core.common.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;


/**
 * RPC服务端，用于接收客户端的请求并返回响应结果
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

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    /**
     * 启动服务端
     * @throws InterruptedException
     */
    public void startApplication() throws  InterruptedException{

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
                log.info("初始化provider过程");
                // 添加编码器、解码器和自定义的服务器处理器
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        // 绑定端口，同步等待成功
        bootstrap.bind(serverConfig.getPort()).sync();
    }

    /**
     * 注册服务 服务名和服务实现类的映射关系
     * @param serviceBean 服务实现类
     */
    public void registyService(Object serviceBean) {
        // 判断服务是否实现了接口
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        // 判断服务是否只实现了一个接口
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        // 获取服务实现的接口
        Class interfaceClass = classes[0];
        // 将服务实现的接口名和服务实现类的映射关系存入缓存
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
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


}