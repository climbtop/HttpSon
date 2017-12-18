package com.common;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WinUtil {
	
	/**
	 * 定时运行一任务
	 * @param exe //任务的绝对路径.
	 * @param name//任务出现后的进程名称.
	 * @return
	 */
	public static boolean runAt(String exe,String name, boolean opened){
		if(exe==null) return false;
		if(name==null || name.length()<0){
			name = exe.substring(exe.lastIndexOf("/")+1);
			name = exe.substring(exe.lastIndexOf("\\")+1);
		}
		try{
			int i = 0,max = 10;
			while(true){
				int seconds = new Date().getSeconds();
				if(seconds>55) seconds = 0;
				String time = WinUtil.nextMinute();
				Runtime.getRuntime().exec("at /delete /yes");// at /delete /yes
				// at 21:04 /interactive  "c:\vidalia.bat"
				String cmd = "at "+time+" "+(opened?"/interactive":"")+" \""+exe+"\" ";
				System.out.println(cmd);
				Process proc = Runtime.getRuntime().exec(cmd);
				proc.waitFor();

				Thread.sleep((60-seconds)*1000);
				if(WinUtil.findProc(name)||i>=max)break;
				i++;
			}
			return i<max;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public static boolean runAt(String exe,String name){
		return runAt(exe,name,false);
	}
	
	/**
	 * 下一个时间分钟
	 */
	public static String nextMinute(){
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		c.add(Calendar.MINUTE, 1);
		c.add(Calendar.SECOND, 3);
		String time = sdf.format(c.getTime());
		return time;
	}
	
	/**
	 * 判定进程是否存在
	 */
	public static boolean findProc(String name){
		//tasklist /FI "IMAGENAME eq vidalia.exe"
		byte[] result = SysUtil.exec("tasklist /FI \"IMAGENAME eq "+name+"\"");
		try {
			String values = new String(result,"GBK").toLowerCase();
			return values.indexOf(name.toLowerCase())>=0;
		} catch (UnsupportedEncodingException e) {
		}
		return false;
	}
	
	/**
	 * 删除某进程树
	 */
	public static void killProc(String name, boolean child){
		//taskkill /IM "vidalia.exe" /F /T
		try {
			Runtime.getRuntime().exec("taskkill /IM \""+name+"\" /F "+(child?"/T":""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void killProc(String name){
		killProc(name,true);
	}
}
