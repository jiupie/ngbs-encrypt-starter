package com.wl.core.encrypt.handler;

import com.wl.core.encrypt.annotation.SeparateEncrypt;
import com.wl.core.encrypt.handler.impl.SignEncryptHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.FilterConfig;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 初始化处理器
 * 遍历restController和controller注解，获取该类中包含@SeparateEncrypt注解的方法，把对应的
 *
 * @author 南顾北衫
 */

public class InitHandler {
    private static final Logger log = LoggerFactory.getLogger(SignEncryptHandlerImpl.class);

    public static void handler(FilterConfig filterConfig, Set<String> encryptCacheUri, AtomicBoolean isEncryptAnnotation) {
        WebApplicationContext servletContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        Map<String, Object> restControllers = new HashMap<>();
        Map<String, Object> controllers = new HashMap<>();
        //获取所有的@controller注解的类
        try {
            controllers = servletContext.getBeansWithAnnotation(Controller.class);
        } catch (BeanCreationException e) {
            log.info(e.getMessage());
        }
        //获取所有的@RestContronller类
        try {
            restControllers = servletContext.getBeansWithAnnotation(RestController.class);
        } catch (BeanCreationException e) {
            log.info(e.getMessage());
        }

        //在@RestContronller注解的类中，查找@SeparateEncrypt注解的方法
        if (restControllers.size() > 0) {
            List<Object> types = restControllers.values().stream().filter(v -> AnnotationUtils.findAnnotation(v.getClass(), SeparateEncrypt.class) != null).collect(Collectors.toList());
            List<Object> notTypes = restControllers.values().stream().filter(v -> AnnotationUtils.findAnnotation(v.getClass(), SeparateEncrypt.class) == null).collect(Collectors.toList());
            restControllerTypesHandler(types, encryptCacheUri);
            restControllerNotTypesHandler(notTypes, encryptCacheUri);
        }

        //在@Contronller注解的类中，查找@SeparateEncrypt注解的方法
        if (controllers.size() > 0) {
            List<Object> types = controllers.values().stream().filter(v -> AnnotationUtils.findAnnotation(v.getClass(), SeparateEncrypt.class) != null).collect(Collectors.toList());
            List<Object> notTypes = controllers.values().stream().filter(v -> AnnotationUtils.findAnnotation(v.getClass(), SeparateEncrypt.class) == null).collect(Collectors.toList());
            controllerTypesHandler(types, encryptCacheUri);
            controllerNotTypesHandler(notTypes, encryptCacheUri);
        }

        if (encryptCacheUri.size() > 0) {
            isEncryptAnnotation.set(true);
        }
    }

    private static void controllerNotTypesHandler(List<Object> types, Set<String> cacheUrl) {
        if (types.size() > 0) {
            types.forEach(t -> {
                Class<?> aClass = t.getClass();
                Method[] declaredMethods = aClass.getDeclaredMethods();
                String[] finalTypeUrl = typeUrl(aClass);
                List<Method> methods = Arrays.stream(declaredMethods).filter(d -> AnnotationUtils.findAnnotation(d, SeparateEncrypt.class) != null).collect(Collectors.toList());
                if (methods.size() == 0) {
                    return;
                }
                MethodHandler(methods, finalTypeUrl, cacheUrl);
            });
        }
    }

    private static void controllerTypesHandler(List<Object> types, Set<String> cacheUrl) {
        if (types.size() > 0) {
            types.forEach(t -> {
                Class<?> aClass = t.getClass();
                Method[] declaredMethods = aClass.getDeclaredMethods();
                String[] finalTypeUrl = typeUrl(aClass);
                if (declaredMethods.length == 0) {
                    return;
                }
                List<Method> methods = Arrays.asList(declaredMethods);
                MethodHandler(methods, finalTypeUrl, cacheUrl);
            });
        }
    }


    private static void restControllerNotTypesHandler(List<Object> types, Set<String> cacheUrl) {
        if (types.size() > 0) {
            types.forEach(t -> {
                Class<?> aClass = t.getClass();
                Method[] declaredMethods = aClass.getDeclaredMethods();
                String[] finalTypeUrl = typeUrl(aClass);
                List<Method> methods = Arrays.stream(declaredMethods).filter(d -> AnnotationUtils.findAnnotation(d, SeparateEncrypt.class) != null).collect(Collectors.toList());
                if (methods.size() == 0) {
                    return;
                }
                restMethodHandler(methods, finalTypeUrl, cacheUrl);
            });
        }
    }

    private static void restControllerTypesHandler(List<Object> types, Set<String> cacheUrl) {
        if (types.size() > 0) {
            types.forEach(t -> {
                Class<?> aClass = t.getClass();
                Method[] declaredMethods = aClass.getDeclaredMethods();
                String[] finalTypeUrl = typeUrl(aClass);
                if (declaredMethods.length == 0) {
                    return;
                }
                List<Method> methods = Arrays.asList(declaredMethods);
                restMethodHandler(methods, finalTypeUrl, cacheUrl);
            });
        }
    }

    /**
     * 获取requestMapping中的value值，也就是url地址
     * GetMapping等注解实际上使用的是requestMapping注解，所以这些注解都会被扫描出来
     *
     * @param aClass /
     * @return /
     */
    private static String[] typeUrl(Class<?> aClass) {
        String[] typeUrl = null;
        if (AnnotationUtils.findAnnotation(aClass, RequestMapping.class) != null) {
            typeUrl = AnnotationUtils.findAnnotation(aClass, RequestMapping.class).value();
        }
        return typeUrl;
    }

    private static void restMethodHandler(List<Method> methods, String[] finalTypeUrl, Set<String> cacheUrl) {
        methods.forEach(m -> {
            if (AnnotationUtils.findAnnotation(m, PostMapping.class) != null || (AnnotationUtils.findAnnotation(m, RequestMapping.class) != null && Arrays.stream(AnnotationUtils.findAnnotation(m, RequestMapping.class).method()).noneMatch(r -> r.equals(RequestMethod.GET)))) {
                urlHandler(m, finalTypeUrl, cacheUrl);
            }
        });
    }

    private static void MethodHandler(List<Method> methods, String[] finalTypeUrl, Set<String> cacheUrl) {
        methods.forEach(m -> {
            if ((AnnotationUtils.findAnnotation(m, PostMapping.class) != null && AnnotationUtils.findAnnotation(m, ResponseBody.class) != null) || (AnnotationUtils.findAnnotation(m, RequestMapping.class) != null && Arrays.stream(AnnotationUtils.findAnnotation(m, RequestMapping.class).method()).allMatch(r -> !r.equals(RequestMethod.GET)) && AnnotationUtils.findAnnotation(m, ResponseBody.class) != null)) {
                urlHandler(m, finalTypeUrl, cacheUrl);
            }
        });
    }

    private static void urlHandler(Method m, String[] finalTypeUrl, Set<String> cacheUrl) {
        String[] urls = null;
        if (AnnotationUtils.findAnnotation(m, PostMapping.class) != null) {
            urls = AnnotationUtils.findAnnotation(m, PostMapping.class).value();
        } else {
            RequestMapping requestMapping = AnnotationUtils.findAnnotation(m, RequestMapping.class);
            if (Arrays.stream(requestMapping.method()).anyMatch(s -> s == RequestMethod.POST)) {
                urls = requestMapping.value();
            }
        }
        if (urls != null) {
            Arrays.stream(urls).forEach(u -> {
                if (!u.startsWith("/")) {
                    u = "/".concat(u);
                }
                if (finalTypeUrl != null && finalTypeUrl.length > 0) {
                    String finalU = u;
                    Arrays.stream(finalTypeUrl).forEach(f -> {
                        if (!f.startsWith("/")) {
                            f = "/".concat(f);
                        }
                        String uri = f.concat(finalU).replaceAll("//+", "/");
                        if (uri.endsWith("/")) {
                            uri = uri.substring(0, uri.length() - 1);
                        }
                        cacheUrl.add(uri);
                    });
                } else {
                    String uri = u.replaceAll("//+", "/");
                    if (uri.endsWith("/")) {
                        uri = uri.substring(0, uri.length() - 1);
                    }
                    cacheUrl.add(uri);
                }
            });
        }
    }
}
