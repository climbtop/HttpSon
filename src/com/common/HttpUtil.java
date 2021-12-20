package com.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 关于HTTP的工具类
 */
public class HttpUtil {

	public static boolean testURL(String uri){
		return testURL(uri, 3*1000);
	}
	public static boolean testURL(String uri, int waittime){
    	HttpURLConnection connection = null;    
        InputStream iStrm = null;  
    	try{
    		URL url = new URL(uri);
            connection = (HttpURLConnection)url.openConnection(); 
    		connection.setConnectTimeout(waittime);
    		connection.setReadTimeout(waittime);
    		connection.connect();
    		iStrm = connection.getInputStream();  
    		if(iStrm==null) return false;
	         while ( iStrm.read() != -1 ){ 
	        	 return true;
	         }
    		connection.disconnect();
    	}catch(Throwable e){
    	}finally {   
            if (iStrm != null){
                try{iStrm.close(); }catch(Exception e){}
            }
            if(connection!=null){
            	connection.disconnect();
            }
        }   
    	return false;
    }
    
    public static boolean testURL(String[] uris){
    	if(uris==null) return false;
    	for(int i=0; i<uris.length; i++){
    		if(uris[i].trim().length()<=0)continue;
    		if(testURL(uris[i])){ return true;}
    	}
    	return false;
    }
    
    public static String getDomain(String url){
		String temp = url;
		if(temp==null || temp.length()<=0) return "";
		if(temp.startsWith("http://")) temp = temp.substring(7);
		if(temp.startsWith("https://")) temp = temp.substring(8);
		if(temp.indexOf("/")>0){
			return temp.substring(0,temp.indexOf("/"));
		}
		return temp;
	}
    public static String getRelPath(String url){
		String temp = url;
		if(temp==null || temp.length()<=0) return "";
		if(temp.startsWith("http://")) temp = temp.substring(7);
		if(temp.startsWith("https://")) temp = temp.substring(8);
		if(temp.lastIndexOf("/")>0){
			return temp.substring(0,temp.lastIndexOf("/")+1);
		}
		if(!temp.endsWith("/")){
			temp = temp + "/";
		}
		return temp;
    }
    public static String getScheme(String url){
		String temp = url;
		if(temp==null || temp.length()<=0) return "";
		if(temp.toLowerCase().startsWith("http://"))  return "http://";
		if(temp.toLowerCase().startsWith("https://")) return "https://";
		return "";
    }
    
    public static boolean testTcpPort(InetAddress address, int port) {
		boolean flag = false;
		try {
			Socket socket = new Socket(address, port);
			socket.setReuseAddress(true);
			socket.close();
			flag = true;
		} catch (Exception e) {
		}
		return flag;
	}
	
	public static boolean testUdpPort(int port) {
		boolean flag = false;
		try {
			DatagramSocket socket = new DatagramSocket(port);
			socket.setReuseAddress(true);
			socket.close();
			flag = true;
		} catch (Exception e) {
		}
		return flag;
	}
	
	public static void setHttpSSLNoVerifier(HttpsURLConnection connection) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[1];
            trustAllCerts[0] = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(sslSocketFactory);

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            connection.setHostnameVerifier(hostnameVerifier);
        } catch (Exception e) {
        }
    }
	
	public static byte[] readFromURL(String urlStr) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpURLConnection connection = null;
        InputStream stream = null;
        try {
        	URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            if(urlStr.toLowerCase().startsWith("https")) {
            	setHttpSSLNoVerifier((HttpsURLConnection) connection);
            }
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
            	return baos.toByteArray();
            }
            stream = connection.getInputStream();
            if (stream != null) {
            	byte[] b=new byte[1024];
            	int z;
            	  while((z=stream.read(b, 0, b.length))!=-1){
            		  baos.write(b, 0, z);
            	  }
            }
        } catch(Exception e) {
        	e.printStackTrace();
        	return baos.toByteArray();
        } finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
				}
			}
        }
        return baos.toByteArray();
	}
	
}
