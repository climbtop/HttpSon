package com.httpclient;

import java.io.Serializable;

/**
 * HTTP 请求管理器接口类
 */
public interface HttpHelper extends Serializable{

	public void visitURL(HttpData httdData);
	public void visitURI(HttpData httdData)throws Throwable;
	public void setCookie(String key, String value);
	public void removeCookie(String key, String value);
	public String getCookie();
	public HttpProxy getProxy();
	public void setProxy(HttpProxy proxy);
	
}
