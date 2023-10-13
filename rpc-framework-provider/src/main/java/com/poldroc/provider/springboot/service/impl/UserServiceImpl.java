package com.poldroc.provider.springboot.service.impl;


import com.poldroc.rpc.framework.interfaces.UserService;
import com.poldroc.rpc.framework.spring.starter.annotations.ARpcService;

/**
 *
 * @author Poldroc
 * @date 2023/10/13
 */

@ARpcService
public class UserServiceImpl implements UserService {

    public void test() {
        System.out.println("test");
    }
}
