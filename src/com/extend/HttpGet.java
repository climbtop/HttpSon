/**
 * ��   ����	HttpGet.java
 * �������ڣ�	2010-04-12
 * �ļ�˵����	
 * �� �� �ߣ�����ʢ
 * �ش��޸ļ�¼��
 * 			2010-04-12 by ����ʢ �� ����
 * 
 * Copyright (C) 2008 - ��Ȩ����   2008 21CN Corp. Ltd
 */
package com.extend;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.httpclient.HttpData;
/**
 * Ĭ������,���˾�����
 */
public class HttpGet extends HttpData{
	protected String[] values = null;
	protected String regex = null;
	protected LinkedList<String> lists = null;
	
	public HttpGet(String url){
		super(url);
	}
	
	public HttpGet(Map map){
		super(map);
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
	
	public List<String> getMultiVal(String regex) {
		lists = new LinkedList<String>();
		List<String[]> valList = this.parserContentListByRegex(regex);
		if(valList!=null && valList.size()>0){
			for(String[] sarr : valList){
				if(sarr!=null && sarr.length>0){
					lists.addLast(sarr[0]);
				}
			}
		}
		return lists;
	}
	
	public List<String[]> getMultiVals(String regex) {
		return this.parserContentListByRegex(regex);
	}
	
	public List<String[]> getTabVals(){
		return Httper.getTabVals(getContent());
	}
	
	public List<String[]> getTabVals(int columns){
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
	
	public String getNodeVals(String nodeword){
		return Httper.getNodeVals(nodeword,getContent());
	}
	public String getNodeVals(String nodeword, boolean include){
		return Httper.getNodeVals(nodeword,include,getContent());
	}
	
}