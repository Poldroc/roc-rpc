package com.poldroc.rpc.framework.interfaces;

import java.util.List;

/**
 *
 * @author Poldroc
 * @date 2023/9/21
 */

public interface DataService {

    /**
     * 发送数据
     *
     * @param body
     */
    String sendData(String body);

    /**
     * 获取数据
     *
     * @return
     */
    List<String> getList();
}
