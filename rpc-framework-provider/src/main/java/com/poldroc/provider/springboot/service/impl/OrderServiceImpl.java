package com.poldroc.provider.springboot.service.impl;

import com.poldroc.rpc.framework.interfaces.OrderService;
import com.poldroc.rpc.framework.spring.starter.annotations.ARpcService;


import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Poldroc
 * @date 2023/10/13
 */

@ARpcService(serviceToken = "order-token", group = "order-group", limit = 2)
public class OrderServiceImpl implements OrderService {

    @Override
    public List<String> getOrderNoList() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList("item1", "item2");
    }

    /**
     * 测试大数据量
     * @param i
     * @return
     */
    @Override
    public String testMaxData(int i) {
        StringBuffer stb = new StringBuffer();
        for (int j = 0; j < i; j++) {
            stb.append("1");
        }
        return stb.toString();
    }
}
