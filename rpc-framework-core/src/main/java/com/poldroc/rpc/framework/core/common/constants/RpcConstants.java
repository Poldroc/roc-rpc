package com.poldroc.rpc.framework.core.common.constants;

/**
 * 常量类
 * @author Poldroc
 * @date 2023/9/21
 */

public class RpcConstants {

    public static final short MAGIC_NUMBER = 2023;

    public static final String JDK_PROXY_TYPE = "jdk";

    public static final String RANDOM_ROUTER_TYPE = "random";

    public static final String ROTATE_ROUTER_TYPE = "rotate";

    public static final String JDK_SERIALIZE_TYPE = "jdk";

    public static final String FAST_JSON_SERIALIZE_TYPE = "fastJson";

    public static final String HESSIAN2_SERIALIZE_TYPE = "hessian2";

    public static final String KRYO_SERIALIZE_TYPE = "kryo";

    public static final String HOST = "host";

    public static final String PORT = "port";

    public static final String GROUP = "group";

    public static final String WEIGHT = "weight";

    public static final String SERVICE_URL = "serviceUrl";

    public static final String SERVICE_TOKEN  ="serviceToken";

    public static final String LIMIT = "limit";

    public static final String ASYNC =  "async";

    public static final String TIME_OUT = "timeOut";

    public static final String RETRY = "retry";

    public static final String TOLERANT = "tolerant";

    public static final Integer DEFAULT_TIMEOUT = 3000;

    public static final Integer DEFAULT_THREAD_NUMS = 256;

    public static final Integer DEFAULT_QUEUE_SIZE = 512;

    public static final Integer DEFAULT_MAX_CONNECTION_NUMS = DEFAULT_THREAD_NUMS + DEFAULT_QUEUE_SIZE;

    /**
     * 默认解码指定分隔符
     */
    public static final String DEFAULT_DECODE_CHAR = "$_i0#Xsop1_$";

    public static final int SERVER_DEFAULT_MSG_LENGTH = 1024 * 10;

    /**
     * 客户端最大响应数据体积
     */
    public static final int CLIENT_DEFAULT_MSG_LENGTH = 1024 * 10;

}
