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

这块的具体设计思路其实比较简单，通过当前Class的类加载器去加载META-INF/irpc/目录底下存在的资源文件，并且将它们放入一个LinkedHashMap中。核心代码 如下：

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
