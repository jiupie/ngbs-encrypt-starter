package com.wl.core.encrypt.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 验证签名注解
 * 前端请求传输过来，请求头里面有sign和timestamp字段，
 * 把 post请求体中的字段和timestamp字段进行排序然后md5
 * sign为请求参数签名后md5转16进制值
 * 只拦截post请求,使用AOP实现
 *
 * @author 南顾北衫
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SignEncrypt {

    long timeout() default 60000L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

}
