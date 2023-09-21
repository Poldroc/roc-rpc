package com.poldroc.rpc.framework.core.registry.zookeeper;

import lombok.Data;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * zk客户端抽象类
 * @author Poldroc
 * @date 2023/9/15
 */
@Data
public abstract class AbstractZookeeperClient {

    /**
     * zk地址
     */
    private String zkAddress;

    /**
     * zk超时时间
     */
    private int baseSleepTimes;

    /**
     * zk重试次数
     */
    private int maxRetryTimes;

    public AbstractZookeeperClient(String zkAddress) {
        this.zkAddress = zkAddress;
        // 默认3000ms
        this.baseSleepTimes = 1000;
        this.maxRetryTimes = 3;
    }

    public AbstractZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        this.zkAddress = zkAddress;
        this.baseSleepTimes = (baseSleepTimes == null) ? 1000 : baseSleepTimes;
        this.maxRetryTimes = (maxRetryTimes == null) ? 3 : maxRetryTimes;
    }

    /**
     * 更新节点
     * @param address
     * @param data
     */
    public abstract void updateNodeData(String address, String data);


    public abstract Object getClient();

    /**
     * 拉取节点的数据
     *
     * @param path
     * @return
     */
    public abstract String getNodeData(String path);

    /**
     * 获取指定目录下的字节点数据
     *
     * @param path
     * @return
     */
    public abstract List<String> getChildrenData(String path);

    /**
     * 创建持久化类型节点数据信息
     *
     * @param address
     * @param data
     */
    public abstract void createPersistentData(String address, String data);

    /**
     * 创建有序且持久化类型节点数据信息
     *
     * @param address
     * @param data
     */
    public abstract void createPersistentWithSeqData(String address, String data);


    /**
     * 创建有序且临时类型节点数据信息
     *
     * @param address
     * @param data
     */
    public abstract void createTemporarySeqData(String address, String data);


    /**
     * 创建临时节点数据类型信息
     *
     * @param address
     * @param data
     */
    public abstract void createTemporaryData(String address, String data);

    /**
     * 设置某个节点的数值
     *
     * @param address
     * @param data
     */
    public abstract void setTemporaryData(String address, String data);

    /**
     * 断开zk的客户端链接
     */
    public abstract void destroy();


    /**
     * 展示节点下边的数据
     *
     * @param address
     */
    public abstract List<String> listNode(String address);


    /**
     * 删除节点下边的数据
     *
     * @param address
     * @return
     */
    public abstract boolean deleteNode(String address);


    /**
     * 判断是否存在其他节点
     *
     * @param address
     * @return
     */
    public abstract boolean existNode(String address);


    /**
     * 监听path路径下某个节点的数据变化
     *
     * @param address
     */
    public abstract void watchNodeData(String address, Watcher watcher);

    /**
     * 监听子节点下的数据变化
     *
     * @param address
     * @param watcher
     */
    public abstract void watchChildNodeData(String address, Watcher watcher);

}
