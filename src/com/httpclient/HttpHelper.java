package com.httpclient;

import java.io.Serializable;

/**
 * HTTP ����������ӿ���
 */
public interface HttpHelper extends Serializable{

	public void visitURL(HttpData httdData);
	public void visitURI(HttpData httdData)throws Throwable;
	public void setCookie(String key, String value);
	public void setCookie(String cookies);
	public void removeCookie(String key);
	public String getCookie();
	public HttpProxy getProxy();
	public void setProxy(HttpProxy proxy);
	public HttpHelper clone();
}
