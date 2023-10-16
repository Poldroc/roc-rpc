# RPC-Framework-By-Poldroc



## 代理层

- 基于Netty搭建了一套简单的服务端和客户端通信模型。
- 通过自定义协议体RpcProtocol的方式来解决网络粘包和拆包的问题。
- 封装了统一的代理接口，合理引入了JDK代理来实现网络传输的功能。
- 客户端通过队列消费的异步设计来实现消息发送，通过uuid来标示请求线程和响应线程之间的数据匹配问题。

![image-20230917012659415](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202309170126607.png)



## 注册中心

![image-20230917013114895](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202309170131966.png)





## 路由层

实现了**随机路由**和**轮询路由** 两大类





## 序列化层

对接市面上常见的序列化技术框架: Hessian2、Kryo、JDK、FastJson





## 当前框架设计回顾

目前我们Rpc框架的基本设计架构如下图所示，除了简单的客户端发送请求抵达服务端之外，还新增了以下几个角色：

- 代理层 （根据配置生成不同的动态代理类）；
- 路由层 （根据配置选用不同的负载均衡方法）；
- 注册中心层（根据配置接入多种注册中心，通过引入第三者来实现“协调”的效果）；
- 序列化层（根据配置采用不同的序列化框架，传输协议的统一）。



![image-20230923195432406](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202309231954645.png)

暂时来看，请求的基本功能算是比较完善了，但是在实际的开发使用过程中可能还会存在以下问题：

- 对client的请求做鉴权；
- 分组管理服务；
- 如何实现基于ip直连的方式访问server端？
- 调用过程中需要记录下调用的相关日志信息。

> 对应解释如下：

`对client的请求做鉴权`

随着互联网业务的不断扩展，服务的种类开始变得越来越丰富，其中就有可能会出现一些比较注重安全方面的服务信息，例如查询账户信息服务、查询身份证信息服务、查询工资流水服务等等，这些服务都有一个共同点，就是**私密性强，调用时候需要做安全防范**。所以我尝试在Rpc框架内部加入对服务鉴权的操作，通过鉴权来提高一定的安全性。

具体的思路大概是：请求抵达服务端调用具体方法之前，先对其调用凭证进行判断操作，如果凭证不一致则抛出异常。

![image-20230926225014957](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202309262250192.png)



`分组管理服务`



![image-20230926235605079](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202309262356262.png)



`如何实现基于ip直连的方式访问server端`

按照指定ip访问的方式请求server端是我们在测试阶段会比较常见的方式，例如服务部署之后，发现2个名字相同的服务，面对相同的请求参数，在两个服务节点中返回的结果却不一样，此时就可以通过指定请求ip来进行debug诊断。

![image-20230926235956771](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202309262359955.png)

`用过程中需要记录下调用的相关日志信息`

每次请求都最好能有一次请求调用的记录，方便开发者调试。日志的内容一般会关注以下几个点：调用方信息，请求的具体服务的哪个方法，请求时间。





**客户端的责任链插入位置**

```java
com.poldroc.rpc.framework.core.client.ConnectionHandler#getChannelFuture
```

选择在这里插入的原因是，客户端在获取到目标方的channel集合之后需要进行筛选过滤，最终才会发起真正的请求。

**服务端的责任链插入位置**

```java
com.poldroc.rpc.framework.core.server.ServerHandler#channelRead
```

在 ChannelInboundHandlerAdapter 内部加入过滤链说明此事请求数据已经落入到了server端的业务线程池中，接下来需要通过责任链的每一个环节进行校对，最终确认是否可以执行目标函数。





## SPI(Service Provider Interface)

> 是一种通过外界配置来加载具体代码内容的技术手段

引入SPI技术的目的是希望可以通过配置化的方式来引入自定义插件部分，例如自定义负载均衡策略、自定义序列化算法技术、自定义代理工厂等等。通常常见的实现思路是：在统一规定的文件目录底下，新建一份文件，并在该文件内部定义好需要加载的类，让核心程序在不做内部源代码修改的条件下可以引入执行的代码逻辑。业界常见的SPI机制大体分为了两种类型：

- JDK内部自带的SPI机制；
- 自定义实现的SPI机制。

### 自己实现SPI机制

这块的具体设计思路其实比较简单，通过当前Class的类加载器去加载META-INF/rpc/目录底下以类路径命名的资源文件，并且将它们放入一个LinkedHashMap中。核心代码 如下：

```java
package com.poldroc.rpc.framework.core.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义SPI扩展加载器
 *
 * @author Poldroc
 * @date 2023/9/28
 */
public class ExtensionLoader {
    /**
     * 扩展加载器目录前缀
     */
    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    /**
     * 扩展加载器缓存
     * key: 扩展加载器接口
     * value: 扩展加载器实例
     */
    public static Map<String, LinkedHashMap<String, Class>> EXTENSION_LOADER_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * 加载指定接口的扩展加载器
     *
     * @param clazz 扩展加载器接口
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        // 参数校验
        if (clazz == null) {
            throw new IllegalArgumentException("class is null!");
        }

        // 获取扩展加载器配置文件路径
        String spiFilePath = EXTENSION_LOADER_DIR_PREFIX + clazz.getName();

        // 获取类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();

        // 获取所有匹配的扩展文件的URL枚举
        Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);

        // 遍历所有匹配的扩展文件
        while (enumeration.hasMoreElements()) {
            // 获取下一个扩展文件的URL
            URL url = enumeration.nextElement();

            // 获取扩展文件的输入流
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());

            // 获取扩展文件的缓冲读取器
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // 读取扩展文件的每一行
            String line;
            // 存储扩展类信息
            LinkedHashMap<String, Class> classMap = new LinkedHashMap<>();

            // 遍历扩展文件的每一行
            while ((line = bufferedReader.readLine()) != null) {
                // 如果配置中加入了#开头则表示忽略该类无需进行加载
                if (line.startsWith("#")) {
                    continue;
                }
                // 按照=分割扩展类信息
                String[] lineArr = line.split("=");
                // 获取扩展类名
                String implClassName = lineArr[0];
                // 获取扩展接口名
                String interfaceName = lineArr[1];
                // 将扩展类信息存入classMap
                classMap.put(implClassName, Class.forName(interfaceName));
            }

            // 只会触发class文件的加载，而不会触发对象的实例化
            if (EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())) {
                // 支持开发者自定义配置，将新加载的信息合并到已有缓存中
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            } else {
                // 将新加载的信息存入缓存
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }
    }

}
```



至此，整套框架的大致模型如下：

![image-20231002094906517](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202310020949679.png)

例如当客户端发起一个dataService.sendData方法的时候，实际上会通过一个代理对象帮其将参数封装好，然后经过一系列的过滤链二次包装，再通过路由层计算好实际应该发送的目标机器，最后通过序列化层将其转换为字节数组，通过netty底层将其从网络通道发送到目标服务节点上。



## 高并发

- 如何使用阻塞队列对高并发请求的一个削弱
- 业务线程池的引入保证请求的处理吞吐能力
- 异步调用的简单实现

### 服务端优化

#### 使用堵塞队列提升吞吐性能

* **单独设计一条独立的队列用于接收请求**

单独使用一条堵塞队列用于接收请求，然后在队尾由业务线程池来负责消费请求数据。这样即使请求出现了堆积，也是堆积在一条我们比较能轻易操作的队列当中。



首先定义一个请求分发器ServerChannelDispatcher，这个分发器的内部存有一条阻塞队列 **RPC_DATA_QUEUE。** 另外分发器的内部还有一个线程对象ServerJobCoreHandle专门负责将队列的数据读出，然后提及到业务线程池去执行。

> 核心代码

```java
package com.poldroc.rpc.framework.core.dispatcher;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.RpcProtocol;
import com.poldroc.rpc.framework.core.server.ServerChannelReadData;

import java.lang.reflect.Method;
import java.util.concurrent.*;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.*;
/**
 * 服务器通道分发器
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
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                // 反序列化
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                // 执行过滤链路
                                SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
                                // 执行目标方法
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                // 遍历所有方法，找到目标方法，找到与客户端请求的目标方法名匹配的方法
                                for (Method method : methods) {
                                    if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                        // 如果目标方法的返回值为void，则直接调用目标方法
                                        if (method.getReturnType().equals(Void.TYPE)) {
                                            // 动态调用方法
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        } else {
                                            // 如果目标方法的返回值不为void，则调用目标方法，并将返回值赋值给result
                                            result = method.invoke(aimObject, rpcInvocation.getArgs());
                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                // 将结果序列化
                                RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                // 将结果返回给客户端
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
```



### 客户端优化

例如当我们遇到一些只需要触发接口调用，但是对于接口返回内容并不关心的这类函数，就没有必要再在代码中监听对方的消息返回行为了，此时可以采用**异步发送的策略**进行实现。



在底层的代理类com.poldroc.rpc.framework.core.proxy.jdk.JDKClientInvocationHandler内部实现部分加入了一个if判断，如果发送请求的部分存在async相关配置，则不会进入循环监听的逻辑代码部分，具体：

```java
package com.poldroc.rpc.framework.core.proxy.jdk;

import com.poldroc.rpc.framework.core.client.RpcReferenceWrapper;
import com.poldroc.rpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.DEFAULT_TIMEOUT;
import static com.poldroc.rpc.framework.core.common.constants.RpcConstants.TIME_OUT;

/**
 * 各代理工厂都统一使用
 * 核心任务就是将需要调用的方法名称、服务名称，参数统统都封装好到RpcInvocation当中，然后塞入到一个队列里，并且等待服务端的数据返回
 * @author Poldroc
 * @date 2023/9/15
 */

public class JDKClientInvocationHandler implements InvocationHandler {

    /**
     * 用于锁定当前对象
     */
    private final static Object OBJECT = new Object();

    private RpcReferenceWrapper rpcReferenceWrapper;

    private int timeOut = DEFAULT_TIMEOUT;

    public JDKClientInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
        timeOut = Integer.valueOf(String.valueOf(rpcReferenceWrapper.getAttatchments().get(TIME_OUT)));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setTargetMethod(method.getName());
        // 注入uuid，用于标识请求
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttatchments());
        // 将请求信息放入发送队列
        SEND_QUEUE.add(rpcInvocation);
        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }
        // 如果不是异步调用，客户端会等待服务端的响应，同时检查是否超时
        long beginTime = System.currentTimeMillis();
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        while (System.currentTimeMillis() - beginTime < timeOut ) {
            // 从响应结果集中获取响应结果
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                // 如果是RpcInvocation类型，说明是服务端返回的响应结果，直接返回
                return ((RpcInvocation)object).getResponse();
            }
        }
        // 如果超时，抛出异常
        throw new TimeoutException("client wait server's response timeout!");
    }
}

```



## 容错层

- **服务端异常返回给到调用方展示**
- **客户端调用可以支持超时重试** 
- **服务提供方进行接口限流** 



#### 服务端异常正常返回

设计思路是：**将服务端的异常信息统一采集起来，返回给到调用方并且将堆栈记录打印。**

![image-20231005195135855](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202310051951055.png)







在客户端调用服务端的时候，数据都会被封装在了一个叫做

com.poldroc.rpc.framework.core.common.RpcInvocation的代码对象中，该对象包含了请求的目标方法，请求参数，正常响应内容等字段，现在我计划给它新增一个异常信息字段Throwable e：

```java
/**
 * RPC自定义协议请求的封装类
 * @author Poldroc
 * @date 2023/9/12
 */
@Data
@ToString
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

        /**
         * 附加属性
         */
        private Map<String,Object> attachments = new HashMap<>();

        /**
         * 主要用于记录服务端抛出的异常信息
         */
        private Throwable e;
        
}

```



e字段用于存储服务端抛出的异常信息，而相关的异常信息则是在服务端的com.poldroc.rpc.framework.core.dispatcher.ServerChannelDispatcher任务中进行捕获。

捕获原理：在服务端获取到目标函数和传入参数之后，需要通过反射来执行相关调用，可以在外加一层try catch去捕获该部分的异常信息：



#### 超时重试机制

关于接口超时重试这类机制，其实建议在实际使用的时候再三斟酌下，**并不是所有的接口在超时的时候都需要进行重试，面对一些非幂等性的接口调用情况，重试机制就应该谨慎使用**。下边我们来深入分析下，什么样的场景适合使用重试机制。

- 目标集群中有A，B服务器，A服务器性能不佳，处理请求比较缓慢，B服务器性能优于A，所以当接口调用A出现超时之后，可以尝试重新发起调用，将请求转到B上从而获取数据结果。
- 网络因为某些特殊异常，导致突然间断，此时可以通过重试机制发起二次调用，这时候重试机制就对接口的整体可用性有了一定的保障。

听了上边的这些场景介绍，我们似乎会发现重试机制的存在还是有一定好处的，那么接下来让我们来思考下重试机制使用不当可能会导致什么情况发生：

- 对于一些对数据重复性较为敏感的接口，例如转账，下单，以及一些和金融相关的接口，当接口调用出现超时之后，并不好确认数据包是否已经抵达到目标服务，所以这类场景下对接口设置超时重试功能需要有所斟酌。

综合上述的这些因素，我在设计思路为：**如果出现超时异常，默认可以发起1次重试机会，如果不想使用重试功能，可以在配置中将对应方法的重试次数设置为0。** 例如下边的案例代码：

```java
public static void main(String[] args) throws Throwable {
    Client client = new Client();
    RpcReference rpcReference = client.initClientApplication();
    RpcReferenceWrapper<DataService> rpcReferenceWrapper = new RpcReferenceWrapper<>();
    rpcReferenceWrapper.setAimClass(DataService.class);
    rpcReferenceWrapper.setGroup("dev");
    rpcReferenceWrapper.setServiceToken("token-a");
    rpcReferenceWrapper.setTimeOut(3000);
    //超时重试次数
    rpcReferenceWrapper.setRetry(0);
    rpcReferenceWrapper.setAsync(false);
    DataService dataService = rpcReference.get(rpcReferenceWrapper);
    //订阅服务
    client.doSubscribeService(DataService.class);

    ConnectionHandler.setBootstrap(client.getBootstrap());
    client.doConnectServer();
    client.startClient();
    String result = dataService.testErrorV2();
    System.out.println("结束调用");
    System.out.println(result);
}

```

大致的一个逻辑处理流程图如下图所示：

![image-20231005231735391](https://gitee.com/poldroc/typora-drawing-bed01/raw/master/imgs/202310052317574.png)

**重试策略**：立即重试

调用失败后立即发送二次重试，并且会把超时的请求路由到其他机器上，而不是本机尝试。





#### 服务端保护机制

- 控制业务应用整体的连接上限；
- 单个服务请求的限流。



##### 对单个应用连接进行控制

采用RPC服务的集群设计中，通常都是服务的消费方要比提供方更多，服务提供者有可能会同时和上百个服务调用方建立连接，所以当服务提供方的负载压力达到一定阈值的条件下就应该减少外界新访问的连接。

所以我们现在需要在原有的代码基础上加上以下实现：**对服务端的要有一个统一的连接数控制，比如最大连接限制为512，当前连接数超过512则超出的部分直接拒绝。**

首先需要定义一个限制最大连接数的Handler类：

```java
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

```

细节注意：

* 防止高并发请求下，突然大量请求抵达服务端，但是却被告知断开链接，此时为了防止打印重复的日志，可以采用定时记录的设计思想去实现。



##### 单个服务请求的限流

首先来解释下这个概念，例如UserProvider这个服务提供者，内部有多个方法对外暴露给调用方远程执行，整体的服务调用规律如下表所示：

| **服务名称** | **方法名称**        | **限制并发调用次数** | **日常并发请求量** | **备注** |
| ------------ | ------------------- | -------------------- | ------------------ | -------- |
| UserProvider | UserQueryService    | 100                  | 50                 | 写DB     |
| UserProvider | UserUpdateService   | 40                   | 20                 | 写DB     |
| UserProvider | UserRegistryService | 5                    | 2                  | 写DB     |

由于业务场景中偶尔会有一些大流量的线上活动，这种规模会对现有的访问流量造成突发增加，如果不做相关的防御手段容易直接将流量压力打入到整个数据库层面，从而引发更加严重的系统危害问题。

所以限流的策略更加细粒度化是我们实现保护效果的关键思路。



**限流部分的主要核心思想是采用了Semaphore的组件进行实践。**

`Semaphore` 是 Java JDK 中提供的一种同步工具，用于控制多线程并发访问共享资源。它是一种信号量机制，可以帮助防止竞态条件，并协调多线程之间对关键代码段的访问。`Semaphore` 是 Java.util.concurrent 包中的一部分，从 Java 5 开始引入。

`Semaphore` 主要用于两种情况：

1. **Binary Semaphore（二进制信号量）**：这种类型的信号量只能有两个状态，通常用 0 和 1 表示。它通常被称为互斥锁（Mutex），用于实现互斥访问，即同一时刻只允许一个线程访问共享资源。
   - `acquire` 操作：如果信号量的值大于 0，将其减 1，否则阻塞当前线程，直到信号量变为非零。
   - `release` 操作：增加信号量的值 by 1。
2. **Counting Semaphore（计数信号量）**：这种类型的信号量可以有一个非负整数值，用于控制对有限数量资源的访问。它允许多个线程同时访问资源，但有一个上限值。
   - `acquire` 操作：如果信号量的值大于 0，将其减 1，否则阻塞当前线程，直到信号量变为非零。
   - `release` 操作：增加信号量的值 by 1。

它提供了acquire和tryAcquire两种方法供开发者调用，在Sem aphore的内部其实是有一个计数器，每次向它申请许可的时候如果计数器不为0，则申请通过，如果计数器为0则会处于堵塞（acquire），或者立马断开（tryAcquire），又或者等待一定时间后才断开（tryAcquire可以指定等待时间）。当资源使用完毕之后需要执行release操作，将计数器归还。



使用tryAcquire则是一种“快速响应”的解决思路，当获取申请失败后，不会堵塞当前线程，而是立马通知客户端调用异常，然后发起二次重试，路由到其他节点。**至少这种策略相比于acquire来说不存在请求堆积，导致服务崩溃的风险因素。**



限流部分的代码实现：

划分为了**前置过滤器**和**后置过滤器。**

- 前置过滤器：

请求数据在执行实际业务函数之前需要会经过**前置过滤器**的逻辑，而限流组件则是在前置过滤器的最后一环，主要负责tryAcquire环节。

```java
package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.ServerServiceSemaphoreWrapper;
import com.poldroc.rpc.framework.core.common.annotations.SPI;
import com.poldroc.rpc.framework.core.common.exceptiom.MaxServiceLimitRequestException;
import com.poldroc.rpc.framework.core.filter.ServerFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;
/**
 * 请求数据在执行实际业务函数之前需要会经过前置过滤器的逻辑，
 * 而限流组件则是在前置过滤器的最后一环，主要负责tryAcquire环节
 * @author Poldroc
 * @date 2023/10/7
 */

@SPI("before")
@Slf4j
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        // 从缓存中提取semaphore对象
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        // 尝试获取信号量
        boolean tryResult = semaphore.tryAcquire();
        // 如果获取失败，说明当前服务已经达到最大并发数，直接抛出异常
        if (!tryResult) {
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException rpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(rpcException);
            throw rpcException;
        }
    }
}

```



- 后置过滤器

当业务核心逻辑执行完毕之后，会进入到**后置过滤器**中，这里面可以执行relase操作。

```java
package com.poldroc.rpc.framework.core.filter.server;

import com.poldroc.rpc.framework.core.common.RpcInvocation;
import com.poldroc.rpc.framework.core.common.ServerServiceSemaphoreWrapper;
import com.poldroc.rpc.framework.core.common.annotations.SPI;
import com.poldroc.rpc.framework.core.filter.ServerFilter;

import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;
/**
 * 当业务核心逻辑执行完毕之后，会进入到后置过滤器中，这里面可以执行relase操作
 * @author Poldroc
 * @date 2023/10/7
 */

@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {

    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }
}

```



## 接入层

SpringBoot的使用率更广泛，接入难度也比较低，所以下边会采用以SpringBoot自动装配的思路去设计这个接入层的代码

**接入思路**

开发对应的自动装配类，并且通过引入spi文件去让Spring扫描到该装配类即可。

提供了starter的设计思路，遵循了“约定大于配置”的这种理念，只需要给对应的中间件编写好一个自动配置类以及一份spi文件，最后交给SpringBoot去扫描即可，整体难度会比较低。



