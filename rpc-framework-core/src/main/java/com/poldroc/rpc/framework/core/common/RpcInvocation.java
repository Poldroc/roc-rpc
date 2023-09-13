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
         * 请求的目标接口名称，例如：HelloService
         */
        private String targetServiceName;

        /**
         * 请求的参数
         */
        private Object[] args;

        /**
         * 请求的唯一标识，用于异步调用时，标识请求和响应的对应关系
         * 当请求从客户端发出的时候，会有一个uuid用于记录发出的请求，待数据返回的时候通过uuid来匹配对应的请求线程，并且返回给调用线程
         */
        private String uuid;

        /**
         * 接口响应的数据塞入这个字段中（如果是异步调用或者void类型，这里就为空）
         */
        private Object response;


}
