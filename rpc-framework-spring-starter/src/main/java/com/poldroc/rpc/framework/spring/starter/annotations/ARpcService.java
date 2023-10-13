package com.poldroc.rpc.framework.spring.starter.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;
/**
 *
 * @author Poldroc
 * @date 2023/10/7
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ARpcService {
    int limit() default 10;

    String group() default "default";

    String serviceToken() default "";

}
