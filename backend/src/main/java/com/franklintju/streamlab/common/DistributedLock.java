package com.franklintju.streamlab.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    String key();

    int expireSeconds() default 10;

    String message() default "系统繁忙，请稍后重试";
}
