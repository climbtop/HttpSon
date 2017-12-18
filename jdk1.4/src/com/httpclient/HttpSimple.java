package com.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
    public void visitURI(HttpData _tc) throws Throwable{
    	this._tc = _tc;
    	if ( _tc == null )	return;
        try{HttpURLConnection.setFollowRedirects(isFollowRedirects);}catch(Throwable e){}//设置自动跳转
    	HttpURLConnection connection = null;   
        OutputStream oStream = null;   
        InputStream iStream = null;     
        try {   
        	if(proxy != null) {//加入代理配置
        		Properties prop = System.getProperties();
        		if( proxy.getHost() != null && proxy.getPort() != null ){
            		// 设置http访问要使用的代理服务器的地址
            		prop.setProperty("http.proxyHost", proxy.getHost());
            		// 设置http访问要使用的代理服务器的端口
            		prop.setProperty("http.proxyPort", proxy.getPort().toString());  
        			if( proxy.getUsername() != null && proxy.getPassword() != null ){
                		// 设置http访问要使用的代理服务器的用户名
                		prop.setProperty("http.proxyUser", proxy.getUsername());
                		// 设置http访问要使用的代理服务器的密码
                		prop.setProperty("http.proxyPassword", proxy.getPassword()); 
        			}
        		}
        	}
        	String postData = _tc.getPostData();
            URL url = new URL(_tc.getUrl());
        	refererUrl = currentUrl; currentUrl = _tc.getUrl();
            connection = (HttpURLConnection)url.openConnection(); 
            
            //设置自定义的RequestHeader
    		connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
    		connection.setRequestProperty("Referer", refererUrl);
            connection.setRequestProperty("Cookie", cookies); //设置cookie
            Map requestHeader = _tc.getRequestHeader();
    		Iterator iterator = requestHeader.keySet().iterator();
    		while (iterator.hasNext()) {
    			String key = (String)iterator.next();
    			String value = (String)requestHeader.get(key);
                connection.setRequestProperty(key,value); 
    		}
            
    		//连接的基本参数设定
    		try{
    			connection.getClass().getMethod("setConnectTimeout", new Class[]{int.class})
    			.invoke(connection, new Object[]{new Integer(_tc.getTimeout())});
    		}catch(Throwable e){}
    		try{
    			connection.getClass().getMethod("setReadTimeout", new Class[]{int.class})
    			.invoke(connection, new Object[]{new Integer(_tc.getTimeout())});
    		}catch(Throwable e){}
            connection.setDoOutput(true);   
            connection.setDoInput(true);
            connection.setRequestMethod(_tc.getPostMethod());
            if(postData.length()>0){
	            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());   
	            out.write(postData);   
	            out.close();   
            }
               
            connection.connect();  //建立连接
            //获取Cookie
            String key="",cookieVal = "";
            for (int i = 1; key != null; i++ ) {   
            	key = connection.getHeaderFieldKey(i); 
            	cookieVal = connection.getHeaderField(i);
                if (key!=null && key.equalsIgnoreCase("set-cookie")) {   
                    cookies = cookies+cookieVal+";";   
                }   
            }
            
            //跳转页面自动处理
            int statusCode = connection.getResponseCode();
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
                visitURI(_tc);//重新请求新网页
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
	public void removeCookie(String key, String value) {
		cookies = cookies.replaceAll("\\s*"+key+"\\s*=[^;]*", "");
		cookies = cookies.replaceAll(";;", "");
	}
	public void setCookie(String key, String value) {
		cookies = cookies+";"+key+"="+value;  
	}

}

