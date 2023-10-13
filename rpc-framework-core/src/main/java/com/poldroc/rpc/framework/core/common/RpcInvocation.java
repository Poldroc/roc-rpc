package com.poldroc.rpc.framework.core.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC自定义协议请求的封装类
 * @author Poldroc
 * @date 2023/9/12
 */

public class RpcInvocation implements Serializable {
        private static final long serialVersionUID = -3611379458492006176L;

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

        /**
         * 附加属性
         */
        private Map<String,Object> attachments = new ConcurrentHashMap<>();

        /**
         * 主要用于记录服务端抛出的异常信息
         */
        private Throwable e;

        /**
         * 重试次数
         */
        private int retry;

        public int getRetry() {
                return retry;
        }

        public void setRetry(int retry) {
                this.retry = retry;
        }

        public Throwable getE() {
                return e;
        }

        public void setE(Throwable e) {
                this.e = e;
        }

        public Map<String, Object> getAttachments() {
                return attachments;
        }

        public void setAttachments(Map<String, Object> attachments) {
                this.attachments = attachments;
        }

        public Object getResponse() {
                return response;
        }

        public void setResponse(Object response) {
                this.response = response;
        }

        public String getUuid() {
                return uuid;
        }

        public void setUuid(String uuid) {
                this.uuid = uuid;
        }

        public String getTargetMethod() {
                return targetMethod;
        }

        public void setTargetMethod(String targetMethod) {
                this.targetMethod = targetMethod;
        }

        public String getTargetServiceName() {
                return targetServiceName;
        }

        public void setTargetServiceName(String targetServiceName) {
                this.targetServiceName = targetServiceName;
        }

        public Object[] getArgs() {
                return args;
        }

        public void setArgs(Object[] args) {
                this.args = args;
        }

        @Override
        public String toString() {
                return "RpcInvocation{" +
                        "targetMethod='" + targetMethod + '\'' +
                        ", targetServiceName='" + targetServiceName + '\'' +
                        ", args=" + Arrays.toString(args) +
                        ", response=" + response +
                        ", e=" + e +
                        ", retry=" + retry +
                        ", attachments=" + attachments +
                        '}';
        }

}
