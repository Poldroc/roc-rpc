package com.poldroc.rpc.framework.core.server;

import com.poldroc.rpc.framework.interfaces.UserService;
/**
 *
 * @author Poldroc
 * @date 2023/9/22
 */

public class UserServiceImpl implements UserService {

    @Override
    public void test() {
        System.out.println("test");
    }
}
