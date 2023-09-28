package com.poldroc.rpc.framework.core.router;

import com.poldroc.rpc.framework.core.common.ChannelFutureWrapper;
import com.poldroc.rpc.framework.core.registry.ServiceUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.poldroc.rpc.framework.core.common.cache.CommonClientCache.*;

/**
 * 随机路由器
 * @author Poldroc
 * @date 2023/9/20
 */

public class RandomRouterImpl implements Router {

    /**
     * 刷新路由表
     * 在客户端和服务提供者进行连接建立的环节触发
     * @param selector 选择器
     */
    @Override
    public void refreshRouterArr(Selector selector) {
        // 获取服务提供者的数目
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        // 提前生成调用先后顺序的随机数组
        int[] result = createRandomIndex(arr.length);
        // 生成对应服务集群的每台机器的调用顺序
        for (int i = 0; i < result.length; i++) {
            arr[i] = channelFutureWrappers.get(result[i]);
        }
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);
        ServiceUrl sUrl = new ServiceUrl();
        sUrl.setServiceName(selector.getProviderServiceName());
        // 更新权重
        ROUTER.updateWeight(sUrl);
    }

    /**
     * 选择给定 Selector 对象的服务提供者
     * 随机路由层内部对外暴露的核心方法，
     * 每次外界调用服务的时候都是通过这个函数去获取下一次调用的provider信息
     * @param selector 选择器
     * @return
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getChannelFutureWrappers());
    }

    /**
     * 更新特定服务的服务提供者权重
     * @param sUrl 服务地址
     */
    @Override
    public void updateWeight(ServiceUrl sUrl) {
        // 服务节点的权重
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(sUrl.getServiceName());
        // 根据每个服务提供者的权重计算一个权重数组
        Integer[] weightArr = createWeightArr(channelFutureWrappers);
        // 根据权重数组生成一个随机数组
        Integer[] randomArr = createRandomArr(weightArr);
        // 根据随机数组生成一个调用顺序数组
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[randomArr.length];

        for (int i = 0; i < randomArr.length; i++) {
            arr[i] = channelFutureWrappers.get(randomArr[i]);
        }
        // 更新路由器的映射，使用新的有序数组更新该服务
        SERVICE_ROUTER_MAP.put(sUrl.getServiceName(), arr);

    }

    /**
     * 根据服务提供者的权重创建一个权重数组
     *
     * @param channelFutureWrappers
     * @return
     */
    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrappers) {
        List<Integer> weightArr = new ArrayList<>();
        for (int k = 0; k < channelFutureWrappers.size(); k++) {
            Integer weight = channelFutureWrappers.get(k).getWeight();
            // c 是表示这个服务提供者应该在生成的数组中出现的次数
            int c = weight / 100;
            for (int i = 0; i < c; i++) {
                // 将当前服务提供者的索引（即 k 值）添加到 weightArr 列表中 c 次
                weightArr.add(k);
                // 这样，权重越大的服务提供者，在数组中就会出现更多次，从而按照权重比例分布
            }
        }
        Integer[] arr = new Integer[weightArr.size()];
        return weightArr.toArray(arr);
    }

    /**
     * 创建随机乱序数组
     *
     * @param arr
     * @return
     */
    private static Integer[] createRandomArr(Integer[] arr) {
        int total = arr.length;
        Random ra = new Random();
        for (int i = 0; i < total; i++) {
            int j = ra.nextInt(total);
            if (i == j) {
                continue;
            }
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }

    /**
     * 创建随机索引数组
     *
     * @param len
     * @return
     */
    private int[] createRandomIndex(int len) {
        int[] arrInt = new int[len];
        Random ra = new Random();
        for (int i = 0; i < arrInt.length; i++) {
            arrInt[i] = -1;
        }
        int index = 0;
        while (index < arrInt.length) {
            int num = ra.nextInt(len);
            // 如果数组中不包含这个元素则赋值给数组
            if (!contains(arrInt, num)) {
                arrInt[index++] = num;
            }
        }
        return arrInt;
    }


    /**
     * 判断数组中是否包含某个元素
     *
     * @param arr
     * @param key
     * @return
     */
    public boolean contains(int[] arr, int key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == key) {
                return true;
            }
        }
        return false;
    }


    /**
     * 测试代码片段
     */
    public static void main(String[] args) {
        List<ChannelFutureWrapper> channelFutureWrappers = new ArrayList<>();
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 100));
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 200));
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 9300));
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 400));
        Integer[] r = createWeightArr(channelFutureWrappers);
        System.out.println(r);
    }
}
