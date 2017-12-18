/**
 * 文   件：	HttpPost.java
 * 创建日期：	2011-03-06
 * 文件说明：	
 * 创 建 者：朱孙盛
 * 重大修改记录：
 * 			2010-04-12 by 朱孙盛 ： 创建
 * 
 * Copyright (C) 2008 - 版权所有   2008 21CN Corp. Ltd
 */
package com.extend;
/**
 * 默认例子,做了精简处理
 */
public class HttpPost extends HttpGet{
	public HttpPost(String url){
		super(url);
		this.setPostMethod("POST");
	}
}