package com.wl.core.encrypt.wrapper;

import org.springframework.http.MediaType;

/**
 * 请求加密包装器工厂
 *
 * @author gaoyang
 */
public class EncryptRequestWrapperFactory {

	public static boolean contentIsJson(String contentType) {
		return contentType.equals(MediaType.APPLICATION_JSON_VALUE.toLowerCase()) ||
				contentType.equals(MediaType.APPLICATION_JSON_UTF8_VALUE.toLowerCase());
	}
}
