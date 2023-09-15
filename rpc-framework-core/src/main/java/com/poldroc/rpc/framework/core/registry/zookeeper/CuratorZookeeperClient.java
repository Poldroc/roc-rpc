package com.poldroc.rpc.framework.core.registry.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import com.poldroc.rpc.framework.core.registry.zookeeper.AbstractZookeeperClient;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

/**
 * zookeeper客户端实现类
 * @author Poldroc
 * @date 2023/9/15
 */

public class CuratorZookeeperClient extends AbstractZookeeperClient {


    /**
     *  用于与 ZooKeeper 进行交互
     */
    private CuratorFramework client;

    public CuratorZookeeperClient(String zkAddress) {
        this(zkAddress, null, null);
    }


    public CuratorZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        super(zkAddress, baseSleepTimes, maxRetryTimes);
        // 使用指数补偿重试策略，基于 baseSleepTimes 和 maxRetryTimes 属性来设置重试策略的参数
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(super.getBaseSleepTimes(), super.getMaxRetryTimes());
        if (client == null) {
            client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
            client.start();
        }
    }

    @Override
    public Object getClient() {
        return client;
    }

    /**
     * 更新指定节点 address 的数据
     * @param address
     * @param data
     */
    @Override
    public void updateNodeData(String address, String data) {
        try {
            // 将给定的数据 data 转换为字节数组，并将其设置为指定节点的数据
            client.setData().forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定节点 address 的数据
     * @param address
     * @return
     */
    @Override
    public String getNodeData(String address) {
        try {
            // 获取节点的数据并将其作为字符串返回
            byte[] result = client.getData().forPath(address);
            if (result != null) {
                return new String(result);
            }
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

/**
     * 获取指定节点 path 的子节点列表
     * @param path
     * @return
     */
    @Override
    public List<String> getChildrenData(String path) {
        try {
            List<String> childrenData = client.getChildren().forPath(path);
            return childrenData;
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建持久化类型节点数据信息
     * @param address 节点地址
     * @param data 节点数据
     */
    @Override
    public void createPersistentData(String address, String data) {
        try {
            // creatingParentContainersIfNeeded()创建一个持久节点，如果指定的父节点不存在，则会先创建父节点
            // withMode(CreateMode.PERSISTENT): 这里指定了节点的创建模式
            // .forPath(address, data.getBytes()): 最后，使用 .forPath() 方法指定要创建的节点的路径 address 和节点的数据。
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建持久化且有序的节点数据信息
     * @param address 节点地址
     * @param data 节点数据
     */
    @Override
    public void createPersistentWithSeqData(String address, String data) {
        try {
            // 指定节点的创建模式为持久有序节点
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建临时有序节点数据信息
     * 临时节点在客户端会话结束后自动删除
     * @param address 节点地址
     * @param data 节点数据
     */
    @Override
    public void createTemporarySeqData(String address, String data) {
        try {
            // 指定节点的创建模式为临时有序节点
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建临时节点数据信息
     * @param address 节点地址
     * @param data 节点数据
     */
    @Override
    public void createTemporaryData(String address, String data) {
        try {
            // 指定节点的创建模式为临时节点
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(address, data.getBytes());
            // 父级节点是临时节点的情况下的处理
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            // zooKeeper 不允许在临时节点下创建子节点
            try {
                // 更新临时节点数据的部分。
                // 如果父级节点是临时节点，且不能创建子节点，那么这里使用 setData() 方法更新现有节点的数据。
                // 如果更新成功，那么现有的临时节点数据将被替换为新的数据。
                client.setData().forPath(address, data.getBytes());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * 设置某个节点的数值
     * @param address 节点地址
     * @param data 节点数据
     */
    @Override
    public void setTemporaryData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * 用于关闭 CuratorFramework 客户端
     */
    @Override
    public void destroy() {
        client.close();
    }

    /**
     * 列出指定节点 address 的子节点
     * @param address 节点地址
     * @return
     */
    @Override
    public List<String> listNode(String address) {
        try {
            return client.getChildren().forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 删除指定节点 address
     * @param address 节点地址
     * @return
     */
    @Override
    public boolean deleteNode(String address) {
        try {
            client.delete().forPath(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断指定节点 address 是否存在
     * @param address 节点地址
     * @return
     */
    @Override
    public boolean existNode(String address) {
        try {
            Stat stat = client.checkExists().forPath(address);
            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 监听指定节点 address 的数据变化
     * @param address 节点地址
     * @param watcher 监听器
     */
    @Override
    public void watchNodeData(String address, Watcher watcher) {
        try {
            // 当节点的数据发生更改时，监视器将触发。
            client.getData().usingWatcher(watcher).forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听指定节点 address 的子节点数据变化
     * @param address 节点地址
     * @param watcher 监听器
     */
    @Override
    public void watchChildNodeData(String address, Watcher watcher) {
        try {
            // 当子节点发生变化时，监视器将触发
            client.getChildren().usingWatcher(watcher).forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
