package com.poldroc.rpc.framework.interfaces;

import java.util.List;

/**
 *
 * @author Poldroc
 * @date 2023/10/13
 */

public interface OrderService {

    List<String> getOrderNoList();

    String testMaxData(int i);
}
