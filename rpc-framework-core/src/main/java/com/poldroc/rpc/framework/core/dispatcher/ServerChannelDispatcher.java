package com.poldroc.rpc.framework.core.dispatcher;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import com.poldroc.rpc.framework.core.common.exception.RpcException;
import com.poldroc.rpc.framework.core.server.ServerChannelReadData;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.*;

/**
 * 服务器通道分发器
 *
 * @author Poldroc
 * @date 2023/10/4
 */

public class ServerChannelDispatcher {

    /**
     * 阻塞队列
     */
    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    /**
     * 业务线程池
     */
    private ExecutorService executorService;

    public ServerChannelDispatcher() {

    }

    /**
     * 初始化 阻塞队列和业务线程池
     *
     * @param queueSize
     * @param bizThreadNums
     */
    public void init(int queueSize, int bizThreadNums) {
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);
        // 初始化业务线程池
        // 线程池的核心线程数，最大线程数目，空闲线程存活时间，时间单位，阻塞队列
        executorService = new ThreadPoolExecutor(bizThreadNums, bizThreadNums,
                // 非核心线程在执行完任务后立即被销毁，不会保持空闲
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(512));
    }

    /**
     * 将数据放入阻塞队列
     *
     * @param serverChannelReadData
     */
    public void add(ServerChannelReadData serverChannelReadData) {
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    /**
     * 专门负责将队列的数据读出，然后提及到业务线程池去执行
     */
    class ServerJobCoreHandle implements Runnable {

        /**
         * 可以实现并发处理多个请求，每个请求都在独立的线程中执行，以提高服务器的处理能力
         */
        @Override
        public void run() {
            while (true) {
                try {
                    // 阻塞式获取数据 如果队列为空，线程将阻塞等待，直到有数据可用
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    // 取出一个 ServerChannelReadData 后，将其交给 executorService 线程池中的一个线程去执行，以实现并发处理
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {

                            RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                            // 反序列化
                            RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                            try {
                                // 执行前置过滤链路
                                SERVER_BEFORE_FILTER_CHAIN.doFilter(rpcInvocation);
                            } catch (Exception cause) {
                                // 针对自定义异常进行捕获，并且直接返回异常信息给到客户端，然后打印结果
                                if (cause instanceof RpcException) {
                                    RpcException rpcException = (RpcException) cause;
                                    RpcInvocation reqParam = rpcException.getRpcInvocation();
                                    rpcInvocation.setE(rpcException);
                                    byte[] body = SERVER_SERIALIZE_FACTORY.serialize(reqParam);
                                    RpcProtocol respRpcProtocol = new RpcProtocol(body);
                                    serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                                    return;
                                }
                            }

                            // 执行目标方法
                            Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                            Method[] methods = aimObject.getClass().getDeclaredMethods();
                            Object result = null;
                            // 遍历所有方法，找到目标方法，找到与客户端请求的目标方法名匹配的方法
                            for (Method method : methods) {
                                if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                    // 如果目标方法的返回值为void，则直接调用目标方法
                                    if (method.getReturnType().equals(Void.TYPE)) {
                                        try {
                                            // 动态调用方法
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        } catch (Exception e) {
                                            // 业务异常
                                            rpcInvocation.setE(e);
                                        }
                                    } else {
                                        try {
                                            // 动态调用方法
                                            result = method.invoke(aimObject, rpcInvocation.getArgs());
                                        } catch (Exception e) {
                                            // 业务异常
                                            rpcInvocation.setE(e);
                                        }
                                    }
                                    break;
                                }
                            }
                            rpcInvocation.setResponse(result);
                            // 后置过滤器
                            SERVER_AFTER_FILTER_CHAIN.doFilter(rpcInvocation);
                            // 将结果序列化
                            RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                            // 将结果返回给客户端
                            serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 启动数据消费
     */
    public void startDataConsume() {
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }


}