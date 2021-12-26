package com.httpclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP 请求实体类, postData不用URLEncoder
 */
public abstract class  HttpData implements Serializable{
	
	private String url = "";
	private String postMethod = "GET";
	private String encoding = "GBK"; //下载内容编码方式
	private String postDataEncoding = ""; //POST内容编码方式
	private Map<String, String> postData = new LinkedHashMap<String, String>();
	private Map<String,String> requestHeader = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER); //存储的请求Header
	private Map<String,String> responseHeader = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER); //存储的响应Header
	private List<String> urlTraces = new LinkedList<String>(); //跳转轨迹
	private int maxUrlTraces = 0; //最大跳转次数
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
	
	public HttpData(Map map){
		this.init(map);
	}
	
	public String getPostData() {
		if(hasOriginalData()) { //首先返回直接的最原始数据.
			return postOriginalData;
		}
		return getPostData(postData);
	}
	
	public String getPostData(Map<String, String> postData) {
		StringBuffer sb = new StringBuffer("");
		for (String key : postData.keySet()) {
			String value = postData.get(key);
			key = URLEncoder(key,getPostDataEncoding());
			value = URLEncoder(value,getPostDataEncoding());
			sb.append((sb.length() > 0 ? "&" : "") + key + "="
					+ (value != null ? value : ""));
		}
		return sb.toString();
	}
	
	public boolean hasOriginalData() {
		return postOriginalData!=null && postOriginalData.length()>0;
	}
	
	public String getEncodeData() {
		StringBuffer sb = new StringBuffer("");
		for (String key : postData.keySet()) {
			String value = postData.get(key);
			key = encode(key);
			value = encode(value);
			sb.append((sb.length() > 0 ? "&" : "") + key + "="
					+ (value != null ? value : ""));
		}
		return sb.toString();
	}
	
	public String getPostData(String key) {
		return this.postData.get(key);
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
		for (String keyValue : keyValueArray) {
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
	
	public void setRequestHeader(Map<String, String> requestHeader) {
		if (requestHeader == null)
			return;
		Iterator<String> iterator = requestHeader.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = requestHeader.get(key);
			this.requestHeader.put(key, value);
		}
	}
	
	public Map getResponseHeader() {
		return this.responseHeader;
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

	public String getPageEncodeing(){
		String cType = (String)this.getResponseHeader().get("Content-Type");
		if(cType!=null && cType.toLowerCase().indexOf("charset=")>=0){
			return cType.substring(cType.toLowerCase().indexOf("charset=")+8);
		}
		return getPageEncodeing("(?i)content=\"text/.*?;\\s*charset=(.*?)\"(?-i)");
	}
	
	public String getPageEncodeing(String regex){
		String[] values = this.parserContentByRegex(regex);
		if(values!=null && values.length>0){
			return values[0];
		}
		return getEncoding();
	}
	
	public void setPostData(Map<String, String> postData) {
		this.postData = postData;
	}
	
	public String getContent() {
		return content;
	}

	public String getContent(String enc) {
		if(enc==null) enc = getPageEncodeing();
		try{
			return new String( this.getBufferout(), enc);
		}catch(Exception e){}
		return this.getContent();
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
	
	public Map<String, String> getRequestHeader() {
		return requestHeader;
	}
	
	public Map<String, String> getPostDataByMap(){
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
	 * URL编码
	 */
	public String encode(String text){
		if(text == null )return "";
		try{
			text = text.replaceAll("%", "%25");
			text = text.replaceAll(" ", "%20");
			text = text.replaceAll("/", "%2F");
			text = text.replaceAll("\\?", "%3F");
			text = text.replaceAll("#", "%23");
			text = text.replaceAll("&", "%26");
			text = text.replaceAll("=", "%3D");
			text = text.replaceAll("\\+", "%2B");
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
		Long time = System.currentTimeMillis();
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
		Long time = System.currentTimeMillis();
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
	public List<String[]> parserContentListByRegex(String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(this.getContent());
		List<String[]> resultList = new ArrayList<String[]>();
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
		List<String[]> list = parserContentListByRegex(regex);
		String[] result = new String[list.size()];
		for(int i=0; i<result.length; i++){
			String[] cols = list.get(i);
			result[i] = cols.length>0?cols[0]:"";
		}
		return result;
	}
	
	/**
	 * 返回新连接的相对路径
	 */
    public static String getHref(String curUrl, String newUrl){
		if( newUrl.toLowerCase().startsWith("http://")||
				newUrl.toLowerCase().startsWith("https://")){
			return newUrl;
		}
		if( newUrl.toLowerCase().matches("^[\\w\\-]+\\.[\\w\\-]+\\.[\\w\\-]+")){
			return "http://"+newUrl;
		}
    	if(newUrl.startsWith("/")){
    		if( curUrl.toLowerCase().startsWith("http://")||
    			curUrl.toLowerCase().startsWith("https://")){
    			int index = curUrl.indexOf("/", 8);
    			index = index < 0 ? curUrl.length() : index;
    			return curUrl.substring(0,index)+newUrl;
    		}else{
    			int index = curUrl.indexOf("/");
    			index = index < 0 ? curUrl.length() : index;
    			return curUrl.substring(0,index)+newUrl;
    		}
    	}else{
    		if(curUrl.lastIndexOf("/")>8){
    			curUrl = curUrl.substring(0, curUrl.lastIndexOf("/")+1);
    		}else{
    			curUrl += "/";
    		}
    		return curUrl + newUrl;
    	}
	}
    public String getHref(String newUrl){
    	return getHref(this.url, newUrl);
    }
    
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
	
	public  abstract  void callback() ;

	
    public void init(Map map){
    	if(map.get("url")!=null){
    		this.setUrl( (String)map.get("url") );
    	}
    	if(map.get("method")!=null){
    		this.setPostMethod( (String)map.get("method") );
    	}
    	if(map.get("enc")!=null){
    		this.setEncoding( (String)map.get("enc") );
    	}
    	if(map.get("dataenc")!=null){
    		this.setPostDataEncoding( (String)map.get("dataenc"));
    	}
    	if(map.get("cookie")!=null){
    		this.setCookie( (String)map.get("cookie"));
    	}
    	if(map.get("orgdata")!=null){
    		this.setPostOriginalData( (String)map.get("orgdata"));
    	}
    	if(map.get("timeout")!=null){
    		try{ this.setTimeout( Integer.valueOf((String)map.get("timeout")).intValue()); }catch(Exception e){}
    	}
    	if(map.get("usedpool")!=null){
    		try{ this.setUsedPool( Boolean.valueOf((String)map.get("usedpool")).booleanValue()); }catch(Exception e){}
    	}
    	
    	Iterator it = map.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			Object val = map.get(key);
			if(val==null||key==null)continue;
			if(key.matches("data_key_\\d+")){
				String name = (String)val;
				key = key.replace("_key_", "_val_");
				String value = (String)map.get(key);
				if(value==null)continue;
				this.postData.put(name, value);
				continue;
			}
			if(key.matches("header_key_\\d+")){
				String name = (String)val;
				key = key.replace("_key_", "_val_");
				String value = (String)map.get(key);
				if(value==null)continue;
				this.requestHeader.put(name, value);
				continue;
			}
		}
    }
    
    public Map toMap(){
    	Map map = new LinkedHashMap();
    	if(this.getUrl()!=null){
    		map.put("url", this.getUrl());
    	}
    	if(this.getPostMethod()!=null){
    		map.put("method", this.getPostMethod());
    	}
    	if(this.getEncoding()!=null){
    		map.put("enc", this.getEncoding());
    	}
    	if(this.getPostDataEncoding()!=null){
    		map.put("dataenc", this.getPostDataEncoding());
    	}
    	if(this.getCookie()!=null){
    		map.put("cookie", this.getCookie());
    	}
    	if(this.getPostOriginalData()!=null){
    		map.put("orgdata", this.getPostOriginalData());
    	}
    	if(this.getTimeout()>=0){
    		map.put("timeout", this.getTimeout()+"");
    	}
    	if(this.isUsedPool()||!this.isUsedPool()){
    		map.put("usedpool", this.isUsedPool()+"");
    	}
    	if(this.postData!=null && this.postData.size()>0){
    		Iterator it = this.postData.keySet().iterator();
    		int i = 0;
    		while(it.hasNext()){
    			Object key = it.next();
    			Object val = this.postData.get(key);
    			if(val==null) continue;
    			map.put("data_key_"+i, key);
    			map.put("data_val_"+i, val);
    			i ++;
    		}
    	}
    	if(this.requestHeader!=null && this.requestHeader.size()>0){
    		Iterator it = this.requestHeader.keySet().iterator();
    		int i = 0;
    		while(it.hasNext()){
    			Object key = it.next();
    			Object val = this.requestHeader.get(key);
    			if(val==null) continue;
    			map.put("header_key_"+i, key);
    			map.put("header_val_"+i, val);
    			i ++;
    		}
    	}
    	return map;
    }
	
	public List<String> getUrlTraces() {
		return urlTraces;
	}

	public void setMaxUrlTraces(int maxUrlTraces) {
		this.maxUrlTraces = maxUrlTraces;
	}

	public int getMaxUrlTraces() {
		return maxUrlTraces;
	}
	
}
