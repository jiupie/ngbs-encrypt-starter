package com.wl.core.encrypt.config;

import com.wl.core.encrypt.handler.EncryptHandler;
import com.wl.core.encrypt.handler.InitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 加密过滤器
 *
 * @author 南顾北衫
 */
public class EncryptFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(EncryptFilter.class);
    private EncryptHandler encryptHandler;

    private static AtomicBoolean isEncryptAnnotation = new AtomicBoolean(false);
    private final static Set<String> encryptCacheUri = new HashSet<>();

    public EncryptFilter(EncryptHandler encryptHandler) {
        this.encryptHandler = encryptHandler;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (isEncryptAnnotation.get()) {
            if (checkUri((HttpServletRequest) servletRequest)) {
                this.chain(servletRequest, servletResponse, filterChain);
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void chain(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        EncryptRequestWrapper request = new EncryptRequestWrapper((HttpServletRequest) servletRequest, encryptHandler);
        EncryptResponseWrapper response = new EncryptResponseWrapper((HttpServletResponse) servletResponse);
        filterChain.doFilter(request, response);
        byte[] responseData = response.getResponseData();
        if (responseData.length == 0) {
            return;
        }
        log.info("响应原报文：" + new String(responseData));
        byte[] encryptByte = encryptHandler.encode(URLEncoder.encode(new String(responseData), "UTF-8").getBytes());
        log.info("加密后响应报文：" + new String(encryptByte));
        servletResponse.setContentLength(-1);
        servletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ServletOutputStream outputStream = servletResponse.getOutputStream();
        outputStream.write(encryptByte);
        outputStream.flush();
    }


    private boolean checkUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        uri = uri.replaceAll("//+", "/");
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        if (encryptCacheUri.contains(uri) && request.getMethod().equals("POST")) {
            return true;
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        InitHandler.handler(filterConfig, encryptCacheUri, isEncryptAnnotation);
    }


    @Override
    public void destroy() {

    }

}
