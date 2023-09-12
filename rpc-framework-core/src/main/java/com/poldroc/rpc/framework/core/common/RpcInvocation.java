package com.poldroc.rpc.framework.core.common;

import lombok.Data;

/**
 * RPC自定义协议请求的封装类
 * @author Poldroc
 * @date 2023/9/12
 */
@Data
public class RpcInvocation {

        /**
         * 请求的目标方法名称，例如：sayHello
         */
        private String targetMethod;

        /**
         * 请求的目标接口名称，例如：com.poldroc.rpc.framework.test.api.HelloService
         */
        private String targetServiceName;

        /**
         * 请求的参数
         */
        private Object[] args;

        /**
         * 请求的唯一标识
         */
        private String uuid;

        /**
         * 接口响应的数据塞入这个字段中（如果是异步调用或者void类型，这里就为空）
         */
        private Object response;


}
