package com.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 系统工具类
 */
public class SysUtil {
	
	/**
	 * 执行命令行
	 * @param cmd
	 * @return
	 */
	public static byte[] exec(String cmd) {
		byte[] result = null;
		try{
				Process child = Runtime.getRuntime().exec(cmd);
				InputStream in = child.getInputStream();
				BufferedInputStream bin = new BufferedInputStream(in);
				ByteArrayOutputStream bufferout = new ByteArrayOutputStream();
				byte buf[] = new byte[65535];
				int  len;
				while( (len = bin.read(buf)) > -1 ){
					bufferout.write(buf, 0, len);
				}
				result = bufferout.toByteArray();
				bin.close();
				bufferout.close();

				in.close();
				child.waitFor();
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
}
