package com.httpclient.simple;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpWeb {
	
	private int port = 80;
	private int soTimeout = 30*1000;
	private int conTimeout = 10*1000;
	private String encoding = null;
	private Map header = null;
	
	public String visitURL(String url){
		byte[] baout = download(url);
		try {
			return encoding==null? 
					new String(baout):
			    	new String(baout,encoding);
		} catch (Exception e) {
			return e.toString();
		}
	}
	
	public byte[] download(String url){
		byte[] resp = null;  
		try{
			String domain = getDomain(url);
			if(domain.indexOf(":")>0){
				port = Integer.parseInt(domain.substring(domain.indexOf(":")+1));
				domain = domain.substring(0,domain.indexOf(":"));
			}
			
			Socket socket_telnet = null;
			DataInputStream in = null;
			BufferedWriter out = null;
			
			socket_telnet = new Socket();
			InetSocketAddress isa = new InetSocketAddress(domain, port);
			//InetSocketAddress isa = new InetSocketAddress("127.0.0.1",8888);
			socket_telnet.setSoTimeout(soTimeout);
			socket_telnet.connect(isa, conTimeout);
			
			if(socket_telnet.isConnected()) {
				out = new BufferedWriter(new OutputStreamWriter(
						socket_telnet.getOutputStream()));
				
				StringBuffer packet = new StringBuffer();
				packet.append( "GET " + url + " HTTP/1.1\r\n" );
				packet.append( "Accept: */*\r\n"   );
				packet.append( "Connection: Keep-Alive\r\n"   );
				//packet.append( "Keep-Alive: 300\r\n"   );
				packet.append( "Host: " + domain + "\r\n" );
				packet.append( "\r\n" );

				out.write(packet.toString());
				out.flush();
				
				in = new DataInputStream(socket_telnet.getInputStream());
				resp = readInputStream(in);
                if(encoding==null) {
                	encoding = getPageCharset(resp);
                }
				in.close();
				out.close();
			}
		}catch(Exception e){
			try {
				resp = e.toString().getBytes(encoding);
			} catch (Exception e1) {
				resp = e.toString().getBytes();
			}
		}
		return resp;
	}
	
	public String readInputString(InputStream in)throws Exception{
		byte[] baout = readInputStream(in);
		return encoding==null? 
				new String(baout):
			    new String(baout,encoding);
	}
	
	public byte[] readInputStream(InputStream in)throws Exception{
		header = null;
		encoding = null;
        ByteArrayOutputStream baout = new ByteArrayOutputStream();   
        int ch;   
        long c = 0, len = Long.MAX_VALUE;
        WordMatcher m = new WordMatcher("\r\n\r\n");
        while ( (ch = in.read()) != -1  &&  c<len){
        	if(m.check(ch)){
        		parseHeader(baout.toByteArray());
        		encoding = getCharset()!=null?getCharset():encoding;
        		//if(isClosed()) break;
        		if(isChunked()){
        			baout = getChunked(in);
        			break;
        		}else{
            		len = getContentLength();
            		c = 0;
            		baout = new ByteArrayOutputStream();   
            		continue;
        		}
        	}
        	baout.write(ch);
        	c ++ ;
        }
        byte[] rs = baout.toByteArray();
        baout.close();
        return rs;
	}
	
	private ByteArrayOutputStream getChunked(InputStream in)throws Exception{
		ByteArrayOutputStream baout = new ByteArrayOutputStream(); 
		WordMatcher m = new WordMatcher("\r\n");
		ByteArrayOutputStream basize = null;
		
		int ch,c;
		basize = new ByteArrayOutputStream(); 
		m.reset();
		while ( (ch = in.read()) != -1 ){
			basize.write(ch);
			if(m.check(ch)){
				String str = new String(basize.toByteArray()).trim();
				if(str.length()<=0)break;
				long size = Long.parseLong(str,16);
				if(size<=0) break;
				c = 0;
				while(c<size){
					baout.write(in.read());
					c++;
				}
				basize = new ByteArrayOutputStream(); 
				m.reset();
			}
		}
		return baout;
	}
	
	private String getDomain(String temp){
		if(temp==null || temp.length()<=0) return "";
		temp = temp.trim();
		if(temp.startsWith("http://")) temp = temp.substring(7);
		if(temp.startsWith("https://")) temp = temp.substring(8);
		if(temp.indexOf("/")>0){
			return temp.substring(0,temp.indexOf("/"));
		}
		return temp;
	}
	
	private void parseHeader(byte[] headByte){
		header = new HashMap();
		try {
			String headerText = new String(headByte);
			String[] lines = headerText.split("\r\n");
			for(int i=0; i<lines.length; i++){
				String line = lines[i];
				if(line.indexOf(':')<0)continue;
				String name = line.substring(0,line.indexOf(':'));
				String value = line.substring(line.indexOf(':')+1);
				name = name.trim();
				value = value.trim();
				if(header.get(name)==null){
					header.put(name, value);
				}else{
					header.put(name, header.get(name)+"; "+value);
				}
			}
		} catch (Exception e) {
		}
	}

	public String getHeaderField(String name){
		if(header==null || header.size()<=0) return "";
		Iterator it = header.keySet().iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			if(key.equalsIgnoreCase(name)){
				return (String)header.get(key);
			}
		}
		return "";
	}
	
	public long getContentLength(){
		String contentLength = getHeaderField("Content-Length");
		if(contentLength==null || contentLength.length()<=0) return 0;
		return Long.parseLong(contentLength);
	}
	
	public boolean isChunked(){
		String chunked = getHeaderField("Transfer-Encoding");
		if(chunked==null || chunked.length()<=0) return false;
		return "chunked".equalsIgnoreCase(chunked);
	}
	
	public boolean isClosed(){
		String connection = getHeaderField("Connection");
		if(connection==null || connection.length()<=0) return false;
		return "close".equalsIgnoreCase(connection);
	}
	
	public String getCharset(){
		String contentType = getHeaderField("Content-Type");
		if(contentType==null || contentType.length()<=0) return null;
		contentType = contentType.toLowerCase();
		int x = contentType.indexOf("charset");
		int y = contentType.indexOf("=",x);
		if(x>0 && y>0){
			return contentType.substring(y+1);
		}
		return null;
	}

	private String getPageCharset(byte[] content){
		if(content==null || content.length<=0) return null;
		String regex = "(?i)content\\s*=\\s*\"text/.*?;\\s*charset\\s*=\\s*(.*?)\\s*\"(?-i)";
		String[] values = getGroup(new String(content),regex);
		if(values!=null && values.length>0){
			return values[0];
		}
		return null;
	}
	
	private String[] getGroup(String content, String regex){
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
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
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public int getConTimeout() {
		return conTimeout;
	}

	public void setConTimeout(int conTimeout) {
		this.conTimeout = conTimeout;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Map getHeader() {
		return header;
	}

	public void setHeader(Map header) {
		this.header = header;
	}
	
}