package com.poldroc.rpc.framework.spring.starter.annotations;

import java.lang.annotation.*;
/**
 *
 * @author Poldroc
 * @date 2023/10/7
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ARpcReference {
    String url() default "";

    String group() default "default";

    String serviceToken() default "";

    int timeOut() default 3000;

    int retry() default 1;

    boolean async() default false;

}
