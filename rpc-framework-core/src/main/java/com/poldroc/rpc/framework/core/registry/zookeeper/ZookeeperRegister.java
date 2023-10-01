package com.poldroc.rpc.framework.core.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import com.poldroc.rpc.framework.core.common.event.RpcEvent;
import com.poldroc.rpc.framework.core.common.event.RpcListenerLoader;
import com.poldroc.rpc.framework.core.common.event.RpcNodeChangeEvent;
import com.poldroc.rpc.framework.core.common.event.RpcUpdateEvent;
import com.poldroc.rpc.framework.core.common.event.data.SUrlChangeWrapper;
import com.poldroc.rpc.framework.core.registry.RegistryService;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;
import static com.poldroc.rpc.framework.core.common.cache.CommonServerCache.SERVER_CONFIG;

/**
 * @author Poldroc
 * @date 2023/9/16
 */
@Slf4j
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    private AbstractZookeeperClient zkClient;


    /**
     * 表示zooKeeper中服务注册和发现的根路径的常量字符串
     */
    private String ROOT = "/rpc";

    public AbstractZookeeperClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(AbstractZookeeperClient zkClient) {
        this.zkClient = zkClient;
    }

    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    public ZookeeperRegister() {
        String registryAddr = CLIENT_CONFIG!= null ? CLIENT_CONFIG.getRegisterAddr() : SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
    }

    private String getProviderPath(ServiceUrl sUrl) {
        return ROOT + "/" + sUrl.getServiceName() + "/provider/" + sUrl.getParameters().get("host") + ":" + sUrl.getParameters().get("port");
    }

    private String getConsumerPath(ServiceUrl sUrl) {
        return ROOT + "/" + sUrl.getServiceName() + "/consumer/" + sUrl.getApplicationName() + ":" + sUrl.getParameters().get("host") + ":";
    }

    /**
     * 在zooKeeper中注册服务提供者
     *
     * @param sUrl 服务url
     */
    @Override
    public void register(ServiceUrl sUrl) {
        if (!this.zkClient.existNode(ROOT)) {
            // 首先检查根路径是否存在，如果不存在则创建它
            zkClient.createPersistentData(ROOT, "");
        }
        // 构建URL字符串并使用临时节点在zooKeeper中创建服务提供者的路径
        String urlStr = ServiceUrl.buildProviderUrlStr(sUrl);
        if (!zkClient.existNode(getProviderPath(sUrl))) {
            zkClient.createTemporaryData(getProviderPath(sUrl), urlStr);
        } else {
            zkClient.deleteNode(getProviderPath(sUrl));
            zkClient.createTemporaryData(getProviderPath(sUrl), urlStr);
        }
        super.register(sUrl);
    }

    @Override
    public void unRegister(ServiceUrl sUrl) {
        zkClient.deleteNode(getProviderPath(sUrl));
        super.unRegister(sUrl);
    }


    @Override
    public void subscribe(ServiceUrl sUrl) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = ServiceUrl.buildConsumerUrlStr(sUrl);
        if (!zkClient.existNode(getConsumerPath(sUrl))) {
            zkClient.createTemporarySeqData(getConsumerPath(sUrl), urlStr);
        } else {
            zkClient.deleteNode(getConsumerPath(sUrl));
            zkClient.createTemporarySeqData(getConsumerPath(sUrl), urlStr);
        }
        super.subscribe(sUrl);
    }


    /**
     * 订阅成功后执行的回调方法 它监听服务节点和节点数据的变化
     * @param sUrl
     */
    @Override
    public void doAfterSubscribe(ServiceUrl sUrl) {
        // 监听是否有新的服务注册
        String servicePath = sUrl.getParameters().get("servicePath");
        String newServerNodePath = ROOT + "/" + servicePath;
        watchChildNodeData(newServerNodePath);
        String providerIpStrJson = sUrl.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, List.class);
        for (String providerIp : providerIpList) {
            this.watchNodeDataChange(ROOT + "/" + servicePath + "/" + providerIp);
        }
    }

    @Override
    public void doBeforeSubscribe(ServiceUrl sUrl) {

    }

    @Override
    public List<String> getProviderIps(String serviceName) {
        return this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
    }

    @Override
    public void doUnSubscribe(ServiceUrl sUrl) {
        this.zkClient.deleteNode(getConsumerPath(sUrl));
        super.doUnSubscribe(sUrl);
    }


    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>();
        for (String ipAndHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipAndHost);
            result.put(ipAndHost, childData);
        }
        return result;
    }

    /**
     * 用于监视给定路径下子节点的更改
     * 当发生更改时，它处理事件，发送事件通知，并重新注册监视器以继续监视更改
     *
     * @param newServerNodePath
     */
    public void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                log.info("[watchChildNodeData ]" + watchedEvent);
                String path = watchedEvent.getPath();
                List<String> childrenDataList = zkClient.getChildrenData(path);
                SUrlChangeWrapper sUrlChangeWrapper = new SUrlChangeWrapper();
                sUrlChangeWrapper.setProviderUrl(childrenDataList);
                sUrlChangeWrapper.setServiceName(path.split("/")[2]);
                //自定义的一套事件监听组件
                RpcEvent rpcEvent = new RpcUpdateEvent(sUrlChangeWrapper);
                RpcListenerLoader.sendEvent(rpcEvent);
                //收到回调之后再注册一次监听，这样能保证一直都收到消息
                watchChildNodeData(path);
            }
        });
    }

    /**
     * 订阅服务节点内部的数据变化
     *
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                log.info("[watchNodeDataChange ]" + watchedEvent);
                // 获取触发节点数据变化的事件对象 watchedEvent，其中包含有关事件的信息，如事件类型、节点路径等
                String path = watchedEvent.getPath();
                String nodeData = zkClient.getNodeData(path);
                nodeData = nodeData.replace(";", "/");
                ProviderNodeInfo providerNodeInfo = ServiceUrl.buildURLFromUrlStr(nodeData);
                RpcEvent rpcEvent = new RpcNodeChangeEvent(providerNodeInfo);
                RpcListenerLoader.sendEvent(rpcEvent);
                watchNodeDataChange(newServerNodePath);
            }
        });
    }
}
