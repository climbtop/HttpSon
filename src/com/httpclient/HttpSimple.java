package com.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
/**
 * HTTP 简单管理器
 */
public class HttpSimple implements HttpHelper{
	
	private HttpData _tc;	//创建一个客户端，类似打开一个浏览器
	private String cookies = "";  //存储的cookie
	private String refererUrl = "",currentUrl = ""; //上一页关联(read)
	private HttpProxy proxy = null; //代理配置
	public static boolean isFollowRedirects = false; //禁止自动跳转

	public HttpHelper clone(){
		HttpSimple hs = new HttpSimple();
		hs._tc = null;
		hs.cookies = this.cookies;
		hs.refererUrl = this.refererUrl;
		hs.proxy = this.proxy;
		return hs;
	}
	
	public HttpData get_tc() {
		return _tc;
	}
	
	public HttpProxy getProxy() {
		return proxy;
	}
	public void setProxy(HttpProxy proxy) {
		this.proxy = proxy;
	}
	
	/**
     * 通过URL取得网页内容
     */
    public void visitURL(HttpData hppdData){
    	try{
    		visitURI(hppdData);
    	}catch(Throwable e){}
    }
    
	/**
     * 通过URL取得网页内容
     */
    public void visitURI(HttpData _tc) throws Throwable
    {  
    	visitURI(_tc, false);
    }
    
	/**
     * 通过URL取得网页内容
     */
    protected void visitURI(HttpData _tc, boolean redirect) throws Throwable
    {   
    	this._tc = _tc;
    	if ( _tc == null )	return;
    	if ( _tc.getMaxUrlTraces()>0 && 
       		 _tc.getMaxUrlTraces()<= _tc.getUrlTraces().size()){
       		return;
       	}
        HttpURLConnection.setFollowRedirects(isFollowRedirects);//设置自动跳转
    	HttpURLConnection connection = null;   
        OutputStream oStream = null;   
        InputStream iStream = null;     
        try {
        	loadGlobalProxy(); //加入全局代理
        	String postData = _tc.getPostData();
            URL url = new URL(_tc.getUrl());
            if(!redirect){
            	refererUrl = currentUrl; 
            }
            currentUrl = _tc.getUrl();
            _tc.getUrlTraces().add(currentUrl);
            connection = (HttpURLConnection)openConnection(url); 
            
            //设置自定义的RequestHeader
    		connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
    		connection.setRequestProperty("Referer", refererUrl);
            connection.setRequestProperty("Cookie", cookies); //设置cookie
            Map<String,String> requestHeader = _tc.getRequestHeader();
    		Iterator<String> iterator = requestHeader.keySet().iterator();
    		while (iterator.hasNext()) {
    			String key = iterator.next();
    			String value = requestHeader.get(key);
                connection.setRequestProperty(key,value); 
    		}
    		setProxyProperty(connection);//代理帐号密码验证
            
    		//连接的基本参数设定
    		connection.setConnectTimeout(_tc.getTimeout());
    		connection.setReadTimeout(_tc.getTimeout());
            connection.setDoOutput(true);   
            connection.setDoInput(true);
            connection.setRequestMethod(_tc.getPostMethod());
            if(postData.length()>0){
	            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());   
	            out.write(postData);   
	            out.close();   
            }
               
            connection.connect();  //建立连接
            int statusCode = connection.getResponseCode();
            
            //清楚并获取Cookie
            _tc.getResponseHeader().clear();
            String key="",keyval = "";
            for (int i = 1; key != null; i++ ) {   
            	key = connection.getHeaderFieldKey(i); 
            	keyval = connection.getHeaderField(i);
            	_tc.getResponseHeader().put(key, keyval);
                if (key!=null && key.equalsIgnoreCase("set-cookie")) {   
                    cookies = cookies+keyval+";";   
                }   
            }
            
            //跳转页面自动处理
            if(statusCode == 301 || statusCode == 302){
                String location = connection.getHeaderField("Location");
				if (location.indexOf("://") < 0) {
					String hostUrl = _tc.getDomain();
					if(hostUrl.endsWith("/")) 
						hostUrl = hostUrl.substring(0,hostUrl.length()-1);
					if(location.startsWith("/"))
						location = location.substring(1);
					location = hostUrl + "/" + location;
					if(location.indexOf("://")<0){
						location = "http://"+location;
					}
				}
				_tc.setUrl(location);
                _tc.setPostData("");
                _tc.setPostMethod("GET");
                visitURI(_tc, true);//重新请求新网页
            }else{
            	//非自动跳转请求
	            iStream = connection.getInputStream();   
	            byte[] bufferout = processServerResponse(
	            		(HttpURLConnection)connection, iStream, _tc.getEncoding());
	            
	            // 设置读取流的编码格式，自定义编码 
	            String _content  = new String(bufferout,_tc.getEncoding());
	            //取得数据后回调
	            _tc.setContent(_content);
				_tc.setBufferout(bufferout); 
				_tc.setCookie(getCookie());
				try{_tc.callback();}catch(Throwable e){}
            }
            connection.disconnect();
        } catch(Throwable e){
        	throw e;
        } finally {   
            if (iStream != null)   
                iStream.close();   
            if (oStream != null)   
                oStream.close();   
        }   
    }
    
	/**
     * 请求结果处理
     */
    private byte[] processServerResponse(HttpURLConnection http,   
            InputStream iStrm, String enc) throws Throwable {   
        byte[] responseMsg = null;   
        if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {   
            int length = http.getContentLength();     
                ByteArrayOutputStream bStrm = new ByteArrayOutputStream();   
                int ch;   
                while ( (ch = iStrm.read()) != -1){   
                    bStrm.write(ch);   
                }
                responseMsg = bStrm.toByteArray();   
                bStrm.close();   
            if (length != -1 ) {
            	if(responseMsg.length != length){
            		System.out.println("ContentLength: "+length+
            				", DownloadLength:"+responseMsg.length);   
            	}
            }
        } else {   
        	responseMsg = http.getResponseMessage().getBytes(enc);   
        }   
        return responseMsg;   
    }
	public String getCookies() {
		return cookies;
	}
	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
	public String getCookie() {
		return cookies;
	}
	public void removeCookie(String key) {
		cookies = cookies.replaceAll("\\s*"+key+"\\s*=[^;]*", "");
		cookies = cookies.replaceAll(";;", "");
	}
	public void setCookie(String key, String value) {
		cookies = cookies+";"+key+"="+value;  
	}
	public void setCookie(String cookies) {
		this.cookies = cookies;
	}
	
	private void loadGlobalProxy(){
		if(proxy==null) return;
		String type = proxy.getType();
		if(type==null || type.length()<=0){
			//type默认方式
			Properties prop = System.getProperties();
			if( proxy.getHost() != null && proxy.getPort() != null ){
	    		// 设置http访问要使用的代理服务器的地址
	    		prop.setProperty("http.proxyHost", proxy.getHost());
	    		// 设置http访问要使用的代理服务器的端口
	    		prop.setProperty("http.proxyPort", proxy.getPort().toString());  
				if( proxy.getUsername() != null && proxy.getPassword() != null ){
					final String username = proxy.getUsername();
					final String password = proxy.getPassword();
	        		// 设置http访问要使用的代理服务器的用户名
	        		prop.setProperty("http.proxyUser", username);
	        		// 设置http访问要使用的代理服务器的密码
	        		prop.setProperty("http.proxyPassword", password);
	        	    Authenticator.setDefault(new Authenticator(){
	        	        protected PasswordAuthentication getPasswordAuthentication() {  
	        	            return new PasswordAuthentication(username, password.toCharArray());  
	        	        }  
	        	    });  
				}
			}
		}
	}
	
	//返回是否代理的connection
	private URLConnection openConnection(URL url) throws IOException{
		if( !isProxyDirect(proxy) ){
			String name = proxy.getType();
			if( proxy.getHost() != null && proxy.getPort() != null ){
				Proxy.Type type = Proxy.Type.DIRECT;
				if("HTTP".equalsIgnoreCase(name)) type = Proxy.Type.HTTP;
				if("SOCKS".equalsIgnoreCase(name)) type = Proxy.Type.SOCKS;
				Proxy px = new Proxy(type, 
						new InetSocketAddress(proxy.getHost(), proxy.getPort()));
				return url.openConnection(px);
			}
		}
		return url.openConnection();
	}
	
	//格式如："Proxy-Authorization"= "Basic Base64.encode(user:password)" 
	private void setProxyProperty(HttpURLConnection conn){
		if( isProxyDirect(proxy) ) return;
		if( proxy.getUsername() == null || proxy.getPassword() == null )return;
		String headerKey = "Proxy-Authorization";  
		String value = (proxy.getUsername()+":"+proxy.getPassword());
		try{ value = new String(value.getBytes("utf-8")); }catch(Exception e){}
		String headerValue = "Basic " + value;
		conn.setRequestProperty(headerKey, headerValue); 
	}
	
	private boolean isProxyDirect(HttpProxy proxy){
		return (proxy == null || proxy.getType()==null || 
				"DIRECT".equalsIgnoreCase(proxy.getType()) || proxy.getType().length()<=0 );
	}
	
}

