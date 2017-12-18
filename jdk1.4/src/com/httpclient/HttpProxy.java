/**
 * 文   件：	HttpProxy.java
 * 创建日期：	2010-04-21
 * 文件说明：	
 * 创 建 者：朱孙盛
 * 重大修改记录：
 * 			2010-04-21 by 朱孙盛 ： 创建
 * 
 * Copyright (C) 2008 - 版权所有   2008 21CN Corp. Ltd
 */
package com.httpclient;

import java.io.Serializable;

/**
 * 代理设置
 */
public class HttpProxy implements Serializable{

	private String host = null ; /*代理连接IP地址*/
	private Integer port = null; /*代理连接端口号*/
	private String username = null; /*代理连接用户名*/
	private String password = null; /*代理连接密码*/
	private boolean preemptive = true; /*代理抢先认证*/

	public HttpProxy(){
	}
	
	public HttpProxy(String host, Integer port){
		this.host = host;
		this.port = port;
	}
	
	public HttpProxy(String host, Integer port, String username, String password){
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isPreemptive() {
		return preemptive;
	}

	public void setPreemptive(boolean preemptive) {
		this.preemptive = preemptive;
	}

}
