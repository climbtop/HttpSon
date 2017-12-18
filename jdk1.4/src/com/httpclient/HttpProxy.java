/**
 * ��   ����	HttpProxy.java
 * �������ڣ�	2010-04-21
 * �ļ�˵����	
 * �� �� �ߣ�����ʢ
 * �ش��޸ļ�¼��
 * 			2010-04-21 by ����ʢ �� ����
 * 
 * Copyright (C) 2008 - ��Ȩ����   2008 21CN Corp. Ltd
 */
package com.httpclient;

import java.io.Serializable;

/**
 * ��������
 */
public class HttpProxy implements Serializable{

	private String host = null ; /*��������IP��ַ*/
	private Integer port = null; /*�������Ӷ˿ں�*/
	private String username = null; /*���������û���*/
	private String password = null; /*������������*/
	private boolean preemptive = true; /*����������֤*/

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
