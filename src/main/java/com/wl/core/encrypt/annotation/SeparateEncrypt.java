package com.wl.core.encrypt.annotation;

import java.lang.annotation.*;

/**
 * 独立注解
 * 用于部分类和方法加密使用
 * 返回接口数据进行加密,使用servlet过滤器实现
 * request需要json格式
 * post请求
 *
 * @author 南顾北衫
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SeparateEncrypt {
}
