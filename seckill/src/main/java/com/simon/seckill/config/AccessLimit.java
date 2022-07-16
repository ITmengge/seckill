package com.simon.seckill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解,使用计数器简单实现
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Target(ElementType.METHOD)     // 是一个方法
public @interface AccessLimit {

    int second();

    int maxCount();

    boolean needLogin() default true;
}
