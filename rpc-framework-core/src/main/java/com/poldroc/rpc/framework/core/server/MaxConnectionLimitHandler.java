package com.poldroc.rpc.framework.core.server;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 服务端最大连接数限制处理器
 * @author Poldroc
 * @date 2023/10/6
 */
@ChannelHandler.Sharable
@Slf4j
public class MaxConnectionLimitHandler extends ChannelInboundHandlerAdapter {

    /**
     * 最大连接数
     */
    private final int maxConnectionNums;

    /**
     * 当前连接数 线程安全的方式
     */
    private final AtomicInteger numConnection = new AtomicInteger(0);

    /**
     * 子连接的Channel对象
     */
    private final Set<Channel> childChannel = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /**
     * 记录被丢弃的连接数量 这是在jdk1.8之后出现的对于AtomicLong的优化版本
     */
    private final LongAdder numDroppedConnections = new LongAdder();

    /**
     * 用于标记是否已经调度了日志打印任务
     */
    private final AtomicBoolean loggingScheduled = new AtomicBoolean(false);

    public MaxConnectionLimitHandler(int maxConnectionNums) {
        this.maxConnectionNums = maxConnectionNums;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = (Channel) msg;
        // 连接数加一
        int conn = numConnection.incrementAndGet();
        // 如果连接数小于最大连接数，将channel加入到childChannel中
        if (conn > 0 && conn <= maxConnectionNums) {
            this.childChannel.add(channel);
            // 添加监听器，当channel关闭时，将channel从childChannel中移除，并将连接数减一
            channel.closeFuture().addListener(future -> {
                childChannel.remove(channel);
                numConnection.decrementAndGet();
            });
            super.channelRead(ctx, msg);
        } else {
            // 递减连接计数器
            numConnection.decrementAndGet();
            // 避免产生大量的time_wait连接
            // 设置SO_LINGER为0，表示立即关闭连接
            channel.config().setOption(ChannelOption.SO_LINGER, 0);
            // 强制关闭channel
            channel.unsafe().closeForcibly();
            // 递增丢弃连接计数器
            numDroppedConnections.increment();
            // 这里加入一道CAS（Compare-And-Swap）操作来确保只有一个线程安排了日志记录，并且在1秒后调度writeNumDroppedConnectionLog方法
            if (loggingScheduled.compareAndSet(false, true)) {
                ctx.executor().schedule(this::writeNumDroppedConnectionLog, 1, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 记录连接失败的日志
     */
    private void writeNumDroppedConnectionLog() {
        // 将标记设置为false
        loggingScheduled.set(false);
        // 获取丢弃的连接数并重置计数器
        final long dropped = numDroppedConnections.sumThenReset();
        // 记录日志
        if (dropped > 0) {
            log.error("Dropped {} connections because of connection limit", dropped);
        }
    }

}
