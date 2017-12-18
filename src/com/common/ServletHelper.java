package com.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Servlet������(�Ż�)
 */
public class ServletHelper{

    /**
     * ���ز���
     * @param req
     * @param names
     * @return
     */
    public static String getRemoteIp(Object request){
	    //��ȡԶ��IP
		 String ip = (String)invoke(request, "getHeader","x-forwarded-for");
		 if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) 
		 {
			ip = (String)invoke(request, "getHeader","Proxy-Client-IP");
		 }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) 
	    {
	    	ip = (String)invoke(request, "getHeader","WL-Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) 
	    {
	       ip = (String)invoke(request, "getRemoteAddr");
	    }
	    if(ip!=null && ip.indexOf(",") > -1)
	    {
			int i = ip.indexOf(",");
			ip = ip.substring(0,i);
		}
	   return ip;
    }
    
    public static Object invoke(Object obj, String method, Object... param){
    	try{
    		if(param == null){
    			try{ return obj.getClass().getMethod(method).invoke(obj);}catch(Exception e){}
    		}else{
        		Class<?>[] types = new Class[param.length];
        		for(int i=0; i<param.length; i++) types[i] = param[i].getClass();
    			try{ return obj.getClass().getMethod(method,types).invoke(obj,param);}catch(Exception e){}
    		}
    	}catch(Exception e){
    	}
    	return null;
    }
    
    
    /**
     * ���ز���
     * @param req
     * @param names
     * @return
     */
	public static String getParameter(Object request, String[] names, String def, boolean isUTF8){
    	if(names==null) return null;
    	for(String name : names ){
    		try{
    			String enc = decoding;
    			String value = null;
    			try{ request.getClass().getMethod("setCharacterEncoding",String.class).invoke(request,enc);}catch(Exception e){}
    			try{ value = (String)request.getClass().getMethod("getParameter",name.getClass()).invoke(request,name);}catch(Exception e){}
	        	if(value!=null) value = new String(value.getBytes(enc),isUTF8?"UTF-8":toencoding);
	        	if(value != null && value.length()>0 ){
	        		return value.trim();
	        	}
    		}catch(Exception e){
    		}
    	}
    	return def;
    }
	public static String getParameter(Object req, String[] names, String def){
		return getParameter(req, names, def,false);
	}
	public static String getParameter(Object req, String[] names, boolean isUTF8){
    	return getParameter(req, names, null,isUTF8);
    }
	public static String getParameter(Object req, String[] names){
    	return getParameter(req, names, null,false);
    }
    
    /**
     * ����Request�Ĳ���
     */
	public static  Map getParamMap(Object request,boolean isUTF8, String appendWord){
		Map params = new HashMap();
		String enc = decoding;
		Map paramMap = null;
		
		try{ request.getClass().getMethod("setCharacterEncoding",String.class).invoke(request,enc);}catch(Exception e){}
		try{ paramMap = (Map)request.getClass().getMethod("getParameterMap").invoke(request);}catch(Exception e){}
		
		for (Iterator iter = paramMap.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) paramMap.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? 
						valueStr + values[i]: valueStr + values[i] + ",";				
			}
			if(isUTF8){
				try{ 
					valueStr = new String(valueStr.getBytes(enc),"UTF-8");
				}catch(Exception e){}
			}else{
				try{ 
					valueStr = new String(valueStr.getBytes(enc),toencoding);
				}catch(Exception e){}
			}
			params.put(name, removeAppendWord(valueStr,appendWord));
		}
		return params;
	}
	public static  Map getParamMap(Object request){
    	return getParamMap(request,false,null);
    }
	public static  Map getParamMap(Object request,boolean isUTF8){
    	return getParamMap(request,isUTF8,null);
    }
    
	public static  void excludeKeys(Map map, String[] keys){
    	if(keys == null )return;
    	for(String key : keys){
    		map.remove(key);
    	}
    }
	private static String removeAppendWord(String valueStr,String appendWord){
    	if(valueStr==null||appendWord==null) return valueStr;
		valueStr = valueStr.trim();
		if(valueStr.endsWith(appendWord)){
			try{
				valueStr = valueStr
					.substring(0,valueStr.length()-appendWord.length());
			}catch(Exception e){}
		}
		return valueStr;
    }

	public static String getDecoding() {
		return decoding;
	}

	public static void setDecoding(String decoding) {
		ServletHelper.decoding = decoding;
		ServletHelper.store();
	}

	public static String getToencoding() {
		return toencoding;
	}

	public static void setToencoding(String toencoding) {
		ServletHelper.toencoding = toencoding;
		ServletHelper.store();
	}
	
    public static String getDbdecoding() {
		return dbdecoding;
	}

	public static void setDbdecoding(String dbdecoding) {
		ServletHelper.dbdecoding = dbdecoding;
	}

	public static String getDbtoencoding() {
		return dbtoencoding;
	}

	public static void setDbtoencoding(String dbtoencoding) {
		ServletHelper.dbtoencoding = dbtoencoding;
	}

	public static void load(){
		try {
			Properties prop = new Properties();
			InputStream in = ServletHelper.class.getResourceAsStream(CONF_NAME);
			prop.load(in);
			in.close();
			decoding = prop.getProperty("http.request.decoding");
			toencoding = prop.getProperty("http.request.toencoding");
			dbdecoding = prop.getProperty("database.query.decoding");
			dbtoencoding = prop.getProperty("database.query.toencoding");
		} catch (Exception e) {
			//log.warn("load properties.",e);
		}
    }
	
    public static void store(){
		try {
			Properties prop = new Properties();
			prop.setProperty("http.request.decoding",decoding);
			prop.setProperty("http.request.toencoding",toencoding);
			prop.setProperty("database.query.decoding",dbdecoding);
			prop.setProperty("database.query.toencoding",dbtoencoding);
			OutputStream out = new FileOutputStream(new File(
					ServletHelper.class.getResource(CONF_NAME).getFile()));
			prop.store(out, "");
			out.close();
		} catch (Exception e) {
			//log.warn("store properties.",e);
		}
    }
    
	/**
     * Servlet������뷽ʽ
     */
    private static String decoding = "GBK";
    /**
     * Servlet�����ٱ��뷽ʽ
     */
    private static String toencoding = "GBK";
	/**
     * Database��ѯ���뷽ʽ
     */
    private static String dbdecoding = "GBK";
    /**
     * Database��ѯ�ٱ��뷽ʽ
     */
    private static String dbtoencoding = "GBK";
	/**
	 * ʵ�����������Ļ���
	 */
    private static String CONF_NAME = "/charcoding.properties";
	static{
		load();
	}
	protected void finalize() throws Throwable {
		store();
		super.finalize();
	}
}
