package com.wl.core.encrypt.config;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;

/**
 * 加密响应包装器
 *
 * @author gaoyang
 */
public class EncryptResponseWrapper extends HttpServletResponseWrapper {

    private ServletOutputStream filterOutput;
    private final ByteArrayOutputStream output;

    public EncryptResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }


    @Override
    public ServletOutputStream getOutputStream() {
        if (filterOutput == null) {
            filterOutput = new ServletOutputStream() {
                @Override
                public void write(int b) {
                    output.write(b);
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                }
            };
        }

        return filterOutput;
    }

    public byte[] getResponseData() {
        return output.toByteArray();
    }
}
