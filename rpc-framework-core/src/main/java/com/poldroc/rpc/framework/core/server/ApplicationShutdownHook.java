package com.poldroc.rpc.framework.core.server;

import com.poldroc.rpc.framework.core.common.event.RpcDestroyEvent;
import com.poldroc.rpc.framework.core.common.event.RpcListenerLoader;
import lombok.extern.slf4j.Slf4j;
/**
 * 应用关闭钩子，用于在jvm进程关闭的时候触发一些事件，比如注销服务等等
 * @author Poldroc
 * @date 2023/9/22
 */

@Slf4j
public class ApplicationShutdownHook {

    /**
     * 注册一个shutdownHook的钩子，当jvm进程关闭的时候触发
     */
    public static void registryShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("======== registryShutdownHook ========");
                RpcListenerLoader.sendSyncEvent(new RpcDestroyEvent("destroy"));
                log.info("destroy");
            }
        }));
    }

}
