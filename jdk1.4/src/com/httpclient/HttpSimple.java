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
 * HTTP �򵥹�����
 */
public class HttpSimple implements HttpHelper{
	
	private HttpData _tc;	//����һ���ͻ��ˣ����ƴ�һ�������
	private String cookies = "";  //�洢��cookie
	private String refererUrl = "",currentUrl = ""; //��һҳ����(read)
	private HttpProxy proxy = null; //��������
	public static boolean isFollowRedirects = false; //��ֹ�Զ���ת

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
     * ͨ��URLȡ����ҳ����
     */
    public void visitURL(HttpData hppdData){
    	try{
    		visitURI(hppdData);
    	}catch(Throwable e){}
    }
    
	/**
     * ͨ��URLȡ����ҳ����
     */
    public void visitURI(HttpData _tc) throws Throwable{
    	this._tc = _tc;
    	if ( _tc == null )	return;
        try{HttpURLConnection.setFollowRedirects(isFollowRedirects);}catch(Throwable e){}//�����Զ���ת
    	HttpURLConnection connection = null;   
        OutputStream oStream = null;   
        InputStream iStream = null;     
        try {   
        	if(proxy != null) {//�����������
        		Properties prop = System.getProperties();
        		if( proxy.getHost() != null && proxy.getPort() != null ){
            		// ����http����Ҫʹ�õĴ���������ĵ�ַ
            		prop.setProperty("http.proxyHost", proxy.getHost());
            		// ����http����Ҫʹ�õĴ���������Ķ˿�
            		prop.setProperty("http.proxyPort", proxy.getPort().toString());  
        			if( proxy.getUsername() != null && proxy.getPassword() != null ){
                		// ����http����Ҫʹ�õĴ�����������û���
                		prop.setProperty("http.proxyUser", proxy.getUsername());
                		// ����http����Ҫʹ�õĴ��������������
                		prop.setProperty("http.proxyPassword", proxy.getPassword()); 
        			}
        		}
        	}
        	String postData = _tc.getPostData();
            URL url = new URL(_tc.getUrl());
        	refererUrl = currentUrl; currentUrl = _tc.getUrl();
            connection = (HttpURLConnection)url.openConnection(); 
            
            //�����Զ����RequestHeader
    		connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
    		connection.setRequestProperty("Referer", refererUrl);
            connection.setRequestProperty("Cookie", cookies); //����cookie
            Map requestHeader = _tc.getRequestHeader();
    		Iterator iterator = requestHeader.keySet().iterator();
    		while (iterator.hasNext()) {
    			String key = (String)iterator.next();
    			String value = (String)requestHeader.get(key);
                connection.setRequestProperty(key,value); 
    		}
            
    		//���ӵĻ��������趨
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
               
            connection.connect();  //��������
            //��ȡCookie
            String key="",cookieVal = "";
            for (int i = 1; key != null; i++ ) {   
            	key = connection.getHeaderFieldKey(i); 
            	cookieVal = connection.getHeaderField(i);
                if (key!=null && key.equalsIgnoreCase("set-cookie")) {   
                    cookies = cookies+cookieVal+";";   
                }   
            }
            
            //��תҳ���Զ�����
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
                visitURI(_tc);//������������ҳ
            }else{
            	//���Զ���ת����
	            iStream = connection.getInputStream();   
	            byte[] bufferout = processServerResponse(
	            		(HttpURLConnection)connection, iStream, _tc.getEncoding());
	            
	            // ���ö�ȡ���ı����ʽ���Զ������ 
	            String _content  = new String(bufferout,_tc.getEncoding());
	            //ȡ�����ݺ�ص�
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
     * ����������
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

