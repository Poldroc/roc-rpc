package com.poldroc.rpc.framework.core.common.cache;

import com.poldroc.rpc.framework.core.common.RpcInvocation;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 公用缓存 存储请求队列等公共信息
 * @author Poldroc
 * @date 2023/9/14
 */

public class CommonClientCache {

    /**
     * 请求队列 用于存储请求信息
     */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);

    /**
     * 响应结果集
     */
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
}
