package com.wl.core.encrypt.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.core.encrypt.annotation.SignEncrypt;
import com.wl.core.encrypt.exception.EncryptException;
import com.wl.core.encrypt.handler.SignEncryptHandler;
import com.wl.core.encrypt.wrapper.EncryptRequestWrapperFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 签名加密拦截器
 *
 * @author gaoyang
 */
public class SignEncryptInterceptor implements MethodInterceptor {

    private final String signSecret;
    private final SignEncryptHandler signEncryptHandler;

    public SignEncryptInterceptor(String signSecret, SignEncryptHandler signEncryptHandler) {
        this.signEncryptHandler = signEncryptHandler;
        this.signSecret = signSecret;
    }


    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object proceed = methodInvocation.proceed();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        if (!"post".equalsIgnoreCase(request.getMethod()) || !EncryptRequestWrapperFactory.contentIsJson(request.getContentType())) {
            return proceed;
        }
        SignEncrypt signEncryptAnnotation = methodInvocation.getMethod().getAnnotation(SignEncrypt.class);
        long timeout = signEncryptAnnotation.timeout();
        TimeUnit timeUnit = signEncryptAnnotation.timeUnit();
        if (request.getContentLength() < 1) {
            return proceed;
        }
        Object[] params = methodInvocation.getArguments();
        if (params.length == 0) {
            return proceed;
        }
        Object arg = null;
        //获取方法，此处可将signature强转为MethodSignature
        Method method = methodInvocation.getMethod();
        //参数注解，1维是参数，2维是注解
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Object param = params[i];
            Annotation[] paramAnn = annotations[i];
            //参数为空，直接下一个参数
            if (param == null || paramAnn.length == 0) {
                continue;
            }
            for (Annotation annotation : paramAnn) {
                //这里判断当前注解是否为RequestBody.class
                if (annotation.annotationType().equals(RequestBody.class)) {
                    arg = param;
                    break;
                }
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();

        Map<Object, Object> jsonMap = objectMapper.readValue(objectMapper.writeValueAsString(arg), Map.class);
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        if (!StringUtils.hasText(sign) || !StringUtils.hasText(timestamp)) {
            throw new EncryptException("sign or timestamp is null");
        }
        jsonMap.put("timestamp", timestamp);
        jsonMap.put("sign", sign);
        return signEncryptHandler.handle(proceed, timeout, timeUnit, signSecret, jsonMap);
    }
}
