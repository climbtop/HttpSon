package com.httpclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * HTTP 请求管理器
 */
public class HttpSession implements HttpHelper{

	private HttpData _tc;	//创建一个客户端，类似打开一个浏览器
	private String cookies = null; //临时存储cookie串
	private Map<String,String> cookie = new LinkedHashMap<String,String>(); //存储的cookie
	private String refererUrl = "",currentUrl = ""; //上一页关联(read)
	private HttpProxy proxy = null; //代理配置
	private static MultiThreadedHttpConnectionManager conManager = 
        new MultiThreadedHttpConnectionManager();//多线程时使用
	static{
		Protocol myhttps = new Protocol("https",new HttpSecureProtocolSocketFactory(), 443);
		Protocol.registerProtocol("https", myhttps);
	}
	//多线程管理器参数设定
	public static void setHttpManagerParam(int conTimeout, int socTimeout, int maxThread){
		if(conManager==null) return;
        HttpConnectionManagerParams params = conManager.getParams(); 
        params.setConnectionTimeout(conTimeout);
        params.setSoTimeout(socTimeout);
        params.setMaxTotalConnections(maxThread);
        params.setDefaultMaxConnectionsPerHost(maxThread);
	}
	
	public HttpHelper clone(){
		HttpSession hs = new HttpSession();
		hs._tc = null;
		hs.cookies = this.cookies;
		hs.refererUrl = this.refererUrl;
		hs.currentUrl = this.currentUrl;
		hs.proxy = this.proxy;
		hs.cookie.putAll(cookie);
		return hs;
	}
	
	public HttpData get_tc() {
		return _tc;
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
    	String postData = "";
        if(_tc.getPostData()!=null && !_tc.getPostData().equals(""))
        {
            postData = _tc.getPostData();
        }
        if(_tc.getPostMethod()==null || _tc.getPostMethod().equals(""))
            _tc.setPostMethod("GET");
        String postUrl = _tc.getUrl();
        if(!redirect){
        	refererUrl = currentUrl; 
        }
        currentUrl = postUrl;
        _tc.getUrlTraces().add(currentUrl);
        //--------Transfer Start------------------------------------------------------------------;
        
        HttpClient httpClient = (_tc.isUsedPool()?new HttpClient(conManager):new HttpClient());
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(_tc.getTimeout());
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        if(proxy != null) {//加入代理配置
    		if( proxy.getHost() != null && proxy.getPort() != null ){
    			//设置代理服务器的ip地址和端口   
    			httpClient.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());   
    			//使用抢先认证   
    			httpClient.getParams().setAuthenticationPreemptive(proxy.isPreemptive() );  
    			if( proxy.getUsername() != null && proxy.getPassword() != null ){
    				//如果代理需要密码验证，这里设置用户名密码   
    				httpClient.getState().setProxyCredentials(
    						AuthScope.ANY, new UsernamePasswordCredentials(
    						proxy.getUsername(),proxy.getPassword()));  
    			}
    		}
        }
        
        if(_tc.getPostMethod().toUpperCase().equals("GET"))
        {
            if(!postData.equals(""))
            {
                if(postUrl.indexOf("?") > -1)
                    postUrl += "&"+postData;
                else
                    postUrl += "?"+postData;
            }

            GetMethod getHC = new GetMethod(postUrl);
            getHC.setFollowRedirects(false);
            getHC.getParams().setParameter(HttpMethodParams.SO_TIMEOUT,_tc.getTimeout());
            getHC.getParams().setHttpElementCharset("GBK");
            
            //发送Cookie
            StringBuilder cookieValue = new StringBuilder();
            for(String key : this.cookie.keySet()){
                cookieValue.append(key);
                cookieValue.append("=");
                cookieValue.append(this.cookie.get(key));
                cookieValue.append("; ");
            }
            getHC.setRequestHeader("Cookie", this.cookies!=null?this.cookies:cookieValue.toString());
            getHC.setRequestHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)"); 
            getHC.setRequestHeader("Referer", refererUrl);
            
            //设置自定义的RequestHeader
            Map<String,String> requestHeader = _tc.getRequestHeader();
    		Iterator<String> iterator = requestHeader.keySet().iterator();
    		while (iterator.hasNext()) {
    			String key = iterator.next();
    			String value = requestHeader.get(key);
    			getHC.setRequestHeader(key, value);
    		}
            
            int statusCode;
            try {
                statusCode = httpClient.executeMethod(getHC);

                Header[] headers = getHC.getResponseHeaders();
                _tc.getResponseHeader().clear();
                for(Header header : headers){
                	_tc.getResponseHeader().put(header.getName(), header.getValue());
                	if( header.getName().equalsIgnoreCase("Set-Cookie") ){
                        if(header.getElements().length > 1){
                            HeaderElement[] elements = header.getElements();
                            for(HeaderElement element : elements){
                                checkCookie(element);
                            }
                        }
                        else
                        {
                            checkCookie(header);
                        }
                	}
                }
               
                if(statusCode==HttpStatus.SC_OK || (statusCode != 301 && statusCode != 302)){
                    
                    String charset = _tc.getEncoding();
                    
                    //下载信息
                    InputStream inputStream = getHC.getResponseBodyAsStream();
                    try {
        				BufferedInputStream bin = new BufferedInputStream(inputStream);
        				ByteArrayOutputStream bufferout = new ByteArrayOutputStream();
        				 // 使用二进制读取方式，循环读取源文件内容 
        				byte buf[] = new byte[1024];
        				int  len;
        				while( (len = bin.read(buf)) > -1 ){//顺序读取的内容并赋值给整型变量b,直到文件结束为止。
        					bufferout.write(buf, 0, len);
        				}
        				bin.close();
        				getHC.abort();
                        //取得采集的内容
                        String _content  = bufferout.toString(charset);// 设置读取流的编码格式，自定义编码 
                        //取得数据后回调
                        _tc.setContent(_content);
        				_tc.setBufferout(bufferout.toByteArray()); 
        				 bufferout.close();
                        _tc.setCookie(getCookie());
                        try{_tc.callback();}catch(Throwable e){}
        			} catch (Throwable e) {
        				throw e;
        			}
    				if(inputStream!=null)
    					try{inputStream.close();}catch(Exception e){}
                }
                else 
                {
                    if(statusCode == 301 || statusCode == 302){                        
                        Header locationHeader = getHC.getResponseHeader("location");
                        String location = locationHeader.getValue();
						if (location.indexOf("://") < 0) {
							String hostUrl = getHC.getHostConfiguration().getHostURL();
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
                        
                    }
                    
                }
                
                
            } catch (HttpException e2) {
            	throw e2;
            } catch (IOException e2) {
            	throw e2;
            } catch (Throwable e3){
            	throw e3;
            } finally {
            	getHC.releaseConnection();
 			}
        }
        else
        {
            PostMethod postHC = new PostMethod(postUrl);
            //postHC.setFollowRedirects(false);
            postHC.getParams().setParameter(HttpMethodParams.SO_TIMEOUT,_tc.getTimeout());
            postHC.getParams().setHttpElementCharset("GBK");
            
            //发送Cookie
            StringBuilder cookieValue = new StringBuilder();
            for(String key : this.cookie.keySet()){
                cookieValue.append(key);
                cookieValue.append("=");
                cookieValue.append(this.cookie.get(key));
                cookieValue.append("; ");
            }
            postHC.setRequestHeader("Cookie", this.cookies!=null?this.cookies:cookieValue.toString());
            postHC.setRequestHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
            postHC.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
            postHC.setRequestHeader("Referer", refererUrl);
            
            //设置自定义的RequestHeader
            Map<String,String> requestHeader = _tc.getRequestHeader();
    		Iterator<String> iterator = requestHeader.keySet().iterator();
    		while (iterator.hasNext()) {
    			String key = iterator.next();
    			String value = requestHeader.get(key);
    			postHC.setRequestHeader(key, value);
    		}
            
            //post数据到服务器
            if(postData!=null && !postData.equals("")){
            	postHC.setRequestBody(postData);
            }
            
            int statusCode;
            try {
                statusCode = httpClient.executeMethod(postHC);

                Header[] headers = postHC.getResponseHeaders();
                _tc.getResponseHeader().clear();
                for(Header header : headers){
                	_tc.getResponseHeader().put(header.getName(), header.getValue());
                	if( header.getName().equalsIgnoreCase("Set-Cookie") ){
                        if(header.getElements().length > 1){
                            HeaderElement[] elements = header.getElements();
                            for(HeaderElement element : elements){
                                checkCookie(element);
                            }
                        }
                        else
                        {
                            checkCookie(header);
                        }
                	}
                }

                if(statusCode==HttpStatus.SC_OK || (statusCode != 301 && statusCode != 302) ){
                    
                    String charset = _tc.getEncoding();
                                  
                    //下载信息
                    InputStream inputStream = postHC.getResponseBodyAsStream();
                    try {
        				BufferedInputStream bin = new BufferedInputStream(inputStream);
        				ByteArrayOutputStream bufferout = new ByteArrayOutputStream();
        				 // 使用二进制读取方式，循环读取源文件内容 
        				byte buf[] = new byte[1024];
        				int  len;
        				while( (len = bin.read(buf)) > -1 ){//顺序读取的内容并赋值给整型变量b,直到文件结束为止。
        					bufferout.write(buf, 0, len);
        				}
        				bin.close();
        				postHC.abort();
                        //取得采集的内容
                        String _content  = bufferout.toString(charset);// 设置读取流的编码格式，自定义编码 
                        //取得数据后回调
                        _tc.setContent(_content);
        				_tc.setBufferout(bufferout.toByteArray());
        				 bufferout.close();
                        _tc.setCookie(getCookie());
                        try{_tc.callback();}catch(Throwable e){}
        			} catch (Throwable e) {
        				throw e;
        			} finally{
        				if(inputStream!=null)
        					try{inputStream.close();}catch(Exception e){}
        			}
                }
                else 
                {
                    if(statusCode == 301 || statusCode == 302){
                        Header locationHeader = postHC.getResponseHeader("location");
                        String location = locationHeader.getValue();
						if (location.indexOf("://") < 0) {
							String hostUrl = postHC.getHostConfiguration().getHostURL();
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
                    }
                    
                }
                
                
            } catch (HttpException e2) {
                throw e2;
            } catch (IOException e2) {
            	throw e2;
            } catch (Throwable e3){
            	throw e3;
            } finally {
            	postHC.releaseConnection();
 			}
        }
    }

    
    private void checkCookie(NameValuePair entry){
            //这个就是Cookie了
    		String name = entry.getName();
            String value = entry.getValue();
            if(name == null) return ;
            
            if(name.equalsIgnoreCase("Set-Cookie")==false){
	            if(value == null || value.equalsIgnoreCase("deleted")){
	            	this.cookie.remove(name);
	            }else{
	            	this.cookie.put(name,value);
	            }
            }else{
            	String[] keyvals = value.split(";");
            	for(String keyval: keyvals){
            		keyval = keyval.trim();
            		int index = keyval.indexOf("=");
            		if(index>0){
            			String tempName = keyval.substring(0,index);
            			String tempValue = keyval.substring(index+1);
        	            if(tempValue == null || tempValue.equalsIgnoreCase("deleted")){
        	            	this.cookie.remove(tempName);
        	            }else{
        	            	this.cookie.put(tempName,tempValue);
        	            }
            		}
            	}
            }
    }

    public void setCookie(String key, String value){
    	this.cookie.put(key,value);
    }
    
    public void removeCookie(String key){
    	this.cookie.remove(key);
    }
    
    public String getCookie(){
        StringBuilder cookieValue = new StringBuilder();
        for(String key : this.cookie.keySet()){
            cookieValue.append(key);
            cookieValue.append("=");
            cookieValue.append(this.cookie.get(key));
            cookieValue.append("; ");
        }
        if(cookieValue.length()==0){
        	return this.cookies;
        }
        return cookieValue.toString();
    }
    
	public void setCookie(String cookies) {
		this.cookies = cookies;
	}
    
	public HttpProxy getProxy() {
		return proxy;
	}
	public void setProxy(HttpProxy proxy) {
		this.proxy = proxy;
	}
}


