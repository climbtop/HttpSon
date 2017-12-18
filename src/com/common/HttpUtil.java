package com.common;

import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

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
	
}
