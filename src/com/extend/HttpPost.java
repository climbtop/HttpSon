/**
 * ��   ����	HttpPost.java
 * �������ڣ�	2011-03-06
 * �ļ�˵����	
 * �� �� �ߣ�����ʢ
 * �ش��޸ļ�¼��
 * 			2010-04-12 by ����ʢ �� ����
 * 
 * Copyright (C) 2008 - ��Ȩ����   2008 21CN Corp. Ltd
 */
package com.extend;

import java.util.Map;

/**
 * Ĭ������,���˾�����
 */
public class HttpPost extends HttpGet{
	public HttpPost(String url){
		super(url);
		this.setPostMethod("POST");
	}
	public HttpPost(Map map){
		super(map);
		this.setPostMethod("POST");
	}
}