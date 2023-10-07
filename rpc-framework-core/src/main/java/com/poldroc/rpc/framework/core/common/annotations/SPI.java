package com.poldroc.rpc.framework.core.common.annotations;

import java.lang.annotation.*;

/**
 * SPI注解
 * @author Poldroc
 * @date 2023/10/5
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {

    String value() default "";
}
