package com.poldroc.consumer.springboot.controller;


import com.poldroc.rpc.framework.interfaces.OrderService;
import com.poldroc.rpc.framework.interfaces.UserService;
import com.poldroc.rpc.framework.spring.starter.annotations.ARpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @Author linhao
 * @Date created in 5:43 下午 2022/3/8
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @ARpcReference
    private UserService userService;

    /**
     * 验证各类参数配置是否异常
     */
    @ARpcReference(group = "order-group",serviceToken = "order-token")
    private OrderService orderService;

    @GetMapping(value = "/test")
    public void test(){
        userService.test();
    }


    @GetMapping(value = "/testMaxData")
    public String testMaxData(){
        String result = orderService.testMaxData(100);
        System.out.println(result.length());
        return result;
    }


    @GetMapping(value = "/get-order-no")
    public List<String> getOrderNo(){
        List<String> result =  orderService.getOrderNoList();
        System.out.println(result);
        return result;
    }


}
