package com.httpclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 请求实体类, postData不用URLEncoder
 */
public abstract class HttpData implements Serializable{
	
	private String url = "";
	private String postMethod = "GET";
	private String encoding = "GBK"; //下载内容编码方式
	private String postDataEncoding = ""; //POST内容编码方式
	private Map postData = new HashMap();
	private Map requestHeader = new HashMap(); //存储的cookie
	//readable
	private String content = ""; //返回内容(文本)
    private byte[] bufferout = null;//返回内容(二进制)
	private String cookie = ""; 
	private String postOriginalData = ""; //PostData最原始数据
	private int timeout = 0 ; //超时时间
	private boolean usedPool = false ; //是否使用线程池

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getPostOriginalData() {
		return postOriginalData;
	}

	public void setPostOriginalData(String postOriginalData) {
		this.postOriginalData = postOriginalData;
	}

	public byte[] getBufferout() {
		return bufferout;
	}

	public void setBufferout(byte[] bufferout) {
		this.bufferout = bufferout;
	}

	public HttpData(){
	}
	
	public HttpData(String url){
		this.setUrl(url);
	}
	
	public HttpData(String url,String postData){
		this.setUrl(url);
		this.setPostData(postData);
	}
	
	public HttpData(String url,String postData,String method){
		this.setUrl(url);
		this.setPostData(postData);
		this.setPostMethod(method);
	}
	
	public HttpData(String url,String postData,String method,String encoding){
		this.setUrl(url);
		this.setPostData(postData);
		this.setPostMethod(method);
		this.setEncoding(encoding);
	}
	
	public String getPostData() {
		if(postOriginalData!=null //首先返回直接的最原始数据.
				&& postOriginalData.length()>0){
			return postOriginalData;
		}
		StringBuffer sb = new StringBuffer("");
		Iterator it = postData.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			String value = (String)postData.get(key);
			key = URLEncoder(key,getPostDataEncoding());
			value = URLEncoder(value,getPostDataEncoding());
			sb.append((sb.length() > 0 ? "&" : "") + key + "="
					+ (value != null ? value : ""));
		}
		return sb.toString();
	}
	
	public String getPostData(String key) {
		return (String)this.postData.get(key);
	}
	/**
	 * 注意:key 和 value 不要 URL Encoder.
	 */
	public void setPostData(String key, String value) {
		this.postData.put(key, value);
	}
	public void removePostData(String key) {
		if(this.postData==null)return;
		this.postData.remove(key);
	}
	public void setPostData(String data) {
		if (data == null || data.length() <= 0)
			postData.clear();
		postData.clear();
		String[] keyValueArray = data.split("&");
		for(int i=0; i<keyValueArray.length; i++){
			String keyValue = keyValueArray[i];
			if (keyValue.length() > 0 && keyValue.indexOf("=") > 0) {
				String key = keyValue.substring(0, keyValue.indexOf("="));
				String value = keyValue.substring(keyValue.indexOf("=") + 1);
				postData.put(key, value);
			}
		}
	}
	public void setRequestHeader(String key, String value) {
		this.requestHeader.put(key, value);
	}
	
	public void setRequestHeader(Map requestHeader) {
		if (requestHeader == null)
			return;
		Iterator iterator = requestHeader.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = (String)requestHeader.get(key);
			this.requestHeader.put(key, value);
		}
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPostMethod() {
		return postMethod;
	}

	public void setPostMethod(String postMethod) {
		this.postMethod = postMethod;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setPostData(Map postData) {
		this.postData = postData;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	public Map getRequestHeader() {
		return requestHeader;
	}
	
	public Map getPostDataByMap(){
		return postData;
	}
	
	/**
	 * 当前路径
	 */
	public String getDomainUrl(){
		String url = this.getUrl();
		if(url==null || url.length()<=0) return "";
		if(url.lastIndexOf("/")>0){
			return url.substring(0,url.lastIndexOf("/")+1);
		}
		return url;
	}
	
	/**
	 * 当前域名
	 */
	public String getDomain(){
		String temp = this.getUrl();
		if(temp==null || temp.length()<=0) return "";
		if(temp.startsWith("http://")) temp = temp.substring(7);
		if(temp.startsWith("https://")) temp = temp.substring(8);
		if(temp.indexOf("/")>0){
			return temp.substring(0,temp.indexOf("/"));
		}
		return temp;
	}
	
	/**
	 * URL编码
	 */
	public String URLEncoder(String text,String encoding){
		if(text == null )return "";
		try{
			if(encoding==null||encoding.length()<=0){
				return URLEncoder.encode(text);
			}
			return URLEncoder.encode(text,encoding);
		}catch(Exception e){
		}
		return text;
	}
	
	/**
	 * URL解码
	 */
	public String URLDecoder(String text,String decoding){
		if(text == null )return "";
		try{
			if(decoding==null||decoding.length()<=0){
				return URLDecoder.decode(text);
			}
			return URLDecoder.decode(text,decoding);
		}catch(Exception e){
		}
		return text;
	}
	
	
	/**
	 * 保存网页
	 */
	public String saveFile(String path) {
		return saveFile(path,".html");
	}
	public String saveFile(String path,String ext, String encoding) {
		long time = System.currentTimeMillis();
		String name = time + ext;
		return saveAsFile(path, name, encoding);
	}
	public String saveAsFile(String path,String name, String encoding) {
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		String filePath = path + name;
		File file = new File(filePath);
		String content = this.getContent();
		//if(content.length()<=0)return filePath;
		try {
			byte[] data = content.getBytes(encoding);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(data);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filePath;
	}
	public String saveFile(String path,String ext) {
		return saveFile(path,ext,"GBK");
	}
	
	/**
	 * 保存数据
	 */
	public String saveBinary(String path) {
		return saveBinary(path,".dat");
	}
	public String saveBinary(String path,String ext) {
		long time = System.currentTimeMillis();
		String name = time + ext;
		return saveAsBinary(path, name);
	}
	public String saveAsBinary(String path,String name) {
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		String filePath = path + name;
		File file = new File(filePath);
		byte[] bufferout = this.getBufferout();
		if(bufferout==null || bufferout.length<=0)return filePath;
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(bufferout);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filePath;
	}
	
	/**
	 * 分析Content的内容,获取有用信息
	 */
	public String[] parserContentByRegex(String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(this.getContent());
		String[] result = new String[0];
		if(matcher.find()){
			result = new String[matcher.groupCount()];
			for(int i=1; i<=matcher.groupCount(); i++){
				String value = matcher.group(i);
				value = (value == null?"":value.trim());
				result[i-1] = value;
			}
		}
		return result;
	}
	
	/**
	 * 分析Content的内容列表,获取有用信息
	 */
	public List parserContentListByRegex(String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(this.getContent());
		List resultList = new ArrayList();
		while(matcher.find()){
			int groupCount = matcher.groupCount();
			if(groupCount>0){
				String[] result = new String[groupCount];
				for(int i=1; i<=groupCount; i++){
					String value = matcher.group(i);
					value = (value == null?"":value.trim());
					result[i-1] = value;
				}
				resultList.add(result);
			}
		}
		return resultList;
	}
	
	/**
	 * 分析Content的内容列表第一个,获取有用信息
	 */
	public String[] parserContentFirstByRegex(String regex){
		List list = parserContentListByRegex(regex);
		String[] result = new String[list.size()];
		for(int i=0; i<result.length; i++){
			String[] cols = (String[])list.get(i);
			result[i] = cols.length>0?cols[0]:"";
		}
		return result;
	}
	
	abstract public void callback();

	public String getPostDataEncoding() {
		return postDataEncoding;
	}

	public void setPostDataEncoding(String postDataEncoding) {
		this.postDataEncoding = postDataEncoding;
	}

	public boolean isUsedPool() {
		return usedPool;
	}

	public void setUsedPool(boolean usedPool) {
		this.usedPool = usedPool;
	}

}
