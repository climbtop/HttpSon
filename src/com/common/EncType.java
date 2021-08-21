package com.common;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件格式
 */
public class EncType {

	public static String check(File f){
		String code = "Unknown";
		BufferedInputStream bin = null;
		try {
			bin = new BufferedInputStream(new FileInputStream(f));
			int p = (bin.read() << 8) + bin.read();
			switch (p) {
			case 0xefbb:
				code = "UTF-8";
				break;
			case 0xfffe:
				code = "Unicode";
				break;
			case 0xfeff:
				code = "Unicode big endian";
				break;
			default:
				code = check(bin); //"ANSI";
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally{
			if(bin!=null){
				try{bin.close();}catch(Exception e){}
			}
		}
		return code;
	}
	
	public static String check(InputStream bin)throws IOException{
		int b,utf8=0,gbk=0,m=-1,n=-1;
		
		while( (b=bin.read()) != -1){
			
			if(b<0x80){
				m=-1; n=-1;
			}
			
			if(b>=0x80 && m>=0){
				m++;
			}
			
			if(b>=0x80 && n>=0){
				n++;
			}
			
			if(b>=0xE0 && b<=0xEF && m<0){
				m++;
			}
			
			if(b>=0x80 && n<0){
				n++;
			}
			
			if(m>=2){
				m=-1;
				utf8++;
			}
			
			if(n>=1){
				n=-1;
				gbk++;
			}
		}
		
		double rate = (utf8*3*1.0) / (gbk*2);  
		if(rate>0.95)return "UTF-8";
		return "ANSI";
	}
	
	
	public static String check(String f){
		return check(new File(f));
	}
	
	public static String check(byte[] data){
		ByteArrayInputStream bStrm = null;
		try{
			bStrm = new ByteArrayInputStream(data);
			return check(bStrm);
		}catch(Exception e){
		}finally{
			if(bStrm!=null){
				try{bStrm.close();}catch(Exception e){}
			}
		}
		return "Unknown";
	}
	
	public static String check(String content,String enc){
		try {
			return check(content.getBytes(enc));
		} catch (Exception e) {
		}
		return "Unknown";
	}
}
