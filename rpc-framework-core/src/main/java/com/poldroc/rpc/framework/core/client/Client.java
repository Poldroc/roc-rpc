package com.poldroc.rpc.framework.core.client;

import com.alibaba.fastjson2.JSON;
import com.poldroc.rpc.framework.core.common.RpcDecoder;
import com.poldroc.rpc.framework.core.common.RpcEncoder;
import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import com.poldroc.rpc.framework.core.common.config.ClientConfig;
import com.poldroc.rpc.framework.core.proxy.jdk.JDKProxyFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

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
    public static EventLoopGroup clientGroup = null;

    /**
     * 客户端配置对象
     */
    private ClientConfig clientConfig;

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
    public RpcReference startApplication() throws InterruptedException {
        // 客户端线程组
        clientGroup = new NioEventLoopGroup();
        // 创建一个启动器
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //管道中初始化一些逻辑，这里包含了编解码器和服务端响应处理器
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
        // 连接netty服务器
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
        log.info("============ 服务启动 ============");
        // 启动客户端发送线程
        this.startClient(channelFuture);
        // 注入一个代理工厂，创建一个 RPC 代理对象，用于远程调用服务
        return new RpcReference(new JDKProxyFactory());
    }

/*    public static void main(String[] args) throws Throwable {
        // 创建客户端实例
        Client client = new Client();

        // 创建客户端配置对象，设置服务器地址和端口
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPort(9090);
        clientConfig.setServerAddr("localhost");
        client.setClientConfig(clientConfig);

        // 启动客户端应用程序
        RpcReference rpcReference = client.startClientApplication();

        // 获取远程服务代理
        DataService dataService = rpcReference.get(DataService.class);

        // 循环调用远程服务方法并打印结果
        for (int i = 0; i < 100; i++) {
            String result = dataService.sendData("test");
            System.out.println(result);
        }
    }*/

    /**
     * 开启发送线程 专门从事将数据包发送给服务端，起到一个解耦的效果
     *
     * @param channelFuture 通道的异步操作
     */
    private void startClient(ChannelFuture channelFuture) {
        // 创建一个异步发送线程
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
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
                channelFuture.channel().writeAndFlush(rpcProtocol);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } /*finally {
                // 关闭客户端线程组
                clientGroup.shutdownGracefully();
            }*/
        }


    }
}
