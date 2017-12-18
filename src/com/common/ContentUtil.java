package com.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ContentUtil {
	
	public static int BYTE_LENGTH=4096;

	public static byte[] zip(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return zip(is);
	}
	
	public static byte[] zip(InputStream is) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		ZipOutputStream gos = new ZipOutputStream(bos);  
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = is.read(b)) != -1;) {  
	    	gos.write(b, 0, n);
	    }  
	    is.close();
	    gos.close();
	    return bos.toByteArray();
	}
	
	public static byte[] unzip(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return unzip(is);
	}
	
	public static byte[] unzip(InputStream is) throws IOException{
	    ZipInputStream gis = new ZipInputStream(is);  
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = gis.read(b)) != -1;) {  
	        bos.write(b, 0, n);
	    }  
	    gis.close();
	    bos.close();
	    return bos.toByteArray();
	}
	
	public static byte[] gzip(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return gzip(is);
	}
	
	public static byte[] gzip(InputStream is) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		GZIPOutputStream gos = new GZIPOutputStream(bos);  
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = is.read(b)) != -1;) {  
	    	gos.write(b, 0, n);
	    }  
	    is.close();
	    gos.close();
	    return bos.toByteArray();
	}
	
	public static byte[] ungzip(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return ungzip(is);
	}
	
	public static byte[] ungzip(InputStream is) throws IOException{
	    GZIPInputStream gis = new GZIPInputStream(is);  
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = gis.read(b)) != -1;) {  
	        bos.write(b, 0, n);
	    }  
	    gis.close();
	    bos.close();
	    return bos.toByteArray();
	}
	
	
	public static byte[] deflater(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return deflater(is);
	}
	
	public static byte[] deflater(InputStream is) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		DeflaterOutputStream dos = new DeflaterOutputStream(bos);  
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = is.read(b)) != -1;) {  
	    	dos.write(b, 0, n);
	    }  
	    is.close();
	    dos.close();
	    return bos.toByteArray();
	}
	
	public static byte[] undeflater(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return undeflater(is);
	}
	
	public static byte[] undeflater(InputStream is) throws IOException{
		InputStream dis = null;
		try{
		   Class cls = Class.forName("java.util.zip.DeflaterInputStream");
		   dis = (InputStream)cls.getConstructor(InputStream.class).newInstance(is); 
		}catch(Exception ex){}		
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = dis.read(b)) != -1;) {  
	        bos.write(b, 0, n);
	    }  
	    dis.close();
	    bos.close();
	    return bos.toByteArray();
	}
	
	public static byte[] inflater(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return inflater(is);
	}
	
	public static byte[] inflater(InputStream is) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		OutputStream ios = null;
		try{
		   Class cls = Class.forName("java.util.zip.InflaterOutputStream");
		   ios = (OutputStream)cls.getConstructor(OutputStream.class).newInstance(bos); 
		}catch(Exception ex){}
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = is.read(b)) != -1;) {  
	    	ios.write(b, 0, n);
	    }  
	    is.close();
	    ios.close();
	    return bos.toByteArray();
	}
	
	public static byte[] uninflater(byte[] input) throws IOException{
		ByteArrayInputStream is = new ByteArrayInputStream(input);
		return uninflater(is);
	}
	
	public static byte[] uninflater(InputStream is) throws IOException{
		InflaterInputStream iis = new InflaterInputStream(is);  
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] b = new byte[BYTE_LENGTH];  
	    for (int n; (n = iis.read(b)) != -1;) {  
	        bos.write(b, 0, n);
	    }  
	    iis.close();
	    bos.close();
	    return bos.toByteArray();
	}
}
