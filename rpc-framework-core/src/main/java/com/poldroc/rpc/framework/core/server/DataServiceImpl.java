package com.poldroc.rpc.framework.core.server;

import com.poldroc.rpc.framework.interfaces.DataService;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Poldroc
 * @date 2023/9/22
 */

public class DataServiceImpl implements DataService {

    @Override
    public String sendData(String body) {
        System.out.println("这里是服务提供者，body is " + body);
        return "success";
    }

    @Override
    public List<String> getList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("idea1");
        arrayList.add("idea2");
        arrayList.add("idea3");
        return arrayList;
    }
}
