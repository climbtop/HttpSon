package com.common.socket;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class CookieWorker implements SocketWorker{

	private String contentType;
	private String charset;

	public CookieWorker() {
		contentType = "text/html";
		charset = "UTF-8";
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	protected String getWrapContent(byte[] data, String contentType, String charset) throws Exception {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("HTTP/1.1 200 OK\r\n");
			sb.append("Cache-Control: no-cache\r\n");
			sb.append("Cache-Control: no-store\r\n");
			sb.append("Pragma: no-cache\r\n");
			sb.append("Connection: close\r\n");
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			Date now = new Date();
			sb.append("Date: " + sdf.format(now) + "\r\n");
			sb.append("Expires: " + sdf.format(now) + "\r\n");
			if (charset != null) {
				sb.append("Content-Type: " + contentType + "; charset=" + charset + "\r\n");
			} else {
				sb.append("Content-Type: " + contentType + "\r\n");
			}
			sb.append("Content-Length: " + data.length + "\r\n");
			sb.append("\r\n");
			if (charset != null) {
				sb.append(new String(data, charset));
			} else {
				sb.append(new String(data));
			}
			return sb.toString();
		} catch (Exception e) {
			throw e;
		}
	}
	
	protected byte[] getReloadContent(String alert){
		return getReloadContent(alert,"");
	}
	
	protected byte[] getReloadContent(String alert, String params){
		String reload = "window.location.href=window.location.href;";
		if(params!=null && params.length()>0){
			reload = "window.location.href=window.location.href+'"+params+"';";
		}
		try {
			alert =  alert==null ? "" : ("alert('"+alert+"');");
			return (alert + reload).getBytes(getCharset());
		} catch (Exception e) {
			return null;
		}
	}
	
	protected Map<String,String> parseRequestHeaderMap(byte[] dataCopy){
		Map<String,String> headers = new HashMap<String,String>();
		String content = new String(dataCopy);
		String[] groups = content.split("\n");
		for(int i=1; i<groups.length && groups.length > 0; i++){
			String group = groups[i];
			if(group.trim().length()<=0){
				break;
			}
			group = group.trim();
			String name = "", value = "";
			if(group.indexOf(':')<0){
				name = group;
				value = "";
			}else{
				name = group.substring(0,group.indexOf(':'));
				value = group.substring(group.indexOf(':')+1);
			}
			try {
				value = URLDecoder.decode(value, getCharset());
				headers.put(name.trim(), value);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return headers;
	}
	
	protected String getRequestHeader(byte[] dataCopy, String name){
		return parseRequestHeaderMap(dataCopy).get(name);
	}
	
	protected Map<String,String> parseParametersMap(byte[] dataCopy){
		Map<String,String> parmeters = new HashMap<String,String>();
		String content = new String(dataCopy);
		String details = content.substring(0,content.indexOf('\n'));
		details = details.substring(0,details.lastIndexOf(" HTTP"));
		if(details.indexOf('?')<0){
			details = "";
		}else{
			details = details.substring(details.indexOf('?')+1);
		}
		String[] groups = details.split("&");
		for(int i=0; i<groups.length; i++){
			String group = groups[i];
			if(group.trim().length()<=0){
				continue;
			}
			group = group.trim();
			String name = "", value = "";
			if(group.indexOf('=')<0){
				name = group;
				value = "";
			}else{
				name = group.substring(0,group.indexOf('='));
				value = group.substring(group.indexOf('=')+1);
			}
			try {
				value = URLDecoder.decode(value, getCharset());
				parmeters.put(name.trim(), value);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return parmeters;
	}
	
	protected String getParameter(byte[] dataCopy, String name){
		return parseParametersMap(dataCopy).get(name);
	}
	
	protected String getRequestURI(byte[] dataCopy){
		String content = new String(dataCopy);
		String details = content.substring(0,content.indexOf('\n'));
		details = details.substring(0,details.lastIndexOf(" HTTP"));
		if(details.indexOf('?')>0){
			details = details.substring(0,details.indexOf('?'));
		}
		if(details.indexOf(' ')>0){
			details = details.substring(details.indexOf(' ')+1);
		}
		return details.trim();
	}

	public byte[] processData(byte[] dataIn) throws Exception{
		byte[] dataDone = processCookieData(dataIn);
		String dataText = getWrapContent(dataDone, getContentType(), getCharset());
		return dataText.getBytes(getCharset());
	}
	
	abstract public byte[] processCookieData(byte[] dataIn);
	
}
