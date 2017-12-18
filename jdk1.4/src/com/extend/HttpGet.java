/**
 * 文   件：	HttpGet.java
 * 创建日期：	2010-04-12
 * 文件说明：	
 * 创 建 者：朱孙盛
 * 重大修改记录：
 * 			2010-04-12 by 朱孙盛 ： 创建
 * 
 * Copyright (C) 2008 - 版权所有   2008 21CN Corp. Ltd
 */
package com.extend;
import java.util.LinkedList;
import java.util.List;

import com.httpclient.HttpData;
/**
 * 默认例子,做了精简处理
 */
public class HttpGet extends HttpData{
	protected String[] values = null;
	protected String regex = null;
	protected LinkedList lists = null;
	public HttpGet(String url){
		super(url);
	}
	public  void  callback(){
	}
	
	public String getVal(String regex) {
		values = this.parserContentByRegex(regex);
		if(values!=null && values.length>0){
			return values[0];
		}
		return "";
	}
	
	public String[] getVals(String regex) {
		return this.parserContentByRegex(regex);
	}
	
	public List getMultiVal(String regex) {
		lists = new LinkedList();
		List valList = this.parserContentListByRegex(regex);
		if(valList!=null && valList.size()>0){
			for(int i=0; i<valList.size(); i++){
			    String[] sarr  = (String[])valList.get(i);
				if(sarr!=null && sarr.length>0){
					lists.addLast(sarr[0]);
				}
			}
		}
		return lists;
	}
	
	public List getMultiVals(String regex) {
		return this.parserContentListByRegex(regex);
	}
	
	public List getTabVals(){
		return Httper.getTabVals(getContent());
	}
	
	public List getTabVals(int columns){
		return Httper.getTabVals(columns, getContent());
	}
	
	public String getValById(String marker){
		return Httper.getValById(marker, getContent());
	}
	
	public String getValByName(String marker){
		return Httper.getValByName(marker, getContent());
	}
	
	public String subString(String startword,String endword, boolean include){
		return Httper.subString(startword,endword,include,getContent());
	}
	public String subString(String startword,String endword){
		return Httper.subString(startword,endword,getContent());
	}
	
	public String substring(String startword,String endword, boolean include){
		return Httper.substring(startword,endword,include,getContent());
	}
	public String substring(String startword,String endword){
		return Httper.substring(startword,endword,getContent());
	}
	
	public String delstring(String startword,String endword){
		return Httper.delstring(startword,endword,getContent());
	}
	public String delString(String startword,String endword){
		return Httper.delString(startword,endword,getContent());
	}
	

}