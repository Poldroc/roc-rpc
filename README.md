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
