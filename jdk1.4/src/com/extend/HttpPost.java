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
/**
 * Ĭ������,���˾�����
 */
public class HttpPost extends HttpGet{
	public HttpPost(String url){
		super(url);
		this.setPostMethod("POST");
	}
}