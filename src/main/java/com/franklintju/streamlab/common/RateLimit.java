package com.franklintju.streamlab.common;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String key() default "";

    int maxRequests() default 10;

    int windowSeconds() default 60;

    String message() default "请求过于频繁，请稍后重试";
}
